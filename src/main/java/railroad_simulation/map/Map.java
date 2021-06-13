package railroad_simulation.map;

import railroad_simulation.RailroadSimulation;
import railroad_simulation.exception.InvalidConfigurationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class Map {
    public static final String SEGMENTS_TXT = "segments.txt";
    public static final String RIGHT = "R";
    public static final String  LEFT = "L";
    public static final String MAP_TXT = "map.txt";
    public static final String INVALID_MAP_CONFIGURATION = "Invalid map configuration";
    private static Field[][] mapMatrix;
    private static List<Station> stationList;
    private static List<RailroadSegment> railroadSegmentList;
    private static List<RailroadCrossing> railroadCrossingList;
    private static List<RoadSegment> roadSegmentList;

    private Map(){}

    public static Field getNextFieldTrain(Field currentField, Field previousField){
        var row = currentField.getCoordinates().getRow();
        var column = currentField.getCoordinates().getColumn();
        try {
            var northField = mapMatrix[row + 1][column];
            var westField = mapMatrix[row][column + 1];
            var southField = mapMatrix[row - 1][column];
            var eastField = mapMatrix[row][column - 1];
            if ((northField.getFieldType().equals(FieldType.RAILROAD) ||
                    northField.getFieldType().equals(FieldType.INTERSECTION)) && !northField.equals(previousField)) {
                return northField;
            } else if ((westField.getFieldType().equals(FieldType.RAILROAD) ||
                    westField.getFieldType().equals(FieldType.INTERSECTION)) && !westField.equals(previousField)) {
                return westField;
            } else if ((southField.getFieldType().equals(FieldType.RAILROAD) ||
                    southField.getFieldType().equals(FieldType.INTERSECTION)) && !southField.equals(previousField)) {
                return southField;
            } else if ((eastField.getFieldType().equals(FieldType.RAILROAD) ||
                    eastField.getFieldType().equals(FieldType.INTERSECTION)) && !eastField.equals(previousField)) {
                return eastField;
            }
        } catch (IndexOutOfBoundsException exception){
            RailroadSimulation.LOGGER.log(Level.WARNING, exception.getMessage(), exception);
        }
        return currentField;
    }

    public static Field getNextFieldVehicle(Field currentField, Field previousField, String side){
        var row = currentField.getCoordinates().getRow();
        var column = currentField.getCoordinates().getColumn();
        Field northField = null;
        Field southField = null;
        Field westField = null;
        Field eastField = null;
        try {
            if(row > 0) {
                northField = mapMatrix[row - 1][column];
            }
            if (row < 29) {
                southField = mapMatrix[row + 1][column];
            }
            if(column > 0) {
                eastField = mapMatrix[row][column - 1];
            }
            if (column < 29) {
                westField = mapMatrix[row][column + 1];
            }
            if (northField != null && (northField.getFieldType().equals(FieldType.ROAD)
                    || northField.getFieldType().equals(FieldType.INTERSECTION)) && !northField.equals(previousField)
                    && northField.getRoadSide().equals(side)) {
                return northField;
            } else if (westField != null && (westField.getFieldType().equals(FieldType.ROAD)
                    || westField.getFieldType().equals(FieldType.INTERSECTION)) && !westField.equals(previousField)
                    && westField.getRoadSide().equals(side)) {
                return westField;
            } else if (southField != null && (southField.getFieldType().equals(FieldType.ROAD)
                    || southField.getFieldType().equals(FieldType.INTERSECTION)) && !southField.equals(previousField)
                    && southField.getRoadSide().equals(side)) {
                return southField;
            } else if (eastField != null && (eastField.getFieldType().equals(FieldType.ROAD)
                    || eastField.getFieldType().equals(FieldType.INTERSECTION)) && !eastField.equals(previousField)
                    && eastField.getRoadSide().equals(side)) {
                return eastField;
            }
        }catch (IndexOutOfBoundsException exception){
            RailroadSimulation.LOGGER.log(Level.WARNING, exception.getMessage(), exception);
        }
        return currentField;
    }

    public static void readSegments(){
        List<RailroadSegment> segmentList = new ArrayList<>();
        try (var bufferedReader = new BufferedReader(new FileReader(Paths.get(RailroadSimulation.MAP_FOLDER).toAbsolutePath()
                                                                    + File.separator + SEGMENTS_TXT))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splits = line.split(" # ");
                List<Field> fieldList = Arrays.stream(splits[1].split("],\\[")).map(s -> {
                    var column = Integer.parseInt(s.split(",")[0].replace("[", ""));
                    var row = Integer.parseInt(s.split(",")[1].replace("]", ""));
                    return mapMatrix[row][column];
                }).collect(Collectors.toList());
                segmentList.add(new RailroadSegment(splits[0], fieldList));
                Collections.reverse(fieldList);
                var stringBuilder = new StringBuilder(splits[0]);
                stringBuilder.reverse();
                segmentList.add(new RailroadSegment(stringBuilder.toString(), fieldList));
            }
        } catch (IOException fileNotFoundException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage(), fileNotFoundException);
            System.exit(-1);
        }
        Map.railroadSegmentList = segmentList;
    }

    public static void setSegmentsOnStations(){
        for(RailroadSegment segment : railroadSegmentList){
            segment.getFirstStation().addSegment(segment);
            segment.getSecondStation().addSegment(segment);
        }
    }

    private static List<String> readMap(){
        List<String> map = new ArrayList<>();
        String line;
        var mapFile = new File(Paths.get(RailroadSimulation.MAP_FOLDER).toAbsolutePath() + File.separator + MAP_TXT);
        try (var bufferedReader = new BufferedReader(new FileReader(mapFile))){
            while ((line = bufferedReader.readLine()) != null) {
                List<String> matches = Pattern.compile("[a-zA-Z0-9]{2}").matcher(line).results().map(MatchResult::group).collect(Collectors.toList());
                map.addAll(matches);
            }
        } catch (IOException exception){
            RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            System.exit(-1);
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
        try {
            for (var row = 0; row < 30; row++) {
                for (var column = 0; column < 30; column++) {
                    mapItem = map.get(counter);
                    switch (mapItem) {
                        case "1R" -> {
                            var field = new Field(FieldType.ROAD, 1, RIGHT, false, row, column, 0);
                            mapMatrix[row][column] = field;
                        }
                        case "1L" -> {
                            var field = new Field(FieldType.ROAD, 1, "L", false, row, column, 180);
                            mapMatrix[row][column] = field;
                        }
                        case "1r" -> {
                            var field = new Field(FieldType.ROAD, 1, RIGHT, false, row, column, 270);
                            mapMatrix[row][column] = field;
                        }
                        case "1l" -> {
                            var field = new Field(FieldType.ROAD, 1, "L", false, row, column, 90);
                            mapMatrix[row][column] = field;
                        }
                        case "2R" -> {
                            var field = new Field(FieldType.ROAD, 2, RIGHT, false, row, column, 0);
                            mapMatrix[row][column] = field;
                        }
                        case "2L" -> {
                            var field = new Field(FieldType.ROAD, 2, "L", false, row, column, 180);
                            mapMatrix[row][column] = field;
                        }
                        case "3R" -> {
                            var field = new Field(FieldType.ROAD, 3, RIGHT, false, row, column, 0);
                            mapMatrix[row][column] = field;
                        }
                        case "3L" -> {
                            var field = new Field(FieldType.ROAD, 3, "L", false, row, column, 180);
                            mapMatrix[row][column] = field;
                        }
                        case "3r" -> {
                            var field = new Field(FieldType.ROAD, 3, RIGHT, false, row, column, 90);
                            mapMatrix[row][column] = field;
                        }
                        case "3l" -> {
                            var field = new Field(FieldType.ROAD, 3, "L", false, row, column, 270);
                            mapMatrix[row][column] = field;
                        }
                        case "1T" -> {
                            var field = new Field(FieldType.ROAD, 1, "L", false, row, column, 135);
                            mapMatrix[row][column] = field;
                        }
                        case "1t" -> {
                            var field = new Field(FieldType.ROAD, 1, RIGHT, false, row, column, 135);
                            mapMatrix[row][column] = field;
                        }
                        case "3T" -> {
                            var field = new Field(FieldType.ROAD, 3, RIGHT, false, row, column, 45);
                            mapMatrix[row][column] = field;
                        }
                        case "3t" -> {
                            var field = new Field(FieldType.ROAD, 3, "L", false, row, column, 45);
                            mapMatrix[row][column] = field;
                        }
                        case "TH" -> {
                            var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 90);
                            mapMatrix[row][column] = field;
                        }
                        case "TV" -> {
                            var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 0);
                            mapMatrix[row][column] = field;
                        }
                        case "A1", "B1", "C1", "D1", "E1", "A2", "B2", "C2", "D2", "E2", "A3", "B3", "C3", "D3", "E3", "A4", "B4", "C4", "D4", "E4" -> {
                            addStation(mapItem, row, column);
                        }
                        case "T1" -> {
                            var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 45);
                            mapMatrix[row][column] = field;
                        }
                        case "T2" -> {
                            var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 135);
                            mapMatrix[row][column] = field;
                        }
                        case "T3" -> {
                            var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 225);
                            mapMatrix[row][column] = field;
                        }
                        case "T4" -> {
                            var field = new Field(FieldType.RAILROAD, 0, null, true, row, column, 315);
                            mapMatrix[row][column] = field;
                        }
                        case "1V" -> {
                            var field = new Field(FieldType.INTERSECTION, 1, RIGHT, true, row, column, 270);
                            mapMatrix[row][column] = field;

                        }
                        case "1v" -> {
                            var field = new Field(FieldType.INTERSECTION, 1, "L", true, row, column, 90);
                            mapMatrix[row][column] = field;
                        }
                        case "2H" -> {
                            var field = new Field(FieldType.INTERSECTION, 2, RIGHT, true, row, column, 0);
                            mapMatrix[row][column] = field;
                        }
                        case "2h" -> {
                            var field = new Field(FieldType.INTERSECTION, 2, "L", true, row, column, 180);
                            mapMatrix[row][column] = field;
                        }
                        case "3V" -> {
                            var field = new Field(FieldType.INTERSECTION, 3, RIGHT, true, row, column, 90);
                            mapMatrix[row][column] = field;
                        }
                        case "3v" -> {
                            var field = new Field(FieldType.INTERSECTION, 3, "L", true, row, column, 270);
                            mapMatrix[row][column] = field;
                        }
                        case "00" -> {
                            var field = new Field(FieldType.NONE, 0, null, false, row, column, 0);
                            mapMatrix[row][column] = field;
                        }
                        default -> throw new InvalidConfigurationException(INVALID_MAP_CONFIGURATION);
                    }
                    counter++;
                }
            }
        } catch (InvalidConfigurationException exception){
            RailroadSimulation.CONFIG_LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            System.exit(-1);
        }
        for (Station station : stationList){
            station.loadStationExits();
        }
        setRailroadCrossingList();
    }

    public static void setRoads(){
        List<Field> rightSide1 = new ArrayList<>();
        List<Field> leftSide1 = new ArrayList<>();
        List<Field> rightSide2 = new ArrayList<>();
        List<Field> leftSide2 = new ArrayList<>();
        List<Field> rightSide3 = new ArrayList<>();
        List<Field> leftSide3 = new ArrayList<>();
        List<Field> fieldList = Arrays.stream(mapMatrix).flatMap(Arrays::stream)
                .filter(field -> field.getFieldType()
                .equals(FieldType.ROAD) || field.getFieldType().equals(FieldType.INTERSECTION))
                .collect(Collectors.toList());
        fieldList.forEach(field -> {
            if(field.getRoadCode() == 1){
                if(field.getRoadSide().equals(Map.RIGHT)){
                    rightSide1.add(field);
                } else {
                    leftSide1.add(field);
                }
            } else if(field.getRoadCode() == 2){
                if(field.getRoadSide().equals(Map.RIGHT)){
                    rightSide2.add(field);
                } else {
                    leftSide2.add(field);
                }
            } else {
                if(field.getRoadSide().equals(Map.RIGHT)){
                    rightSide3.add(field);
                } else {
                    leftSide3.add(field);
                }
            }
        });
        roadSegmentList = new ArrayList<>();
        roadSegmentList.add(new RoadSegment(1, rightSide1, leftSide1));
        roadSegmentList.add(new RoadSegment(2, rightSide2, leftSide2));
        roadSegmentList.add(new RoadSegment(3, rightSide3, leftSide3));
    }

    public static void setRailroadCrossingList(){
        railroadCrossingList = new ArrayList<>();
        List<Field> fieldList = Arrays.stream(mapMatrix).flatMap(Arrays::stream)
                .filter(field -> field.getFieldType().equals(FieldType.INTERSECTION)).collect(Collectors.toList());
        var crossing1 = new RailroadCrossing(1);
        var crossing2 = new RailroadCrossing(2);
        var crossing3 = new RailroadCrossing(3);
        fieldList.forEach(field -> {
            switch (field.getRoadCode()){
                case 1 -> {
                    if(field.getRoadSide().equals(RIGHT)) {
                        crossing1.setRightSideField(field);
                    }
                    else {
                        crossing1.setLeftSideField(field);
                    }
                }
                case 2 -> {
                    if(field.getRoadSide().equals(RIGHT)) {
                        crossing2.setRightSideField(field);
                    }
                    else {
                        crossing2.setLeftSideField(field);
                    }
                }
                default -> {
                    if(field.getRoadSide().equals(RIGHT)) {
                        crossing3.setRightSideField(field);
                    }
                    else {
                        crossing3.setLeftSideField(field);
                    }
                }
            }
        });
        railroadCrossingList.add(crossing1);
        railroadCrossingList.add(crossing2);
        railroadCrossingList.add(crossing3);
    }

    public static Field[][] getMapMatrix(){
        return mapMatrix;
    }

    public static List<Station> getStationList() {
        return stationList;
    }

    public static List<RailroadSegment> getRailroadSegmentList() {
        return railroadSegmentList;
    }

    public static List<RoadSegment> getRoadSegmentList(){
        return roadSegmentList;
    }

    public static List<RailroadCrossing> getRailroadCrossingList(){
        return railroadCrossingList;
    }
}
