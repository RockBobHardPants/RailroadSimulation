package railroad_simulation.vehicles.road;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import railroad_simulation.RailroadSimulation;
import railroad_simulation.map.Map;
import railroad_simulation.map.RoadSegment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.logging.Level;

public class Car extends Vehicle{
    private final int numberOfDoors;
    private Image carImage;
    private ImageView carImageView;

    public Car(RoadSegment roadSegment){
        this.roadSegment = roadSegment;
        if(new Random().nextInt(2) == 0) {
            this.currentField = roadSegment.getRightStartingField();
            this.nextField = roadSegment.getRightSideRoad().get(1);
            roadSide = Map.RIGHT;
        } else {
            this.currentField = roadSegment.getLeftStartingField();
            this.nextField = roadSegment.getLeftSideRoad().get(1);
            roadSide = Map.LEFT;
        }
        numberOfDoors = new Random().nextInt(4) + 2;
        movementSpeed = new Random().nextInt(roadSegment.getSpeedLimit() - 20) + 20;
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
            RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage(), fileNotFoundException);
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
