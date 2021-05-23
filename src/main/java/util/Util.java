package util;

import map.Field;
import map.FieldType;

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

    public static void setMap(Field[][] map) {
        Util.map = map;
    }
}
