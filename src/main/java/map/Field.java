package map;

import javafx.scene.image.Image;
import vehicles.rail.locomotive.LocomotiveDrive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Objects;

public class Field {
    public static final String IMAGES = "images";
    private Image fieldImage;
    private final FieldType fieldType;
    private boolean hasElectricity;
    private final Coordinates coordinates;
    private final int fieldRotation;
    private Field nextField;
    private Field previousField;


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
        setFieldImage();
    }

    private void setFieldImage() {
        try {
            if (this.fieldType.equals(FieldType.STATION)) {
                fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "stationTile1.png"));
            } else if (this.fieldType.equals(FieldType.ROAD)){
                fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "roadTile.png"));
            } else if (this.fieldType.equals(FieldType.RAILROAD)){
                fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "railVertical.png"));
            }
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    public Field getNextField() {
        return nextField;
    }

    public void setNextField(Field nextField) {
        this.nextField = nextField;
    }

    public Field getPreviousField() {
        return previousField;
    }

    public void setPreviousField(Field previousField) {
        this.previousField = previousField;
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

    public Field getFieldByCoordinates(int row, int column){
        if(coordinates.getRow() == row && coordinates.getColumn() == column){
            return this;
        }
        return null;
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
