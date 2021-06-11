package railroad_simulation.util;


import org.json.JSONException;
import org.json.JSONObject;
import railroad_simulation.RailroadSimulation;
import railroad_simulation.exception.InvalidConfigurationException;
import railroad_simulation.map.Map;
import railroad_simulation.map.Station;
import railroad_simulation.vehicles.rail.composition.Composition;
import railroad_simulation.vehicles.rail.locomotive.Locomotive;
import railroad_simulation.vehicles.rail.locomotive.LocomotiveDrive;
import railroad_simulation.vehicles.rail.locomotive.LocomotiveType;
import railroad_simulation.vehicles.rail.wagon.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static railroad_simulation.controllers.MainController.INVALID_COMPOSITION;

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
			RailroadSimulation.LOGGER.log(Level.SEVERE, fileNotFoundException.getMessage());
			System.exit(-1);
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

	public static Locomotive buildLocomotive(JSONObject locomotiveObject) throws InvalidConfigurationException, JSONException{
		Locomotive locomotive;
		LocomotiveDrive drive;
		LocomotiveType type;
		var label = locomotiveObject.getString(LABEL);
		var power = locomotiveObject.getDouble(POWER);
		if(label.equals("") || power == 0){
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		switch (locomotiveObject.get(DRIVE).toString()) {
			case "electric" -> drive = LocomotiveDrive.ELECTRIC;
			case "steam" -> drive = LocomotiveDrive.STEAM;
			case "diesel" -> drive = LocomotiveDrive.DIESEL;
			default -> throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		switch (locomotiveObject.get(TYPE).toString()) {
			case "passenger" -> type = LocomotiveType.PASSENGER;
			case "freight" -> type = LocomotiveType.FREIGHT;
			case "maneuver" -> type = LocomotiveType.MANEUVER;
			case "universal" -> type = LocomotiveType.UNIVERSAL;
			default -> throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		locomotive = new Locomotive(drive, type, power, label);
		return locomotive;
	}

	public static Wagon buildWagon(JSONObject wagonObject) throws JSONException, InvalidConfigurationException{
		Wagon wagon;
		var label = wagonObject.getString(LABEL);
		var length = wagonObject.getDouble(LENGTH);
		var type = wagonObject.getString(TYPE);
		switch (type) {
			case "freight" -> {
				var loadCapacity = wagonObject.getDouble(LOAD);
				wagon = new FreightWagon(loadCapacity, label, length);
			}
			case "passenger" -> {
				PassengerWagonType passengerWagonType;
				String description = null;
				var numberOfPersons = 0;
				switch (wagonObject.getString(PASSENGER_TYPE)) {
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
						description = wagonObject.getString(DESCRIPTION);
					}
					default -> throw new InvalidConfigurationException(INVALID_COMPOSITION);
				}
				wagon = new PassengerWagon(passengerWagonType, numberOfPersons, description, label, length);
			}
			case "special" -> {
				if (!wagonObject.has(DESCRIPTION)) {
					throw new InvalidConfigurationException(INVALID_COMPOSITION);
				}
				wagon = new SpecialPurposeWagon(label, wagonObject.getString(DESCRIPTION), length);
			}
			default -> throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		return wagon;
	}

	public static List<Wagon> buildWagonList(JSONObject train) throws JSONException, InvalidConfigurationException {
		List<Wagon> wagonList = new ArrayList<>();
		var wagons = train.getJSONArray(WAGON);
		for(Object wagon : wagons){
			var wagon1 = Util.buildWagon((JSONObject) wagon);
			wagonList.add(wagon1);
		}
		if(wagonList.stream().allMatch(wagon -> wagon instanceof PassengerWagon
				|| wagon instanceof FreightWagon || wagon instanceof SpecialPurposeWagon)) {
			return wagonList;
		}
		else {
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
	}

	public static Composition buildComposition(String label, Locomotive front, Locomotive rear, List<Wagon> wagonList,
	                                           List<Station> stationList, int movementSpeed)
											   throws JSONException, InvalidConfigurationException{
		Composition composition = null;
		if(front != null && stationList != null && movementSpeed != 0) {
			if (front.getLocomotiveType() == LocomotiveType.PASSENGER) {
				if (rear == null || rear.getLocomotiveType() == LocomotiveType.PASSENGER
								 || rear.getLocomotiveType() == LocomotiveType.UNIVERSAL) {
					if(wagonList == null){
						composition = new Composition(label, front, rear, null, stationList, movementSpeed);
					} else if(wagonList.stream().allMatch(PassengerWagon.class::isInstance)) {
						composition = new Composition(label, front, rear, wagonList, stationList, movementSpeed);
					} else {
						throw new InvalidConfigurationException(INVALID_COMPOSITION);
					}
				}
			} else if(front.getLocomotiveType() == LocomotiveType.FREIGHT){
				if(rear == null || rear.getLocomotiveType() == LocomotiveType.FREIGHT
								|| rear.getLocomotiveType() == LocomotiveType.UNIVERSAL){
					if(wagonList == null){
						composition = new Composition(label, front, rear, null, stationList, movementSpeed);
					} else if(wagonList.stream().allMatch(FreightWagon.class::isInstance)){
						composition = new Composition(label, front, rear, wagonList, stationList, movementSpeed);
					} else {
						throw new InvalidConfigurationException(INVALID_COMPOSITION);
					}
				}
			} else if(front.getLocomotiveType() == LocomotiveType.MANEUVER){
				if(rear == null) {
					if (wagonList == null){
						composition = new Composition(label, front, null, null,
								stationList, movementSpeed);
					} else if(wagonList.stream().allMatch(SpecialPurposeWagon.class::isInstance)){
						composition = new Composition(label, front, null, wagonList,
								stationList, movementSpeed);
					} else {
						throw new InvalidConfigurationException(INVALID_COMPOSITION);
					}
				}
			}
			else {
				throw new InvalidConfigurationException(INVALID_COMPOSITION);
			}
		}
		return composition;
	}

	public static Composition getComposition(JSONObject train) throws JSONException, InvalidConfigurationException {
		var label = train.getString(LABEL);
		var frontLocomotive = buildLocomotive(train.getJSONObject(FRONT));
		Locomotive rearLocomotive = null;
		List<Wagon> wagonList = null;
		List<Station> stationList = buildStationList(train.getString(ROUTE));
		var movementSpeed = train.getInt(SPEED);
		if(train.has(REAR)) {
			rearLocomotive = buildLocomotive(train.getJSONObject(REAR));
		}
		if(train.has(WAGON)) {
			wagonList = buildWagonList(train);
		}
		var composition = buildComposition(label, frontLocomotive, rearLocomotive, wagonList, stationList, movementSpeed);
		if (composition == null) {
			throw new InvalidConfigurationException(INVALID_COMPOSITION);
		}
		return composition;
	}

}
