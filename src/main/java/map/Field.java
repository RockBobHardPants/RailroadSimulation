package map;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;

public class Field {
    public static final String IMAGES = "images";
    public static final int TURN_PRE_ROTATION = -45;
    private Image fieldImage;
    private ImageView fieldImageView;
    private final FieldType fieldType;
    private boolean hasElectricity;
    private final Coordinates coordinates;
    private final int fieldRotation;
    private int roadCode;
    private String roadSide;

    public Field(FieldType fieldType,int roadCode, String roadSide, boolean hasElectricity, int row, int column, int fieldRotation){
        this.fieldType = fieldType;
        this.roadCode = roadCode;
        this.roadSide = roadSide;
        this.hasElectricity = hasElectricity;
        this.fieldRotation = fieldRotation;
        coordinates = new Coordinates(row, column);
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
                fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                        + File.separator + IMAGES + File.separator + "intersection.png"));
                fieldImageView = new ImageView(fieldImage);
                fieldImageView.setRotate(fieldRotation);
            } else {
                fieldImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath()
                        + File.separator + IMAGES + File.separator + "grass.png"));
                fieldImageView = new ImageView(fieldImage);
                fieldImageView.setRotate(new Random().nextInt(4) * 90);
            }
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
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

    @Override
    public String toString() {
        return "Field " + coordinates;
    }

    public ImageView getFieldImageView() {
        return fieldImageView;
    }
}
