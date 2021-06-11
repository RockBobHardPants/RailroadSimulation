package railroad_simulation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;

public class UILoader extends Application {

    public static final String MAIN_WINDOW_FXML = "../main_window.fxml";

    @Override
    public void start(Stage stage) {
        Parent root;
        try {
            var mainWindowURL= this.getClass().getResource(MAIN_WINDOW_FXML);
            assert mainWindowURL != null;
            root = FXMLLoader.load(mainWindowURL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ioException){
            RailroadSimulation.LOGGER.log(Level.SEVERE, ioException.getMessage(), ioException);
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}
