package railroad_simulation.controllers;

import org.json.JSONException;
import railroad_simulation.RailroadSimulation;
import railroad_simulation.exception.InvalidConfigurationException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import railroad_simulation.map.Field;
import railroad_simulation.map.FieldType;
import railroad_simulation.map.Map;
import railroad_simulation.map.RailroadCrossing;
import railroad_simulation.util.Util;
import railroad_simulation.vehicles.rail.composition.Composition;
import railroad_simulation.vehicles.rail.wagon.Wagon;
import railroad_simulation.vehicles.road.Car;
import railroad_simulation.vehicles.road.Vehicle;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class MainController {
    public static final String CONFIG_LOGGER = "config";
    public static final String INVALID_COMPOSITION = "Invalid composition configuration";
    @FXML
    public GridPane gridPane;

    @FXML
    public Button ramp;

    @FXML
    private Button button;

    @FXML
    private Button moveButton;

    private WatchService watchService;
    private WatchKey watchKey;
    private final Path pathToDirectory = Paths.get("trains");
    Thread watcherThread;
    private List<Composition> compositionList;
    private List<Vehicle> vehicleList;

    public void renderItem(Object item, Field currentField, ImageView imageView){
        ObservableList<Node> nodes = gridPane.getChildren();
        StackPane currentNode = null;
        for(Node node : nodes){
            if(currentField != null) {
                if (GridPane.getRowIndex(node) == currentField.getCoordinates().getRow() &&
                        GridPane.getColumnIndex(node) == currentField.getCoordinates().getColumn()) {
                    currentNode = (StackPane) node;
                }
            }
        }
        if(currentField != null) {
            var currentNodeFinal = currentNode;
            Platform.runLater(() -> {
                if ((item instanceof Composition || item instanceof  Wagon)
                        && currentField.getFieldType().equals(FieldType.INTERSECTION)
                        && currentField.getRoadCode() == 2){
                    imageView.setRotate(90);
                } else if ((item instanceof Composition || item instanceof  Wagon)
                        && currentField.getFieldType().equals(FieldType.INTERSECTION)
                        && currentField.getRoadCode() != 2){
                    imageView.setRotate(0);
                } else {
                    imageView.setRotate(currentField.getFieldRotation());
                }
                currentNodeFinal.setAlignment(Pos.CENTER);
                try {
                    currentNodeFinal.getChildren().add(1, imageView);
                } catch (Exception ignored){}                                                                           //Iscrtavanje iste slike vise puta, prilikom ulaska voza u stanicu
            });
        }
    }

    //TODO: obrisi voz ili vozilo koji je zavrsio kretanje tako sto ces pogledati sve koji su !isAlive()
    // i proslijediti njihov currentfield da se iz tog node-a obrisu sva djeca

//    private void renderMapItem(List<Field> fieldList){
//        StackPane currentNode = null;
//        ObservableList<Node> nodes = gridPane.getChildren();
//        for(Field currentField : fieldList) {
//            for (Node node : nodes) {
//                if (currentField != null && (GridPane.getRowIndex(node) == currentField.getCoordinates().getRow() &&
//                        GridPane.getColumnIndex(node) == currentField.getCoordinates().getColumn())) {
//                    currentNode = (StackPane) node;
//                }
//            }
//            var currentNodeFinal = currentNode;
//            Platform.runLater(() -> currentNodeFinal.getChildren().remove(1));
//        }
//    }

    private void loadInitialCompositions() {
        for (File file : Objects.requireNonNull(pathToDirectory.toFile().listFiles())){
            try {
                compositionList.add(Util.getComposition(Util.getTrainJSON(file.toPath())));
            } catch (JSONException | InvalidConfigurationException exception){
                var message = file.getName() + " " + exception.getMessage();
                RailroadSimulation.CONFIG_LOGGER.log(Level.WARNING, message, exception);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private final Runnable trainsFileWatcherRunnable = () -> {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchKey = pathToDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
        } catch (IOException ioException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, ioException.getMessage(), ioException);
        }
        var valid = true;
        while (valid){
            try {
                watchKey = watchService.take();
            } catch (InterruptedException exception) {
                RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                Thread.currentThread().interrupt();
            }
            for(WatchEvent<?> event : watchKey.pollEvents()){
                WatchEvent.Kind<?> kind = event.kind();
                WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
                if(kind.equals(ENTRY_CREATE) || kind.equals(ENTRY_MODIFY)) {
                    var fileName = pathWatchEvent.context();
                    try {
                        sleep(100);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                    }
                    try {
                        var composition = Util.getComposition(Util.getTrainJSON(pathToDirectory.resolve(fileName)));
                        if (compositionList.stream().noneMatch(composition1 -> composition1.getLabel().equals(composition.getLabel()))) {
                            compositionList.add(composition);
                            composition.start();
                        }
                    } catch (JSONException | InvalidConfigurationException exception) {
                        var message = fileName + " " + exception.getMessage();
                        RailroadSimulation.CONFIG_LOGGER.log(Level.WARNING, message, exception);
                    }
                }
            }
            valid = watchKey.reset();
        }
    };


    public void initialize()  {
        compositionList = Collections.synchronizedList(new ArrayList<>());
        vehicleList = Collections.synchronizedList(new ArrayList<>());
        loadInitialCompositions();
        watcherThread = new Thread(trainsFileWatcherRunnable);
        watcherThread.start();
        moveButton.setOnMouseClicked(mouseEvent -> moveTrain());
        button.setOnMouseClicked(mouseEvent -> moveVehicles());
        ramp.setOnMouseClicked(mouseEvent -> turnRamp());
        for (var row = 0; row < 30; row++){
            for (var column = 0 ; column < 30; column++){
                var stackPane = new StackPane();
                stackPane.setAlignment(Pos.CENTER);
                stackPane.getChildren().add(Map.getMapMatrix()[row][column].getFieldImageView());
                gridPane.add(stackPane, column, row);
            }
        }
    }

    private void turnRamp() {
        System.out.println(vehicleList.size());
    }

    private void moveVehicles() {
        var car1 = new Car(Map.getRoadSegmentList().get(1));
        var car2 = new Car(Map.getRoadSegmentList().get(1));
        var car3 = new Car(Map.getRoadSegmentList().get(1));
        var car4 = new Car(Map.getRoadSegmentList().get(1));
        var car5 = new Car(Map.getRoadSegmentList().get(1));
        vehicleList.add(car1);
        vehicleList.add(car2);
        vehicleList.add(car3);
        vehicleList.add(car4);
        vehicleList.add(car5);
        var vehicleUpdatingThread = new Thread(()->{
            for(Vehicle vehicle : vehicleList){
                vehicle.start();
            }
            while (true) {
                if(!vehicleList.isEmpty()){
                    for(Vehicle vehicle : vehicleList){
                        if(vehicle.isUpdated()){
                            renderItem(vehicle, vehicle.getCurrentField(), vehicle.getImageView());
                        }
                    }
                }
                try {
                    sleep(1000/60);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                }
                Collections.synchronizedList(vehicleList).removeIf(vehicle -> !vehicle.isAlive());
            }
        });
        vehicleUpdatingThread.start();
    }


    private void moveTrain() {
        var updatingThread = new Thread(() -> {
            for (Composition composition : compositionList){
                composition.start();
            }
            while (true) {
                if (!compositionList.isEmpty()) {
                    for (Composition composition : compositionList) {
                        if (composition.isUpdated()) {
                            renderItem(composition, composition.getCurrentField(), composition.getFrontLocomotive().getLocomotiveImageView());
                            if (composition.getWagonList() != null) {
                                composition.getWagonList().forEach(wagon -> renderItem(wagon, wagon.getCurrentField(), wagon.getWagonImageView()));
                            }
                            if (composition.getRearLocomotive() != null) {
                                renderItem(composition, composition.getRearLocomotive().getCurrentField(), composition.getRearLocomotive().getLocomotiveImageView());
                            }
                            composition.setUpdated(false);
                        }
                    }
                    try {
                        sleep(1000 / 60);
                    } catch (InterruptedException exception) {
                        RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                        Thread.currentThread().interrupt();
                    }
                } else {
                    break;
                }
                Collections.synchronizedList(compositionList).removeIf(composition -> !composition.isAlive());
            }
        });
        updatingThread.start();
    }
}
