package vehicles.rail.wagon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import map.Field;

public abstract class Wagon {
    protected String label;
    protected Double length;
    protected Field currentField;
    protected Field previousField;
    protected Image wagonImage;
    protected ImageView wagonImageView;
    protected boolean moved;

    protected Wagon(String label, Double length) {
        this.label = label;
        this.length = length;
        moved = false;
    }

    protected abstract void setWagonImage();

    public ImageView getWagonImageView() {
        return wagonImageView;
    }

    public Field getCurrentField() {
        return currentField;
    }

    public void setCurrentField(Field currentField) {
        System.out.println(currentField);
        if(moved) {
            previousField = this.currentField;
        }
        this.currentField = currentField;
        moved = true;
    }

    public Field getPreviousField() {
        return previousField;
    }

    @Override
    public String toString() {
        return "Wagon{" +
                "label='" + label + '\'' +
                '}';
    }
}
