package railroad_simulation.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import railroad_simulation.map.Field;
import railroad_simulation.map.RouteHistory;
import railroad_simulation.map.Station;
import railroad_simulation.util.Util;
import railroad_simulation.vehicles.rail.composition.Composition;

import java.util.ArrayList;
import java.util.List;

public class RouteHistoryController {
	@FXML
	public TableView<Station> stationHistoryTable;
	@FXML
	public TableView<Field> fieldHistoryTable;
	@FXML
	public TableColumn<Field, Integer> rowCoordinateColumn;
	@FXML
	public TableColumn<Field, Integer> columnCoordinateColumn;
	public SplitPane splitPane;
	@FXML
	private Label compositionIdLabel;
	@FXML
	private Label timeInSecondsLabel;
	@FXML
	private TableView<RouteHistory> compositionTableView;
	@FXML
	private Button closeButton;

	private final ObservableList<RouteHistory> routeHistoryObservableList = FXCollections.observableList(new ArrayList<>());
	private final ObservableList<Field> fieldObservableList = FXCollections.observableList(new ArrayList<>());
	private final ObservableList<Station> stationObservableList = FXCollections.observableList(new ArrayList<>());
	private double xOffset = 0;
	private double yOffset = 0;

	@FXML
	private void initialize(){
		splitPane.setOnMousePressed(mouseEvent -> {
			xOffset = splitPane.getScene().getWindow().getX() - mouseEvent.getScreenX();
			yOffset = splitPane.getScene().getWindow().getY() - mouseEvent.getScreenY();
		});
		splitPane.setOnMouseDragged(mouseEvent -> {
			splitPane.getScene().getWindow().setX(mouseEvent.getScreenX() + xOffset);
			splitPane.getScene().getWindow().setY(mouseEvent.getScreenY() + yOffset);
		});
		routeHistoryObservableList.addAll(Util.loadRouteHistoryList());
		compositionTableView.setItems(routeHistoryObservableList);
		compositionTableView.setOnMouseClicked(mouseEvent -> showRouteHistory(compositionTableView.getSelectionModel().getSelectedItem()));
		closeButton.setOnMouseClicked(mouseEvent -> closeButton.getScene().getWindow().hide());
		fieldHistoryTable.setOnMouseClicked(mouseEvent -> fieldHistoryTable.getSelectionModel().clearSelection());
		stationHistoryTable.setOnMouseClicked(mouseEvent -> stationHistoryTable.getSelectionModel().clearSelection());
	}

	public void showRouteHistory(RouteHistory routeHistory){
		if(routeHistory != null) {
			compositionIdLabel.setText(routeHistory.getLabel());
			timeInSecondsLabel.setText(String.valueOf(routeHistory.getTimeInSeconds()));
			stationObservableList.clear();
			fieldObservableList.clear();
			rowCoordinateColumn.setCellValueFactory(cell -> cell.getValue().getCoordinates().getPropertyRow().asObject());
			columnCoordinateColumn.setCellValueFactory(cell -> cell.getValue().getCoordinates().getPropertyColumn().asObject());
			fieldObservableList.addAll(routeHistory.getFieldList());
			stationObservableList.addAll(routeHistory.getStationList());
			stationHistoryTable.setItems(stationObservableList);
			fieldHistoryTable.setItems(fieldObservableList);
		}
	}
}
