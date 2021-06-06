package util;


import map.Map;
import map.Station;
import org.json.JSONObject;
import vehicles.rail.composition.Composition;
import vehicles.rail.locomotive.Locomotive;
import vehicles.rail.locomotive.LocomotiveDrive;
import vehicles.rail.locomotive.LocomotiveType;
import vehicles.rail.wagon.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Util {
	public static final String NUMBER = "number";
	public static final String LABEL = "label";

	private Util(){}

	public static JSONObject getTrainJSON(Path path){
		JSONObject train = null;
		try {
			train = new JSONObject(Files.readString(path));
		} catch (IOException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
		}
		return train;
	}

	public static List<Station> buildStationList(String stationListString){
		List<Station> stationList = new ArrayList<>();
		String[] stationLabels = stationListString.split("-");
		for(String stationLabel : stationLabels){
			Optional<Station> stationOptional = Map.getStationList().stream().filter(station -> station.getStationId().equals(stationLabel)).findFirst();
			stationOptional.ifPresent(stationList::add);
		}
		return stationList;
	}

	public static Locomotive buildLocomotive(JSONObject locomotiveObject){
		Locomotive locomotive;
		LocomotiveDrive drive;
		LocomotiveType type;
		var label = locomotiveObject.getString(LABEL);
		var power = locomotiveObject.getDouble("power");
		switch (locomotiveObject.get("drive").toString()){
			case "electric" -> drive = LocomotiveDrive.ELECTRIC;
			case "steam" -> drive = LocomotiveDrive.STEAM;
			case "diesel" -> drive = LocomotiveDrive.DIESEL;
			default -> drive = null;
		}
		switch (locomotiveObject.get("type").toString()){
			case "passenger" -> type = LocomotiveType.PASSENGER;
			case "freight" -> type = LocomotiveType.FREIGHT;
			case "maneuver" -> type = LocomotiveType.MANEUVER;
			case "universal" -> type = LocomotiveType.UNIVERSAL;
			default ->  type = null;
		}
		if(label == null || power == 0 || drive == null || type == null){
			locomotive = null;
		} else {
			locomotive = new Locomotive(drive, type, power, label);
		}
		return locomotive;
	}

	public static Wagon buildWagon(JSONObject wagonObject){
		Wagon wagon;
		var label = wagonObject.getString(LABEL);
		var length = wagonObject.getDouble("length");
		var type = wagonObject.getString("type");
		switch (type){
			case "freight" -> {
				var loadCapacity = Double.parseDouble(wagonObject.getString("load"));
				wagon = new FreightWagon(loadCapacity, label, length);
			}
			case "passenger" -> {
				PassengerWagonType passengerWagonType;
				String description = null;
				var numberOfPersons = 0;
				switch (wagonObject.getString("passengerType")) {
					case "seat" -> {
						passengerWagonType = PassengerWagonType.SEAT;
						numberOfPersons = wagonObject.getInt(NUMBER);
					}
					case "bed" -> {
						passengerWagonType = PassengerWagonType.BED;
						numberOfPersons = wagonObject.getInt(NUMBER);
					}
					case "dormitory" -> {
						passengerWagonType = PassengerWagonType.DORMITORY;
						numberOfPersons = wagonObject.getInt(NUMBER);
					}
					case "restaurant" -> {
						passengerWagonType = PassengerWagonType.RESTAURANT;
						description = wagonObject.getString("description");
					}
					default -> passengerWagonType = null;
				}
				if(passengerWagonType == null){
					wagon = null;
				} else {
					wagon = new PassengerWagon(passengerWagonType, numberOfPersons, description, label, length);
				}
			}
			case "special" -> {
				String description;
				description = wagonObject.getString("description");
				wagon = new SpecialPurposeWagon(label, description, length);
			}
			default -> wagon = null;
		}
		return wagon;
	}

	public static List<Wagon> buildWagonList(JSONObject train){
		List<Wagon> wagonList = new ArrayList<>();
		var wagons = train.getJSONArray("wagon");
		for(Object wagon : wagons){
			wagonList.add(Util.buildWagon((JSONObject) wagon));
		}
		return wagonList;
	}

	public static Composition buildComposition(String label, Locomotive front, Locomotive rear, List<Wagon> wagonList, List<Station> stationList, int movementSpeed){
		Composition composition = null;
		if(front != null && stationList != null && movementSpeed != 0) {
			if (front.getLocomotiveType() == LocomotiveType.PASSENGER) {
				if (rear == null || rear.getLocomotiveType() == LocomotiveType.PASSENGER || rear.getLocomotiveType() == LocomotiveType.UNIVERSAL) {
					if(wagonList == null){
						composition = new Composition(label, front, rear, null, stationList, movementSpeed);
					} else if(wagonList.stream().allMatch(PassengerWagon.class::isInstance)) {
						composition = new Composition(label, front, rear, wagonList, stationList, movementSpeed);
					}
				}
			} else if(front.getLocomotiveType() == LocomotiveType.FREIGHT){
				if(rear == null || rear.getLocomotiveType() == LocomotiveType.FREIGHT || rear.getLocomotiveType() == LocomotiveType.UNIVERSAL){
					if(wagonList == null){
						composition = new Composition(label, front, rear, null, stationList, movementSpeed);
					} else if(wagonList.stream().allMatch(FreightWagon.class::isInstance)){
						composition = new Composition(label, front, rear, wagonList, stationList, movementSpeed);
					}
				}
			} else if(front.getLocomotiveType() == LocomotiveType.MANEUVER){
				if(rear == null) {
					if (wagonList == null){
						composition = new Composition(label, front, null, null, stationList, movementSpeed);
					} else if(wagonList.stream().allMatch(SpecialPurposeWagon.class::isInstance)){
						composition = new Composition(label, front, null, wagonList, stationList, movementSpeed);
					}
				}
			}
		}
		return composition;
	}

	public static Composition getComposition(JSONObject train){
		return Util.buildComposition(train.getString(LABEL), Util.buildLocomotive(train.getJSONObject("front")),
				Util.buildLocomotive(train.getJSONObject("rear")), Util.buildWagonList(train),
				Util.buildStationList(train.getString("route")), train.getInt("speed"));
	}

}
