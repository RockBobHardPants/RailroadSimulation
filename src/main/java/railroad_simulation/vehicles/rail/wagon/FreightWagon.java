package railroad_simulation.vehicles.rail.wagon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import railroad_simulation.RailroadSimulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.logging.Level;

public class FreightWagon extends Wagon{
    private double loadCapacity;

    public FreightWagon(double loadCapacity, String label, Double length) {
        super(label, length);
        this.loadCapacity = loadCapacity;
        setWagonImage();
    }

    @Override
    protected void setWagonImage(){
        try {
            wagonImage = new Image(new FileInputStream(Paths.get(RailroadSimulation.IMAGES_FOLDER).toAbsolutePath() + File.separator + "freightWagon.png"));
            wagonImageView = new ImageView(wagonImage);
        } catch (FileNotFoundException fileNotFoundException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage(), fileNotFoundException);
        }
    }
}
