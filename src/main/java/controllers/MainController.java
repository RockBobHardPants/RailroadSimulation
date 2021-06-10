package controllers;

import com.sun.tools.javac.Main;
import exception.InvalidConfigurationException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import map.Field;
import map.FieldType;
import map.Map;
import util.Util;
import vehicles.rail.composition.Composition;
import vehicles.rail.wagon.Wagon;
import vehicles.road.Car;
import vehicles.road.Vehicle;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.FileHandler;
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
    public Handler mapHandler;
    public Handler mainHandler;
    public Handler configHandler;
    private List<Composition> compositionList;
    private List<Vehicle> vehicleList;

    //TODO: Loggeri, sredi.
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

    //TODO: obrisi voz koji je zavrsio kretanje.

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
            } catch (InvalidConfigurationException exception){
                var message = file.getName() + " " + exception.getMessage();
                Logger.getLogger(CONFIG_LOGGER).log(Level.WARNING, message);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private final Runnable trainsFileWatcherRunnable = () -> {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchKey = pathToDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        var valid = true;
        while (valid){
            try {
                watchKey = watchService.take();
            } catch (InterruptedException e) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, "Interrupted");
                Thread.currentThread().interrupt();
            }
            for(WatchEvent<?> event : watchKey.pollEvents()){
                WatchEvent.Kind<?> kind = event.kind();
                WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
                if(kind.equals(ENTRY_CREATE) || kind.equals(ENTRY_MODIFY)) {
                    var fileName = pathWatchEvent.context();
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, e.getMessage());
                    }
                    try {
                        var composition = Util.getComposition(Util.getTrainJSON(pathToDirectory.resolve(fileName)));
                        if (compositionList.stream().noneMatch(composition1 -> composition1.getLabel().equals(composition.getLabel()))) {
                            compositionList.add(composition);
                            composition.start();
                        }
                    } catch (InvalidConfigurationException exception) {
                        var message = fileName + " " + exception.getMessage();
                        Logger.getLogger(CONFIG_LOGGER).log(Level.WARNING, message);
                    }
                }
            }
            valid = watchKey.reset();
        }
    };


    public void initialize()  {
        try {
            mainHandler = new FileHandler("logs" + File.separator + "main.log", 50, 3, true);
            configHandler = new FileHandler("logs" + File.separator + "config.log", 50, 3, true);
            mapHandler = new FileHandler("logs" + File.separator + "map.log", 50, 3, true);
            configHandler.setLevel(Level.CONFIG);
//            mapHandler.setLevel(Level.CONFIG);
            Logger.getLogger(MainController.class.getName()).addHandler(mainHandler);
            Logger.getLogger(CONFIG_LOGGER).addHandler(configHandler);
            Logger.getLogger(Map.class.getName()).addHandler(mapHandler);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        compositionList = new ArrayList<>();
        vehicleList = new ArrayList<>();
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

    }

    private void moveVehicles() {
        var car1 = new Car(Map.getMapMatrix()[29][8], true);
        vehicleList.add(car1);
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
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, e.getMessage());
                }
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
                    } catch (InterruptedException e) {
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                } else {
                    break;
                }
            }
        });
        updatingThread.start();
    }
}
