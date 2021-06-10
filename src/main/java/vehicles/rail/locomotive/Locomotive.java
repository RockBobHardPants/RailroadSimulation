package vehicles.rail.locomotive;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import map.Field;
import map.FieldType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;


public class Locomotive {
    public static final String IMAGES = "images";
    private final LocomotiveDrive locomotiveDrive;
    private final LocomotiveType locomotiveType;
    private final Double power;
    private final String label;
    private Field currentField;
    private Field previousField;
    private Image locomotiveImage;
    private ImageView locomotiveImageView;
    private boolean inStation;

    public Locomotive(LocomotiveDrive locomotiveDrive, LocomotiveType locomotiveType, Double power, String label) {
        this.locomotiveDrive = locomotiveDrive;
        this.locomotiveType = locomotiveType;
        this.power = power;
        this.label = label;
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
        if(currentField != null) {
            setInStation(currentField.getFieldType().equals(FieldType.STATION));
        }
    }

    public Field getPreviousField() {
        return previousField;
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

    public ImageView getLocomotiveImageView() {
        return locomotiveImageView;
    }

    public void setPreviousField(Field previousField) {
        this.previousField = previousField;
    }

    public boolean isInStation() {
        return inStation;
    }

    public void setInStation(boolean inStation) {
        this.inStation = inStation;
    }
}
