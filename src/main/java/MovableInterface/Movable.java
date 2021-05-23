package MovableInterface;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import map.Field;

public interface Movable {
    void go(Field currentField, Image image);
}
