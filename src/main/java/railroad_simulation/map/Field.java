package railroad_simulation.map;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import railroad_simulation.RailroadSimulation;
import railroad_simulation.vehicles.road.Vehicle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public class Field implements Serializable {
    public static final String IMAGES = "images";
    public static final int TURN_PRE_ROTATION = -45;
    private transient Image fieldImage;
    private transient ImageView fieldImageView;
    private final FieldType fieldType;
    private boolean electricity;
    private final Coordinates coordinates;
    private final int fieldRotation;
    private int roadCode;
    private String roadSide;
    private transient Vehicle vehicleOnField;
    private final transient ObjectProperty<Coordinates> coordinatesProperty = new SimpleObjectProperty<>();

    public Field(FieldType fieldType,int roadCode, String roadSide, boolean hasElectricity, int row, int column, int fieldRotation){
        this.fieldType = fieldType;
        this.roadCode = roadCode;
        this.roadSide = roadSide;
        this.electricity = hasElectricity;
        this.fieldRotation = fieldRotation;
        this.vehicleOnField = null;
        coordinates = new Coordinates(row, column);
        this.coordinatesProperty.set(coordinates);
        setFieldImage();
    }
    //ImageView za skretanje je inicijalno rotiran za -45
    private void setFieldImage() {
        try {
            if (this.fieldType.equals(FieldType.STATION)) {
                switch (fieldRotation) {
                    case -1 -> fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                            + File.separator + IMAGES + File.separator + "stationTile1.png"));
                    case -2 -> fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                            + File.separator + IMAGES + File.separator + "stationTile2.png"));
                    case -3 -> fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                            + File.separator + IMAGES + File.separator + "stationTile3.png"));
                    case -4 -> fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                            + File.separator + IMAGES + File.separator + "stationTile4.png"));
                }
                fieldImageView = new ImageView(fieldImage);
                fieldImageView.setRotate(0);
            } else if (this.fieldType.equals(FieldType.ROAD)){
                switch (roadSide){
                    case "R" -> {
                        switch (fieldRotation){
                            case 0, 90 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadRight.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(fieldRotation);
                            }
                            case 270 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadRight.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(270);
                            }
                            case 45 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadTurn.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(TURN_PRE_ROTATION + fieldRotation);
                            }
                            case 135 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "road.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(TURN_PRE_ROTATION + fieldRotation);
                            }
                        }
                    }
                    case "L" -> {
                        switch (fieldRotation){
                            case 0 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadLeft.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(fieldRotation);
                            }
                            case 90 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadLeft.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(270);
                            }
                            case 180 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadLeft.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(0);
                            }
                            case 270 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadLeft.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(90);
                            }
                            case 135 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "roadTurn2.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(TURN_PRE_ROTATION + fieldRotation);
                            }
                            case 45 -> {
                                fieldImage = new Image(new FileInputStream(Paths.get("images").toAbsolutePath()
                                        + File.separator + "road.png"));
                                fieldImageView = new ImageView(fieldImage);
                                fieldImageView.setRotate(TURN_PRE_ROTATION + fieldRotation);
                            }
                        }
                    }
                }

            } else if (this.fieldType.equals(FieldType.RAILROAD)){
                switch (fieldRotation) {
                    case 45, 135, 225, 315 -> {
                        fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                                + File.separator + IMAGES + File.separator + "railwayLineTurn.png"));
                        fieldImageView = new ImageView(fieldImage);
                        fieldImageView.setRotate(TURN_PRE_ROTATION + fieldRotation);
                    }
                    default -> {
                        fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                                + File.separator + IMAGES + File.separator + "railwayLine.png"));
                        fieldImageView = new ImageView(fieldImage);
                        fieldImageView.setRotate(fieldRotation);
                    }
                }
            } else if (this.fieldType.equals(FieldType.INTERSECTION)){
                if(this.roadCode == 2) {
                        fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                                + File.separator + IMAGES + File.separator + "intersection.png"));
                        fieldImageView = new ImageView(fieldImage);
                        fieldImageView.setRotate(90);
                    } else {
                        fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                                + File.separator + IMAGES + File.separator + "intersection.png"));
                        fieldImageView = new ImageView(fieldImage);
                        fieldImageView.setRotate(0);
                    }
            } else {
                fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                        + File.separator + IMAGES + File.separator + "grass.png"));
                fieldImageView = new ImageView(fieldImage);
                fieldImageView.setRotate(new Random().nextInt(4) * 90);
            }
        } catch (FileNotFoundException fileNotFoundException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage(), fileNotFoundException);
        }
    }

    public int getRoadCode() {
        return roadCode;
    }

    public void setRoadCode(int roadCode) {
        this.roadCode = roadCode;
    }

    public String getRoadSide() {
        return roadSide;
    }

    public void setRoadSide(String roadSide) {
        this.roadSide = roadSide;
    }

    public Vehicle getVehicleOnField() {
        return vehicleOnField;
    }

    public void setVehicleOnField(Vehicle vehicleOnField) {
        this.vehicleOnField = vehicleOnField;
    }

    public void removeVehicleFromField(){
        this.vehicleOnField = null;
    }

    public class Coordinates implements Serializable{
        private final int row;
        private final int column;
        private transient IntegerProperty propertyRow;
        private transient IntegerProperty propertyColumn;

        public Coordinates(int row, int column){
            this.row = row;
            this.column = column;
            this.propertyRow  = new SimpleIntegerProperty();
            this.propertyColumn = new SimpleIntegerProperty();
            this.propertyRow.set(row);
            this.propertyColumn.set(column);
        }
        public int getRow() {
            return row;
        }
        public int getColumn() {
            return column;
        }

        public IntegerProperty getPropertyRow(){
            if (propertyRow == null) {
                propertyRow = new SimpleIntegerProperty();
                propertyRow.setValue(row);
            }
            return propertyRow;
        }
        public IntegerProperty getPropertyColumn(){
            if (propertyColumn == null) {
                propertyColumn = new SimpleIntegerProperty();
                propertyColumn.setValue(column);
            }
            return propertyColumn;
        }

        @Override
        public String toString() {
            return "Coordinates:[" +
                    "row=" + row +
                    ", column=" + column +
                    ']';
        }
    }

    public ObjectProperty<Coordinates> getCoordinatesProperty(){return coordinatesProperty;}

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

    public boolean getElectricity() {
        return electricity;
    }

    public void setElectricity(boolean electricity){
        this.electricity = electricity;
    }

    public int getFieldRotation() {
        return fieldRotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Field field = (Field) o;
        return electricity == field.electricity && fieldType == field.fieldType && coordinates.equals(field.coordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldType, electricity, coordinates);
    }

    @Override
    public String toString() {
        return "Type: " + fieldType + coordinates;
    }

    public ImageView getFieldImageView() {
        return fieldImageView;
    }
}
