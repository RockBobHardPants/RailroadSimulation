package controllers;

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
import map.Map;
import util.Util;
import vehicles.rail.composition.Composition;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class MainController {
    @FXML
    public GridPane gridPane;

    @FXML
    private Button button;

    @FXML
    private Button moveButton;

    private WatchService watchService;
    private WatchKey watchKey;
    private final Path pathToDirectory = Paths.get("trains");
    Thread watcherThread;

    private List<Composition> compositionList;

    public void renderItem(Field currentField, ImageView imageView){
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
                imageView.setRotate(currentField.getFieldRotation());
                currentNodeFinal.setAlignment(Pos.CENTER);
                currentNodeFinal.getChildren().add(1, imageView);
            });
        }
    }

    private void renderMapItem(List<Field> fieldList){
        StackPane currentNode = null;
        ObservableList<Node> nodes = gridPane.getChildren();
        for(Field currentField : fieldList) {
            for (Node node : nodes) {
                if (currentField != null && (GridPane.getRowIndex(node) == currentField.getCoordinates().getRow() &&
                        GridPane.getColumnIndex(node) == currentField.getCoordinates().getColumn())) {
                    currentNode = (StackPane) node;
                }
            }
            var currentNodeFinal = currentNode;
            Platform.runLater(() -> currentNodeFinal.getChildren().remove(1));
        }
    }

    private void loadInitialCompositions() {
        for (File file : Objects.requireNonNull(pathToDirectory.toFile().listFiles())){
            compositionList.add(Util.getComposition(Util.getTrainJSON(file.toPath())));
        }
    }


    @SuppressWarnings("unchecked")
    private final Runnable fileWatcherRunnable = new Runnable() {
        @Override
        public void run() {
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
                    Logger.getLogger("global").log(Level.SEVERE, "Interrupted");
                    Thread.currentThread().interrupt();
                }
                for(WatchEvent<?> event : watchKey.pollEvents()){
                    WatchEvent.Kind<?> kind = event.kind();
                    WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
                    if(kind.equals(ENTRY_CREATE)) {
                        var fileName = pathWatchEvent.context();
                        var composition = Util.getComposition(Util.getTrainJSON(pathToDirectory.resolve(fileName)));
                        if(compositionList.stream().noneMatch(composition1 -> composition1.getLabel().equals(composition.getLabel()))) {
                            compositionList.add(composition);
                        }
                    }
                }
                valid = watchKey.reset();
            }
        }
    };


    public void initialize()  {
        compositionList = new ArrayList<>();
        loadInitialCompositions();
        watcherThread = new Thread(fileWatcherRunnable);
        watcherThread.start();
        moveButton.setOnMouseClicked(mouseEvent -> moveTrain());
        for (var row = 0; row < 30; row++){
            for (var column = 0 ; column < 30; column++){
                var stackPane = new StackPane();
                stackPane.setAlignment(Pos.CENTER);
                stackPane.getChildren().add(Map.getMapMatrix()[row][column].getFieldImageView());
                gridPane.add(stackPane, column, row);
            }
        }
    }


    private void moveVehicle() {

    }
    private void moveTrain() {
        compositionList.forEach(Thread::start);
        var updatingThread = new Thread(() -> {
            while (true) {
                for(Composition composition : compositionList) {
                    if(composition.isUpdated() && !composition.isFinished()){
                        renderItem(composition.getCurrentField(), composition.getFrontLocomotive().getLocomotiveImageView());
                        if(composition.getWagonList() != null) {
                            composition.getWagonList().forEach(wagon -> renderItem(wagon.getCurrentField(), wagon.getWagonImageView()));
                        }
                        if(composition.getRearLocomotive() != null) {
                            renderItem(composition.getRearLocomotive().getCurrentField(), composition.getRearLocomotive().getLocomotiveImageView());
                        }
                        composition.setUpdated(false);
                    }
                }
                try {
                    Thread.sleep(1000/60);
                } catch (InterruptedException e) {
                    Logger.getLogger("global").log(Level.SEVERE, "Interrupted");
                    Thread.currentThread().interrupt();
                }
            }
        });
        updatingThread.start();
    }
}
