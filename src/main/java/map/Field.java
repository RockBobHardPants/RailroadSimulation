package map;

import java.util.Objects;

public class Field {
    private final FieldType fieldType;
    private boolean hasElectricity;
    private final Coordinates coordinates;
    private final int fieldRotation;
    private String stationId;

    public Field(){
        fieldType = null;
        hasElectricity = false;
        coordinates = null;
        fieldRotation = 0;
    }

    public Field(FieldType fieldType, boolean hasElectricity, int row, int column, int fieldRotation){
        this.fieldType = fieldType;
        this.hasElectricity = hasElectricity;
        this.fieldRotation = fieldRotation;
        coordinates = new Coordinates(row, column);
    }

    public Field(int row, int column, String stationId){
        this.fieldType = FieldType.STATION;
        this.hasElectricity = true;
        this.fieldRotation = 0;
        coordinates = new Coordinates(row, column);
        this.stationId = stationId;
    }

    public record Coordinates(int row, int column) {
        public int getRow() {
            return row;
        }
        public int getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return "Coordinates:[" +
                    "row=" + row +
                    ", column=" + column +
                    ']';
        }
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public Coordinates getCoordinates() { return coordinates; }

    public boolean getHasElectricity() {
        return hasElectricity;
    }

    public void setHasElectricity(boolean hasElectricity){
        this.hasElectricity = hasElectricity;
    }

    public int getFieldRotation() {
        return fieldRotation;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return hasElectricity == field.hasElectricity && fieldType == field.fieldType && coordinates.equals(field.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldType, hasElectricity, coordinates);
    }
}
