package util;


import com.sun.source.tree.IfTree;
import controllers.MainController;
import exception.InvalidConfigurationException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static controllers.MainController.*;

public abstract class Util {
	public static final String NUMBER = "number";
	public static final String LABEL = "label";
	public static final String FRONT = "front";
	public static final String SPEED = "speed";
	public static final String ROUTE = "route";
	public static final String REAR = "rear";
	public static final String WAGON = "wagon";
	public static final String TYPE = "type";
	public static final String LENGTH = "length";
	public static final String PASSENGER_TYPE = "passengerType";
	public static final String LOAD = "load";
	public static final String DRIVE = "drive";
	public static final String POWER = "power";
	public static final String DESCRIPTION = "description";

	private Util(){}

	public static JSONObject getTrainJSON(Path path){
		JSONObject train = null;
		try {
			train = new JSONObject(Files.readString(path));
		} catch (IOException fileNotFoundException) {
			Logger.getLogger(Util.class.getName()).log(Level.SEVERE, fileNotFoundException.getMessage());
		}
		return train;
	}

	public static List<Station> buildStationList(String stationListString) throws InvalidConfigurationException {
		List<Station> stationList = new ArrayList<>();
		String[] stationLabels = stationListString.split("-");
		for(String stationLabel : stationLabels){
			Optional<Station> stationOptional = Map.getStationList().stream().filter(station -> station.getStationId().equals(stationLabel)).findFirst();
			if(stationOptional.isPresent()) {
				stationList.add(stationOptional.get());
			} else {
				throw new InvalidConfigurationException(INVALID_COMPOSITION);
			}
		}
		return stationList;
	}

	public static Locomotive buildLocomotive(JSONObject locomotiveObject) throws InvalidConfigurationException {
		if (!locomotiveObject.has(DRIVE) || !locomotiveObject.has(TYPE)) {
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		Locomotive locomotive;
		LocomotiveDrive drive;
		LocomotiveType type;
		var label = locomotiveObject.getString(LABEL);
		var power = locomotiveObject.getDouble(POWER);
		switch (locomotiveObject.get(DRIVE).toString()) {
			case "electric" -> drive = LocomotiveDrive.ELECTRIC;
			case "steam" -> drive = LocomotiveDrive.STEAM;
			case "diesel" -> drive = LocomotiveDrive.DIESEL;
			default -> drive = null;
		}
		switch (locomotiveObject.get(TYPE).toString()) {
			case "passenger" -> type = LocomotiveType.PASSENGER;
			case "freight" -> type = LocomotiveType.FREIGHT;
			case "maneuver" -> type = LocomotiveType.MANEUVER;
			case "universal" -> type = LocomotiveType.UNIVERSAL;
			default -> type = null;
		}
		if (label == null || power == 0 || drive == null || type == null) {
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		} else {
			locomotive = new Locomotive(drive, type, power, label);
		}
		return locomotive;
	}

	public static Wagon buildWagon(JSONObject wagonObject) throws InvalidConfigurationException {
		if(!wagonObject.has(LENGTH) || !wagonObject.has(LABEL) || !wagonObject.has(TYPE)){
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		Wagon wagon = null;
		var label = wagonObject.getString(LABEL);
		var length = wagonObject.getDouble(LENGTH);
		var type = wagonObject.getString(TYPE);
		switch (type) {
			case "freight" -> {
				if(!wagonObject.has(LOAD)){
					throw new InvalidConfigurationException(INVALID_COMPOSITION);
				}
				var loadCapacity = Double.parseDouble(wagonObject.getString(LOAD));
				wagon = new FreightWagon(loadCapacity, label, length);
			}
			case "passenger" -> {
				PassengerWagonType passengerWagonType;
				String description = null;
				var numberOfPersons = 0;
				switch (wagonObject.getString(PASSENGER_TYPE)) {
					case "seat" -> {
						if(!wagonObject.has(NUMBER)){
							throw new InvalidConfigurationException(INVALID_COMPOSITION);
						}
						passengerWagonType = PassengerWagonType.SEAT;
						numberOfPersons = wagonObject.getInt(NUMBER);
					}
					case "bed" -> {
						if(!wagonObject.has(NUMBER)){
							throw new InvalidConfigurationException(INVALID_COMPOSITION);
						}
						passengerWagonType = PassengerWagonType.BED;
						numberOfPersons = wagonObject.getInt(NUMBER);
					}
					case "dormitory" -> {
						if(!wagonObject.has(NUMBER)){
							throw new InvalidConfigurationException(INVALID_COMPOSITION);
						}
						passengerWagonType = PassengerWagonType.DORMITORY;
						numberOfPersons = wagonObject.getInt(NUMBER);
					}
					case "restaurant" -> {
						if(!wagonObject.has(DESCRIPTION)){
							throw new InvalidConfigurationException(INVALID_COMPOSITION);
						}
						passengerWagonType = PassengerWagonType.RESTAURANT;
						description = wagonObject.getString(DESCRIPTION);
					}
					default -> passengerWagonType = null;
				}
				if (passengerWagonType != null) {
					wagon = new PassengerWagon(passengerWagonType, numberOfPersons, description, label, length);
				}
			}
			case "special" -> {
				if(!wagonObject.has(DESCRIPTION)){
					throw new InvalidConfigurationException(INVALID_COMPOSITION);
				}
				wagon = new SpecialPurposeWagon(label, wagonObject.getString(DESCRIPTION), length);
			}
			default -> throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		return wagon;
	}

	public static List<Wagon> buildWagonList(JSONObject train) throws InvalidConfigurationException {
		List<Wagon> wagonList = new ArrayList<>();
		if(!train.has(WAGON)) {
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		var wagons = train.getJSONArray(WAGON);
		for(Object wagon : wagons){
			var wagon1 = Util.buildWagon((JSONObject) wagon);
			wagonList.add(wagon1);
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

	public static Composition getComposition(JSONObject train) throws InvalidConfigurationException {
		if(!train.has(LABEL) || !train.has(FRONT) || !train.has(SPEED) || !train.has(ROUTE) || !train.has(TYPE)){
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		var label = train.getString(LABEL);
		var frontLocomotive = buildLocomotive(train.getJSONObject(FRONT));
		Locomotive rearLocomotive = null;
		List<Wagon> wagonList = new ArrayList<>();
		List<Station> stationList = buildStationList(train.getString(ROUTE));
		var movementSpeed = train.getInt(SPEED);
		if (train.has(REAR)) {
			rearLocomotive = buildLocomotive(train.getJSONObject(REAR));
		}
		if (train.has(WAGON)) {
			wagonList = buildWagonList(train);
		}
		var composition = buildComposition(label, frontLocomotive, rearLocomotive, wagonList, stationList, movementSpeed);
		if(composition == null){
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		return composition;
	}

}
