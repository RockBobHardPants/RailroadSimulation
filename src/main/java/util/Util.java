package util;

import map.Field;
import map.FieldType;
import map.Segment;
import map.Station;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    private static Field[][] map;
    private Util(){}

    public static Field getNextField(Field currentField, Field previousField){
        int row = currentField.getCoordinates().getRow();
        int column = currentField.getCoordinates().getColumn();
        var northField = map[row + 1][column];
        var westField = map[row][column + 1];
        var southField = map[row - 1][column];
        var eastField = map[row][column - 1];
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

    public static List<Segment> readSegments(){
        List<Segment> segmentList = new ArrayList<>();
        try (var bufferedReader = new BufferedReader(new FileReader(Paths.get("").toAbsolutePath()
                                                                    + File.separator + "segments.txt"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splits = line.split(" # ");
                List<Field> fieldList = Arrays.stream(splits[1].split("],\\[")).map(s -> {
                    var column = Integer.parseInt(s.split(",")[0].replace("[", ""));
                    var row = Integer.parseInt(s.split(",")[1].replace("]", ""));
                    return map[column][row];
                }).collect(Collectors.toList());
                segmentList.add(new Segment(splits[0], fieldList));
            }
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        return segmentList;
    }



    public static void setMap(Field[][] map) {
        Util.map = map;
    }
}
