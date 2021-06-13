package railroad_simulation.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.json.JSONException;
import railroad_simulation.RailroadSimulation;
import railroad_simulation.exception.InvalidConfigurationException;
import railroad_simulation.map.Field;
import railroad_simulation.map.FieldType;
import railroad_simulation.map.Map;
import railroad_simulation.map.RailroadSegment;
import railroad_simulation.util.Util;
import railroad_simulation.vehicles.rail.composition.Composition;
import railroad_simulation.vehicles.rail.wagon.Wagon;
import railroad_simulation.vehicles.road.Car;
import railroad_simulation.vehicles.road.Lorry;
import railroad_simulation.vehicles.road.Vehicle;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class MainController {
    public static final String MENU_WINDOW_FXML = "../../menu_window.fxml";
    public static final String INVALID_COMPOSITION = "Invalid composition configuration";
    private static final Path pathToDirectory = Paths.get(RailroadSimulation.TRAINS_FOLDER);
    public static final int FPS = 1_000 / 60;
    public static final int VEHICLE_SPAWN_INTERVAL = 2_000;

    @FXML
    public GridPane gridPane;
    @FXML
    public Button menuButton;
    @FXML
    public AnchorPane mainWindowAnchor;
    @FXML
    private Button startSimulationButton;

    private double xOffset = 0;
    private double yOffset = 0;

    private WatchService watchService;
    private WatchKey watchKey;
    private Thread watcherThread;
    private List<Composition> compositionList;
    private List<Vehicle> vehicleList;
    private Thread vehicleSpawnThread;

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

    private void removeVehicleFromMap(Field currentField, ImageView imageView) {
        StackPane currentNode = null;
        ObservableList<Node> nodes = gridPane.getChildren();
        for (Node node : nodes) {
            if (currentField != null && (GridPane.getRowIndex(node) == currentField.getCoordinates().getRow() &&
                    GridPane.getColumnIndex(node) == currentField.getCoordinates().getColumn())) {
                currentNode = (StackPane) node;
            }
        }
        var currentNodeFinal = currentNode;
        synchronized (this){
            if(currentNodeFinal != null) {
                Platform.runLater(() -> currentNodeFinal.getChildren().remove(imageView));
            }
        }
    }

    private void removeCompositionFromMap(Composition composition){
        StackPane currentNode = null;
        ObservableList<Node> nodeList = gridPane.getChildren();
        for (Node node : nodeList) {
            if ((GridPane.getRowIndex(node) == composition.getCurrentField().getCoordinates().getRow() &&
                    GridPane.getColumnIndex(node) == composition.getCurrentField().getCoordinates().getColumn())) {
                currentNode = (StackPane) node;
            }
        }
        var currentNodeFinal = currentNode;
        List<ImageView> compositionImageViewList = new ArrayList<>();
        compositionImageViewList.add(composition.getFrontLocomotive().getLocomotiveImageView());
        if(composition.getRearLocomotive() != null){
            compositionImageViewList.add(composition.getRearLocomotive().getLocomotiveImageView());
        }
        if(composition.getWagonList() != null){
            composition.getWagonList().forEach(wagon -> compositionImageViewList.add(wagon.getWagonImageView()));
        }
        if(currentNodeFinal != null){
            compositionImageViewList.forEach(imageView -> Platform.runLater(() -> currentNodeFinal.getChildren().remove(imageView)));
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

    public void initialize()  {                                                                                         //Nakon koristenja sinhronizovanih lista iz Collections paketa
        compositionList = new CopyOnWriteArrayList<>(Util.loadInitialCompositions());                                   //ipak je odluceno da se koriste CopyOnWrite liste, jer prethodno
        vehicleList = new CopyOnWriteArrayList<>();                                                                     //navedene nisu totalno thread-safe
        stopSimulation = false;
        mainWindowAnchor.setOnMousePressed(mouseEvent -> {
            xOffset = mainWindowAnchor.getScene().getWindow().getX() - mouseEvent.getScreenX();
            yOffset = mainWindowAnchor.getScene().getWindow().getY() - mouseEvent.getScreenY();
        });
        mainWindowAnchor.setOnMouseDragged(mouseEvent -> {
            mainWindowAnchor.getScene().getWindow().setX(mouseEvent.getScreenX() + xOffset);
            mainWindowAnchor.getScene().getWindow().setY(mouseEvent.getScreenY() + yOffset);
        });
        watcherThread = new Thread(trainsFileWatcherRunnable);
        watcherThread.start();
        startSimulationButton.setOnMouseClicked(mouseEvent -> {
            moveTrain();
            moveVehicles();
        });
        menuButton.setOnMouseClicked(mouseEvent -> enterMenu());
        for (var row = 0; row < 30; row++){
            for (var column = 0 ; column < 30; column++){
                var stackPane = new StackPane();
                stackPane.setAlignment(Pos.CENTER);
                stackPane.getChildren().add(Map.getMapMatrix()[row][column].getFieldImageView());
                gridPane.add(stackPane, column, row);
            }
        }
    }

    private void enterMenu() {
        var menuURL = this.getClass().getResource(MENU_WINDOW_FXML);
        var loader = new FXMLLoader(menuURL);
        try {
            loader.load();
            ((MenuController)loader.getController()).setMainController(this);
            Parent root = loader.getRoot();
            var menuScene = new Scene(root);
            var menuStage = new Stage();
            menuScene.setFill(Color.TRANSPARENT);
            menuStage.setScene(menuScene);
            menuStage.initStyle(StageStyle.UNDECORATED);
            menuStage.initStyle(StageStyle.TRANSPARENT);
            menuStage.initOwner(mainWindowAnchor.getScene().getWindow());
            menuStage.initModality(Modality.WINDOW_MODAL);
            double centerX = mainWindowAnchor.getScene().getWindow().getX()                                             //Centriranje menija preko glavnog prozora
                    + (mainWindowAnchor.getScene().getWindow().getWidth()/2d);
            double centerY = mainWindowAnchor.getScene().getWindow().getY()
                    + (mainWindowAnchor.getScene().getWindow().getHeight()/2d);
            menuStage.setAlwaysOnTop(false);
            menuStage.setOnShown(event -> {
                menuStage.setX(centerX - menuStage.getWidth()/2d);
                menuStage.setY(centerY - menuStage.getHeight()/2d);
            });
            menuStage.showAndWait();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private boolean stopSimulation = false;

    public void restartSimulation(){                                                                                    //zaustave se trenutno pokrenuti threadovi
        stopSimulation = true;                                                                                          //tako sto se stopSimulation postavi na true
        try {
            sleep(100);                                                                                            //da se izbjegne concurent exception
        } catch (InterruptedException exception) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            Thread.currentThread().interrupt();
        }
        vehicleList.forEach(vehicle -> removeVehicleFromMap(vehicle.getCurrentField(), vehicle.getImageView()));        //obrisi sve slicice sa mape
        compositionList.forEach(this::removeCompositionFromMap);
        vehicleList.clear();
        compositionList.clear();
        Map.getRailroadCrossingList().forEach(railroadCrossing -> railroadCrossing.setSafeToCross(true));               //postavi pruzne prelaze na true i pocisti vozove sa segmenata
        Map.getRailroadSegmentList().forEach(RailroadSegment::clearCompositionList);
        initialize();                                                                                                   //ponovo inicijallizuj sve
    }

    private void spawnVehicles(){
        vehicleSpawnThread = new Thread(() -> {
            while (!stopSimulation){
                Vehicle vehicle;
                var roadSegment = Map.getRoadSegmentList().get(new Random().nextInt(3));             //na nasumican nacin odaberi segment na koji ce se vozilo postaviti
                if(roadSegment.getVehicleList().size() < roadSegment.getMaxNumberOfVehicles()) {                        //provjeri broj vozila na segmentu
                    var randomVehicle = new Random().nextInt(5);
                    if (randomVehicle == 0) {
                        vehicle = new Lorry(roadSegment);
                    } else {
                        vehicle = new Car(roadSegment);
                    }
                    vehicleList.add(vehicle);
                    vehicle.start();
                }
                try {
                    sleep(VEHICLE_SPAWN_INTERVAL);
                } catch (InterruptedException exception) {
                    RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                    Thread.currentThread().interrupt();
                }
            }
        });
        vehicleSpawnThread.start();
    }

    private void moveVehicles() {
        spawnVehicles();
        var vehicleUpdatingThread = new Thread(()->{
            while (!stopSimulation) {
                synchronized (this) {
                    if (!vehicleList.isEmpty()) {
                        for (Vehicle vehicle : vehicleList) {
                            if (vehicle.isUpdated()) {
                                renderItem(vehicle, vehicle.getCurrentField(), vehicle.getImageView());
                            }
                        }
                    }
                    try {
                        sleep(FPS);
                    } catch (InterruptedException exception) {
                        RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                        Thread.currentThread().interrupt();
                    }
                    Collections.synchronizedList(vehicleList).forEach(vehicle -> {
                        if (!vehicle.isAlive()) {
                            removeVehicleFromMap(vehicle.getCurrentField(), vehicle.getImageView());
                        }
                    });
                    Collections.synchronizedList(vehicleList).removeIf(vehicle -> !vehicle.isAlive());
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
            while (!stopSimulation) {
                if (!compositionList.isEmpty()) {
                    for (Composition composition : compositionList) {
                        if (composition.isUpdated()) {                                                                  //ako se kompozicija pomjerila
                            renderItem(composition, composition.getCurrentField(),                                      //renderuj svaki dio posebno
                                    composition.getFrontLocomotive().getLocomotiveImageView());
                            if (composition.getWagonList() != null) {
                                composition.getWagonList().forEach(wagon -> renderItem(wagon,
                                        wagon.getCurrentField(), wagon.getWagonImageView()));
                            }
                            if (composition.getRearLocomotive() != null) {
                                renderItem(composition, composition.getRearLocomotive().getCurrentField(),
                                        composition.getRearLocomotive().getLocomotiveImageView());
                            }
                            composition.setUpdated(false);
                        }
                    }
                    try {
                        sleep(FPS);
                    } catch (InterruptedException exception) {
                        RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                        Thread.currentThread().interrupt();
                    }
                }
                Collections.synchronizedList(compositionList).forEach(composition -> {                                  //ako je thread ugasen, obrisi tu kompoziciju sa mape
                    if(!composition.isAlive()) {
                        removeCompositionFromMap(composition);
                    }
                });
                Collections.synchronizedList(compositionList).removeIf(composition -> !composition.isAlive());          //obrisi tu kompoziciju iz liste
            }
        });
        updatingThread.start();
    }
}
