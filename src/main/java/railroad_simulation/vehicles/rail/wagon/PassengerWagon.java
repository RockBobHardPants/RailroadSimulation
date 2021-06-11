package railroad_simulation.vehicles.rail.wagon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import railroad_simulation.RailroadSimulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.logging.Level;

public class PassengerWagon extends Wagon{
    public static final String IMAGES = "images";
    private final PassengerWagonType passengerWagonType;
    private int numberOfPersons;
    private String description;

    public PassengerWagon(PassengerWagonType passengerWagonType, int numberOfPersons, String description, String label, Double length) {
        super(label, length);
        this.passengerWagonType = passengerWagonType;
        this.numberOfPersons = numberOfPersons;
        this.description = description;
        setWagonImage();
    }

    @Override
    protected void setWagonImage() {
        try {
            wagonImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "passengerWagon.png"));
            wagonImageView = new ImageView(wagonImage);
        } catch (FileNotFoundException fileNotFoundException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage(), fileNotFoundException);
        }
    }

    public PassengerWagonType getPassengerWagonType() {
        return passengerWagonType;
    }

    public int getNumberOfPersons() {
        return numberOfPersons;
    }
}
