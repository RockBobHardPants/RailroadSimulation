package railroad_simulation.vehicles.rail.wagon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import railroad_simulation.RailroadSimulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.logging.Level;

public class SpecialPurposeWagon extends Wagon {
    private final String description;

    public SpecialPurposeWagon(String label, String description, Double length) {
        super(label, length);
        this.description = description;
        setWagonImage();
    }

    @Override
    protected void setWagonImage() {
        try {
            wagonImage = new Image(new FileInputStream(Paths.get(RailroadSimulation.IMAGES_FOLDER).toAbsolutePath() + File.separator + "specialPurposeWagon.png"));
            wagonImageView = new ImageView(wagonImage);
        } catch (FileNotFoundException fileNotFoundException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage(), fileNotFoundException);
        }
    }

    public String getDescription() {
        return description;
    }
}
