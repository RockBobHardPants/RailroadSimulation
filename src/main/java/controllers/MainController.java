package controllers;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import map.Field;
import map.FieldType;
import map.Segment;
import map.Station;
import util.Util;
import vehicles.rail.locomotive.Locomotive;
import vehicles.rail.locomotive.LocomotiveDrive;
import vehicles.rail.locomotive.LocomotiveType;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainController {
    @FXML
    public GridPane gridPane;

    @FXML
    private Button button;

    @FXML
    private Button moveButton;

    private List<Field> fieldsMap;
    private List<StackPane> stackPanes;
    private List<Locomotive> locomotives;
    private List<Station> stations;
    private List<Segment> segments;
    private Field[][] fieldMatrix;
    private Locomotive locomotive;

    private List<String> readMap(){
        List<String> map = new ArrayList<>();
        String line;
        var mapFile = new File(Paths.get("").toAbsolutePath() + File.separator + "map.txt");
        try (var bufferedReader = new BufferedReader(new FileReader(mapFile))){
            while ((line = bufferedReader.readLine()) != null) {
//            Predicate<String> filter = Pattern.compile("[A-Z0-9]{2}").asPredicate();
//            map = bufferedReader.lines().map(lines -> lines.split(" ")).flatMap(Arrays::stream).collect(Collectors.toList());
                List<String> matches = Pattern.compile("[A-Z0-9]{2}").matcher(line).results().map(MatchResult::group).collect(Collectors.toList());
                map.addAll(matches);
            }
        } catch (IOException exception){
            Logger.getLogger("global").log(Level.SEVERE, "IO Exception");
        }
//        readSegments();
        return map;
    }

    public void renderItem(Field currentField, Field previousField,ImageView imageView){
        ObservableList<Node> nodes = gridPane.getChildren();
        StackPane currentNode = null;
        StackPane previousNode = null;
        for(Node node : nodes){
            if(GridPane.getRowIndex(node) == currentField.getCoordinates().getRow() &&
                    GridPane.getColumnIndex(node) == currentField.getCoordinates().getColumn()){
                currentNode = (StackPane) node;
            } else if(GridPane.getRowIndex(node) == previousField.getCoordinates().getRow() &&
                    GridPane.getColumnIndex(node) == previousField.getCoordinates().getColumn()){
                previousNode = (StackPane) node;
            }
        }
        var currentNodeFinal = currentNode;
        var previousNodeFinal = previousNode;
        Platform.runLater(() -> {
            imageView.setRotate(imageView.getRotate() + currentField.getFieldRotation());
            currentNodeFinal.setAlignment(Pos.CENTER);
            previousNodeFinal.setAlignment(Pos.CENTER);
//            previousNodeFinal.getChildren().remove(1);
            currentNodeFinal.getChildren().add(1,imageView);
            System.out.println(currentNodeFinal);
        });
    }

//    private void readSegments(){
//        String line;
//        var mapFile = new File(Paths.get("").toAbsolutePath() + File.separator + "segments.txt");
//        try (var bufferedReader = new BufferedReader(new FileReader(mapFile))) {
//            while ((line = bufferedReader.readLine()) != null){
//                List<String> matches = Pattern.compile("[0-9]{1,2},[0-9]{1,2}").matcher(line).results().map(MatchResult::group).collect(Collectors.toList());
//                matches.stream().map(match -> match.split(",")).reduce((a -> )
//            }
//        } catch (IOException ioException) {
//            Logger.getLogger("global").log(Level.SEVERE, "IO Exception");
//        }
//    }

    private void spawnTrain() {
        locomotive = new Locomotive(LocomotiveDrive.DIESEL, LocomotiveType.MANEUVER, 50.0, "A", stations.stream().filter(station -> station.getStationId().equals("C")).findFirst().get(), stations.stream().filter(station -> station.getStationId().equals("A")).findFirst().get());
        locomotives = new ArrayList<>();
//        Optional<Field> initialField = fieldsMap.stream().filter(field -> field.getFieldType().equals(FieldType.RAILROAD)).findAny();
//        List<Field> listOfRailroadFields = fieldsMap.stream().filter(field -> field.getFieldType().equals(FieldType.RAILROAD)).collect(Collectors.toList());
//        var field = listOfRailroadFields.get(new Random().nextInt(listOfRailroadFields.size()));
        var field = stations.stream().filter(station -> station.getStationId().equals("A")).findFirst().get().getStationFields().get(1);
        locomotive.setCurrentField(field);
        locomotives.add(locomotive);
    }

    private void addStation(String label, int i, int j){
        var field = new Field(FieldType.STATION, true, i, j, 0);
        fieldsMap.add(field);
        fieldMatrix[i][j] = field;
        var station = new Station(label.replaceAll("[0-9]", ""));
        station.addStationField(field);
        var existingStation = stations.stream().filter(station1 -> station1.getStationId().equals(station.getStationId())).findFirst();
        if(existingStation.isPresent()) {
            int index = stations.indexOf(existingStation.get());
            stations.get(index).addStationField(field);
        } else {
            stations.add(station);
        }
    }

    //TODO prebaciti inicijalizaciju u Util klasu, da vraća samo fieldMatrix po čemu će se iscrtati gridPane;
    //TODO Napraviti mapu za svaku stanicu tako da se zna koji izlaz iz stanice je za koji smjer i učitaj ga u svaku stanicu
    //TODO Napravi sinhronizaciju nad učitanim segmentima 

    public void initialize() throws FileNotFoundException {
        button.setOnMouseClicked(mouseEvent -> {
            spawnTrain();
        });
        String mapItem;
        fieldMatrix = new Field[30][30];
        moveButton.setOnMouseClicked(mouseEvent -> {
            moveTrain();
        });
        List<String> map = readMap();
        fieldsMap = new ArrayList<>();
        stackPanes = new ArrayList<>();
        stations = new ArrayList<>();
        segments = new ArrayList<>();
        String pathToImages = Paths.get("").toAbsolutePath() + File.separator + "images" + File.separator;
        var grassImage = new Image(new FileInputStream(pathToImages + "grassTile.png"));
        var roadImage = new Image(new FileInputStream(pathToImages + "roadTile.png"));
        var stationImage1 = new Image(new FileInputStream(pathToImages + "stationTile1.png"));
        var stationImage2 = new Image(new FileInputStream(pathToImages + "stationTile2.png"));
        var stationImage3 = new Image(new FileInputStream(pathToImages + "stationTile3.png"));
        var stationImage4 = new Image(new FileInputStream(pathToImages + "stationTile4.png"));
        var railVerticalImage = new Image(new FileInputStream(pathToImages + "railVertical.png"));
        var turn1 = new Image(new FileInputStream(pathToImages + "turn1.png"));
        var intersectionVertical = new Image(new FileInputStream(pathToImages + "intersectionVertical.png"));
        var counter = 0;
        for (var i = 0; i < 30; i++){
            for (var j = 0; j < 30; j++){
                mapItem = map.get(counter);
                ImageView imageView;
                var stackPane = new StackPane();
                switch (mapItem){
                    case "RR" -> {
                        imageView = new ImageView(roadImage);
                        var field = new Field(FieldType.ROAD, false, i, j, 0);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "TH" -> {
                        imageView = new ImageView(railVerticalImage);
                        var field = new Field(FieldType.RAILROAD, true, i, j, 90);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "TV" -> {
                        imageView = new ImageView(railVerticalImage);
                        var field = new Field(FieldType.RAILROAD, true, i, j, 0);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "A1", "B1", "C1", "D1", "E1" -> {
                        imageView = new ImageView(stationImage1);
                        addStation(mapItem, i , j);
                    }
                    case "A2", "B2", "C2", "D2", "E2" -> {
                        imageView = new ImageView(stationImage2);
                        addStation(mapItem, i , j);
                    }
                    case "A3", "B3", "C3", "D3", "E3" -> {
                        imageView = new ImageView(stationImage3);
                        addStation(mapItem, i , j);
                    }
                    case "A4", "B4", "C4", "D4", "E4" -> {
                        imageView = new ImageView(stationImage4);
                        addStation(mapItem, i , j);
                    }
                    case "T1" -> {
                        imageView = new ImageView(turn1);
                        imageView.setRotate(-45);
                        var field = new Field(FieldType.RAILROAD, true, i, j, 45);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "T2" -> {
                        imageView = new ImageView(turn1);
                        imageView.setRotate(-45);
                        var field = new Field(FieldType.RAILROAD, true, i, j, 135);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "T3" -> {
                        imageView = new ImageView(turn1);
                        imageView.setRotate(-45);
                        var field = new Field(FieldType.RAILROAD, true, i, j, 225);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "T4" -> {
                        imageView = new ImageView(turn1);
                        imageView.setRotate(-45);
                        var field = new Field(FieldType.RAILROAD, true, i, j, 315);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "IV" -> {
                        imageView = new ImageView(intersectionVertical);
                        var field = new Field(FieldType.INTERSECTION, true, i, j, 0);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    case "IH" -> {
                        imageView = new ImageView(intersectionVertical);
                        var field = new Field(FieldType.INTERSECTION, true, i, j, 90);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                    default -> {
                        imageView = new ImageView(grassImage);
                        var field = new Field(FieldType.NONE, false, i, j, 0);
                        fieldsMap.add(field);
                        fieldMatrix[i][j] = field;
                    }
                }
                imageView.setFitHeight(34);
                imageView.setFitWidth(34);
                imageView.setRotate(imageView.getRotate() + fieldsMap.get(counter).getFieldRotation());
                stackPane.setAlignment(Pos.CENTER);
                stackPane.getChildren().add(imageView);
                stackPanes.add(stackPane);
                gridPane.add(stackPane, j, i);
                counter++;
            }
        }
        Util.setMap(fieldMatrix);
        segments = Util.readSegments();
        for (Station station : stations){
            station.setStationSegments(segments.stream().filter(segment ->
            segment.getId().contains(station.getStationId())).collect(Collectors.toList()));
        }
    }

    private void moveTrain() {
        var imageView = new ImageView(locomotive.getLocomotiveImage());
        locomotive.start();
        Thread updatingThread = new Thread(){
            @Override
            public void run() {
                while (!locomotive.isFinished()) {
                    if(locomotive.isUpdated()) {
                        renderItem(locomotive.getCurrentField(), locomotive.getPreviousField(), imageView);
                        locomotive.setUpdated(false);
                    }
                    try {
                        Thread.sleep(1000/60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        updatingThread.start();
    }
//        new Thread(()->{
//            Image trainImage = null;
//            try {
//                trainImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + "images" + File.separator + "steamTrain.png"));
//            } catch (FileNotFoundException fileNotFoundException) {
//                fileNotFoundException.printStackTrace();
//            }
//            var imageView = new ImageView(trainImage);
//            var field = locomotives.get(0).getCurrentField();
//
//            ObservableList<Node> nodes = gridPane.getChildren();
//            for(Node node : nodes){
//                if(GridPane.getRowIndex(node) == field.getCoordinates().getRow() &&
//                        GridPane.getColumnIndex(node) == field.getCoordinates().getColumn()){
//                    var stackPane = (StackPane) node;
//                    stackPane.setAlignment(Pos.CENTER);
//                    imageView.setRotate(imageView.getRotate() + field.getFieldRotation());
//                    stackPane.getChildren().add(imageView);
//                    Platform.runLater(() -> {
//                        gridPane.getChildren().remove(node);
//                        gridPane.add(stackPane, field.getCoordinates().getColumn(), field.getCoordinates().getRow());
//                    });
//                }
//            }
//        }).start();


}
