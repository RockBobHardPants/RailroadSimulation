package vehicles.rail.wagon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import map.Field;
import map.FieldType;

public abstract class Wagon {
    protected String label;
    protected Double length;
    protected Field currentField;
    protected Field previousField;
    protected Image wagonImage;
    protected ImageView wagonImageView;
    protected boolean inStation;

    protected Wagon(String label, Double length) {
        this.label = label;
        this.length = length;
    }

    protected abstract void setWagonImage();

    public ImageView getWagonImageView() {
        return wagonImageView;
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

    public void setPreviousField(Field previousField){
        this.previousField = previousField;
    }

    public void setInStation(boolean inStation) {
        this.inStation = inStation;
    }

    public boolean isInStation() {
        return inStation;
    }

    @Override
    public String toString() {
        return "Wagon{" +
                "label='" + label + '\'' +
                '}';
    }
}
