package vehicles.rail.wagon;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class PassengerWagon extends Wagon{
    public static final String IMAGES = "images";
    private final PassengerWagonType passengerWagonType;
    private final int numberOfPersons;

    public PassengerWagon(PassengerWagonType passengerWagonType, int numberOfPersons, String label, Double length) {
        super(label, length);
        this.passengerWagonType = passengerWagonType;
        this.numberOfPersons = numberOfPersons;
        setWagonImage();
    }

    @Override
    protected void setWagonImage() {
        try {
            wagonImage = new Image(new FileInputStream(Paths.get("").toAbsolutePath() + File.separator + IMAGES + File.separator + "passengerWagon.png"));
            wagonImageView = new ImageView(wagonImage);
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
    }

    public PassengerWagonType getPassengerWagonType() {
        return passengerWagonType;
    }

    public int getNumberOfPersons() {
        return numberOfPersons;
    }
}
