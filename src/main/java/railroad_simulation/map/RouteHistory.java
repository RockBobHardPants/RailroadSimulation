package railroad_simulation.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RouteHistory implements Serializable {
	private double timeInMilliseconds;
	private final String label;
	private final List<Station> stationList;
	private final List<Field> fieldList;

	public RouteHistory(String label){
		this.label = label;
		timeInMilliseconds = 0;
		stationList = new ArrayList<>();
		fieldList = new ArrayList<>();
	}

	public double getTimeInSeconds() {
		return timeInMilliseconds / 1000;
	}

	public void addTime(int timeInSeconds) {
		this.timeInMilliseconds += timeInSeconds;
	}

	public List<Station> getStationList() {
		return stationList;
	}

	public void addStations(Station station) {
		if(!stationList.contains(station)) {
			stationList.add(station);
		}
	}

	public List<Field> getFieldList() {
		return fieldList;
	}

	public void addField(Field field) {
		this.fieldList.add(field);
	}

	@Override
	public String toString() {
		return "RouteHistory: " + label +
				"\ntimeInSeconds=" + timeInMilliseconds / 1000 +
				",\n stations=" + stationList +
				",\n fieldList=" + fieldList;
	}

	public String getLabel() {
		return label;
	}
}
