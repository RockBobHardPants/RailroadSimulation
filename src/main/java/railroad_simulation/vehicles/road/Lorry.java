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

public class Lorry extends Vehicle {
    private int loadCapacity;
    private Image lorryImage;
    private ImageView lorryImageView;

    public Lorry(RoadSegment roadSegment){
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
        loadCapacity = new Random().nextInt(10_000 - 1000) + 1000;
        movementSpeed = new Random().nextInt(roadSegment.getSpeedLimit() - 20) + 20;
        roadSide = currentField.getRoadSide();
        roadCode = currentField.getRoadCode();
        setLorryImage();
    }

    private void setLorryImage() {
        try {
            var imageNumber = new Random().nextInt(3);                                                            // nasumicna slika
            lorryImage = new Image(new FileInputStream(Paths.get(RailroadSimulation.IMAGES_FOLDER)
                    .toAbsolutePath() + File.separator + "lorry_" + imageNumber + ".png"));
            lorryImageView = new ImageView(lorryImage);
        } catch (FileNotFoundException fileNotFoundException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage(), fileNotFoundException);
        }
    }

    @Override
    public ImageView getImageView() {
        return lorryImageView;
    }

    public int getLoadCapacity() {
        return loadCapacity;
    }
}
