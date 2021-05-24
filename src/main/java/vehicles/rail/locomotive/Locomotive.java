package vehicles.rail.locomotive;

import MovableInterface.Movable;
import javafx.scene.image.Image;
import map.Field;
import map.FieldType;
import map.Station;
import util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;


public class Locomotive extends Thread implements Movable {
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
    private boolean finished;
    private boolean updated;

    public Locomotive(LocomotiveDrive locomotiveDrive, LocomotiveType locomotiveType, Double power, String label, Station destinationStation, Station departureStatoin) {
        this.locomotiveDrive = locomotiveDrive;
        this.locomotiveType = locomotiveType;
        this.power = power;
        this.label = label;
        this.destinationStation = destinationStation;
        this.departureStation = departureStatoin;
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
    }

    public Field getCurrentField() {
        return currentField;
    }

    public void setCurrentField(Field currentField) {
        this.currentField = currentField;
        previousField = null;
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
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                checkIsFinished();
                System.out.println(currentField.getCoordinates().getRow() + " , " + currentField.getCoordinates().getColumn());
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
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

    @Override
    public void go(Field currentField, Image image) {
        this.currentField = currentField;
        image = locomotiveImage;
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
}
