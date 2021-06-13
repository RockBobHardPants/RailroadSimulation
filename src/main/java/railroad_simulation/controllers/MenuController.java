package railroad_simulation.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MenuController {
	public static final String ROUTE_HISTORY_WINDOW_FXML = "../../route_history_window.fxml";

	@FXML
	public Button resumeButton;
	@FXML
	public Button showHistoryButton;
	@FXML
	public Button exitButton;
	@FXML
	public Button restartSimulationButton;
	@FXML
	private AnchorPane menuAnchor;

	MainController mainController;

	public void setMainController(MainController mainController){
		this.mainController = mainController;
	}

	@FXML
	public void initialize(){
		resumeButton.setOnMouseClicked(mouseEvent -> resumeButton.getScene().getWindow().hide());
		exitButton.setOnMouseClicked(mouseEvent -> System.exit(0));
		showHistoryButton.setOnMouseClicked(mouseEvent -> {
			var routeHistoryURL = this.getClass().getResource(ROUTE_HISTORY_WINDOW_FXML);
			var loader = new FXMLLoader(routeHistoryURL);
			try {
				loader.load();
				Parent root = loader.getRoot();
				var routeHistoryScene = new Scene(root);
				var routeHistoryStage = new Stage();
				routeHistoryStage.setScene(routeHistoryScene);
				routeHistoryStage.initStyle(StageStyle.UNDECORATED);
				routeHistoryStage.setAlwaysOnTop(true);
				menuAnchor.getScene().getWindow().hide();
				routeHistoryStage.show();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		});
		restartSimulationButton.setOnMouseClicked(mouseEvent -> {
			mainController.restartSimulation();
		});
	}
}
