import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UILoader extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root;
        try {
            root = FXMLLoader.load(this.getClass().getResource("main_window.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ioException){
//            Logger.getLogger("global").log(Level.SEVERE, ioException.getMessage());
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}
