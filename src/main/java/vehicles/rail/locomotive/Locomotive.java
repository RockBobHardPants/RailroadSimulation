package vehicles.rail.locomotive;

import MovableInterface.Movable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import map.Field;
import map.FieldType;
import map.Station;
import util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;


public class Locomotive extends Thread {
    public static final String IMAGES = "images";
    private final LocomotiveDrive locomotiveDrive;
    private final LocomotiveType locomotiveType;
    private final Double power;
    private final String label;
    private final Station destinationStation;
    private final Station departureStation;
    private Field currentField;
    private Field previousField;
    private Image locomotiveImage;
    private ImageView locomotiveImageView;
    private boolean finished;
    private boolean updated;

    public Locomotive(LocomotiveDrive locomotiveDrive, LocomotiveType locomotiveType, Double power, String label, Station destinationStation, Station departureStation) {
        this.locomotiveDrive = locomotiveDrive;
        this.locomotiveType = locomotiveType;
        this.power = power;
        this.label = label;
        this.destinationStation = destinationStation;
        this.departureStation = departureStation;
        finished = false;
        setLocomotiveImage();
    }

    private void setLocomotiveImage(){
        try {
            if (this.locomotiveDrive.equals(LocomotiveDrive.STEAM)) {
                locomotiveImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "steamTrain.png"));
            } else if (this.locomotiveDrive.equals(LocomotiveDrive.DIESEL)){
                locomotiveImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "dieselTrain.png"));
            } else{
                locomotiveImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "electricTrain.png"));
            }
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        this.locomotiveImageView = new ImageView(locomotiveImage);
    }

    public Field getCurrentField() {
        return currentField;
    }

    public void setCurrentField(Field currentField) {
        this.currentField = currentField;
    }

    public Field getPreviousField() {
        return previousField;
    }

    public void move(){
        var tempField = currentField;
        currentField = Util.getNextField(currentField, previousField);
        previousField = tempField;
        updated = true;
    }

    public boolean checkStationField(){
        return currentField.getFieldType().equals(FieldType.STATION);
    }

    @Override
    public void run() {
        while (!finished) {
            synchronized (this) {
                move();
                if(checkStationField()){
                    try {
                        wait(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
                checkIsFinished();
                try {
                    wait(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }
    }

    public void checkIsFinished() {
        if(destinationStation.getStationFields().stream().anyMatch(stationField -> stationField.equals(currentField))){
            finished = true;
        }
    }

    public Image getLocomotiveImage() {
        return locomotiveImage;
    }

    public LocomotiveType getLocomotiveType() {
        return locomotiveType;
    }

    public Double getPower() {
        return power;
    }

    public String getLabel() {
        return label;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public ImageView getLocomotiveImageView() {
        return locomotiveImageView;
    }
}
