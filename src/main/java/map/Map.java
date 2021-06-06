package map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Map {
    private static Field[][] mapMatrix;
    private static List<Station> stationList;
    private static List<Segment> segmentList;
    private Map(){}

    public static Field getNextFieldTrain(Field currentField, Field previousField){
        int row = currentField.getCoordinates().getRow();
        int column = currentField.getCoordinates().getColumn();
        var northField = mapMatrix[row + 1][column];
        var westField = mapMatrix[row][column + 1];
        var southField = mapMatrix[row - 1][column];
        var eastField = mapMatrix[row][column - 1];
        if((northField.getFieldType().equals(FieldType.RAILROAD) ||
            northField.getFieldType().equals(FieldType.INTERSECTION)) && !northField.equals(previousField)){
            return northField;
        } else if ((westField.getFieldType().equals(FieldType.RAILROAD) ||
                westField.getFieldType().equals(FieldType.INTERSECTION)) && !westField.equals(previousField)){
            return westField;
        } else if ((southField.getFieldType().equals(FieldType.RAILROAD) ||
                southField.getFieldType().equals(FieldType.INTERSECTION)) && !southField.equals(previousField)){
            return southField;
        } else if ((eastField.getFieldType().equals(FieldType.RAILROAD) ||
                eastField.getFieldType().equals(FieldType.INTERSECTION)) && !eastField.equals(previousField)){
            return eastField;
        }
        return currentField;
    }

    public static Field getNextFieldVehicle(Field currentField, Field previousField, boolean direction){
        int row = currentField.getCoordinates().getRow();
        int column = currentField.getCoordinates().getColumn();
        var northField = mapMatrix[row + 1][column];
        var westField = mapMatrix[row][column + 1];
        var southField = mapMatrix[row - 1][column];
        var eastField = mapMatrix[row][column - 1];
        if(direction) {
            if ((northField.getFieldType().equals(FieldType.ROAD) ||
                    northField.getFieldType().equals(FieldType.INTERSECTION)) && !northField.equals(previousField)) {
                return northField;
            } else if ((westField.getFieldType().equals(FieldType.ROAD) ||
                    westField.getFieldType().equals(FieldType.INTERSECTION)) && !westField.equals(previousField)) {
                return westField;
            } else if ((southField.getFieldType().equals(FieldType.ROAD) ||
                    southField.getFieldType().equals(FieldType.INTERSECTION)) && !southField.equals(previousField)) {
                return southField;
            } else if ((eastField.getFieldType().equals(FieldType.ROAD) ||
                    eastField.getFieldType().equals(FieldType.INTERSECTION)) && !eastField.equals(previousField)) {
                return eastField;
            }
        }
        return currentField;
    }

    public static void readSegments(){
        List<Segment> segmentList = new ArrayList<>();
        try (var bufferedReader = new BufferedReader(new FileReader(Paths.get("").toAbsolutePath()
                                                                    + File.separator + "segments.txt"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splits = line.split(" # ");
                List<Field> fieldList = Arrays.stream(splits[1].split("],\\[")).map(s -> {
                    var column = Integer.parseInt(s.split(",")[0].replace("[", ""));
                    var row = Integer.parseInt(s.split(",")[1].replace("]", ""));
                    return mapMatrix[column][row];
                }).collect(Collectors.toList());
                segmentList.add(new Segment(splits[0], fieldList));
            }
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        Map.segmentList = segmentList;
    }

    private static List<String> readMap(){
        List<String> map = new ArrayList<>();
        String line;
        var mapFile = new File(Paths.get("").toAbsolutePath() + File.separator + "map.txt");
        try (var bufferedReader = new BufferedReader(new FileReader(mapFile))){
            while ((line = bufferedReader.readLine()) != null) {
                List<String> matches = Pattern.compile("[a-zA-Z0-9]{2}").matcher(line).results().map(MatchResult::group).collect(Collectors.toList());
                map.addAll(matches);
            }
        } catch (IOException exception){
            Logger.getLogger("global").log(Level.SEVERE, "IO Exception");
        }
        return map;
    }

    private static void addStation(String label, int row, int column){
        var fieldRotation = Integer.parseInt(label.replaceAll("^[A-Z]", ""));
        var field = new Field(FieldType.STATION,  0, null,true, row, column, -fieldRotation);
        mapMatrix[row][column] = field;
        var stationLabel = label.replaceAll("[0-9]", "");
        var existingStation = stationList.stream().filter(station1 -> station1.getStationId().equals(stationLabel)).findFirst();
        if(existingStation.isPresent()) {
            int index = stationList.indexOf(existingStation.get());
            stationList.get(index).addStationField(field);
        } else {
            var station = new Station(stationLabel);
            station.addStationField(field);
            stationList.add(station);
        }
    }

    public static void initializeMap(){
        mapMatrix = new Field[30][30];
        stationList = new ArrayList<>();
        List<String> map = readMap();
        String mapItem;
        var counter = 0;
        for (var row = 0; row < 30; row++){
            for (var column = 0; column < 30; column++){
                mapItem = map.get(counter);
                switch (mapItem){
                    case "1R" -> {
                        var field = new Field(FieldType.ROAD, 1, "R", false, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "1L" -> {
                        var field = new Field(FieldType.ROAD, 1, "L", false, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "1r" -> {
                        var field = new Field(FieldType.ROAD, 1, "R", false, row, column, 270);
                        mapMatrix[row][column] = field;
                    }
                    case "1l" -> {
                        var field = new Field(FieldType.ROAD,  1, "L",false, row, column, 90);
                        mapMatrix[row][column] = field;
                    }
                    case "2R" -> {
                        var field = new Field(FieldType.ROAD, 2, "R", false, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "2L" -> {
                        var field = new Field(FieldType.ROAD,  2, "L",false, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "3R" -> {
                        var field = new Field(FieldType.ROAD,  3, "R",false, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "3L" -> {
                        var field = new Field(FieldType.ROAD,  3, "L",false, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "3r" -> {
                        var field = new Field(FieldType.ROAD,  3, "R",false, row, column, 90);
                        mapMatrix[row][column] = field;
                    }
                    case "3l" -> {
                        var field = new Field(FieldType.ROAD,  3, "L",false, row, column, 270);
                        mapMatrix[row][column] = field;
                    }
                    case "1T" -> {
                        var field = new Field(FieldType.ROAD,  1, "L",false, row, column, 135);
                        mapMatrix[row][column] = field;
                    }
                    case "1t" -> {
                        var field = new Field(FieldType.ROAD,  1, "R",false, row, column, 135);
                        mapMatrix[row][column] = field;
                    }
                    case "3T" -> {
                        var field = new Field(FieldType.ROAD,  3, "R",false, row, column, 45);
                        mapMatrix[row][column] = field;
                    }
                    case "3t" -> {
                        var field = new Field(FieldType.ROAD,  3, "L",false, row, column, 45);
                        mapMatrix[row][column] = field;
                    }
                    case "TH" -> {
                        var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 90);
                        mapMatrix[row][column] = field;
                    }
                    case "TV" -> {
                        var field = new Field(FieldType.RAILROAD,  0, null,true, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "A1", "B1", "C1", "D1", "E1", "A2", "B2", "C2", "D2", "E2", "A3", "B3", "C3", "D3", "E3", "A4", "B4", "C4", "D4", "E4" -> {
                        addStation(mapItem, row , column);
                    }
                    case "T1" -> {
                        var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 45);
                        mapMatrix[row][column] = field;
                    }
                    case "T2" -> {
                        var field = new Field(FieldType.RAILROAD, 0, null,true, row, column, 135);
                        mapMatrix[row][column] = field;
                    }
                    case "T3" -> {
                        var field = new Field(FieldType.RAILROAD, 0, null,true, row, column, 225);
                        mapMatrix[row][column] = field;
                    }
                    case "T4" -> {
                        var field = new Field(FieldType.RAILROAD,  0, null,true, row, column, 315);
                        mapMatrix[row][column] = field;
                    }
                    case "IV" -> {
                        var field = new Field(FieldType.INTERSECTION, 0, null, true, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                    case "IH" -> {
                        var field = new Field(FieldType.INTERSECTION,  0, null,true, row, column, 90);
                        mapMatrix[row][column] = field;
                    }
                    default -> {
                        var field = new Field(FieldType.NONE,  0, null,false, row, column, 0);
                        mapMatrix[row][column] = field;
                    }
                }
                counter++;
            }
        }
        for (Station station : stationList){
            station.loadStationExits();
        }
    }

    public static Field[][] getMapMatrix(){
        return mapMatrix;
    }

    public static List<Station> getStationList() {
        return stationList;
    }

    public static List<Segment> getSegmentList() {
        return segmentList;
    }
}
