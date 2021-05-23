package vehicles.rail.locomotive;

import MovableInterface.Movable;
import com.sun.tools.javac.Main;
import controllers.MainController;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import map.Field;
import map.Station;
import util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import static java.lang.Thread.sleep;

public class Locomotive extends Thread implements Movable {
    public static final String IMAGES = "images";
    private final LocomotiveDrive locomotiveDrive;
    private final LocomotiveType locomotiveType;
    private final Double power;
    private final String label;
    private final Station destinationStation;
    private Field currentField;
    private Field previousField;
    private Image locomotiveImage;
    private boolean isFinished;

    public Locomotive(LocomotiveDrive locomotiveDrive, LocomotiveType locomotiveType, Double power, String label, Station destinationStation) {
        this.locomotiveDrive = locomotiveDrive;
        this.locomotiveType = locomotiveType;
        this.power = power;
        this.label = label;
        this.destinationStation = destinationStation;
        isFinished = false;
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

    public void move(){
        var tempField = currentField;
        currentField = Util.getNextField(currentField, previousField);
        previousField = tempField;
    }

    @Override
    public void run() {
        while (!isFinished) {
            synchronized (this) {
                move();
                checkIsFinished();
                notifyAll();
                go(currentField, locomotiveImage);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkIsFinished() {
        if(destinationStation.getStationFields().stream().anyMatch(stationField -> stationField.equals(currentField))){
            isFinished = true;
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
}
