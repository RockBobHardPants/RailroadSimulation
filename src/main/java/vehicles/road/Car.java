package vehicles.road;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import map.Field;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Random;

public class Car extends Vehicle{
    private final int numberOfDoors;
    private Image carImage;
    private ImageView carImageView;

    public Car(Field currentField, boolean direction){
        this.currentField = currentField;
        this.direction = direction;
        numberOfDoors = new Random().nextInt(4) + 2;
        roadSide = currentField.getRoadSide();
        roadCode = currentField.getRoadCode();
        setCarImage();
    }

    private void setCarImage() {
        try {
            int imageNumber = new Random().nextInt(5) + 1;                                                        // 0-4 + 1 jer su slike po brojevima 1-5
            carImage = new Image(new FileInputStream(Paths.get("images")
                    .toAbsolutePath() + File.separator + "car_" + imageNumber + ".png"));
            carImageView = new ImageView(carImage);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }


    @Override
    public ImageView getImageView() {
        return carImageView;
    }

    public int getNumberOfDoors() {
        return numberOfDoors;
    }
}
