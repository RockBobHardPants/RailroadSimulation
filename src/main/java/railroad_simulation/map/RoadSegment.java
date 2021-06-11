package railroad_simulation.map;

import railroad_simulation.vehicles.road.Vehicle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RoadSegment {
	private final int id;
	private int maxNumberOfVehicles;
	private int speedLimit;
	private List<Vehicle> vehicleList;
	private final List<Field> rightSideRoad;
	private final List<Field> leftSideRoad;

	public RoadSegment(int id, List<Field> rightSideRoad, List<Field> leftSideRoad){
		this.id = id;
		speedLimit = 50;
		vehicleList = new ArrayList<>();
		if(id == 1){
			rightSideRoad.sort((field1, field2) -> Integer.compare(field2.getCoordinates().getRow(), field1.getCoordinates().getRow()));
			rightSideRoad.sort((field2, field1) -> Integer.compare(field1.getCoordinates().getColumn(), field2.getCoordinates().getColumn()));
			leftSideRoad.sort(Comparator.comparingInt(field -> field.getCoordinates().getColumn()));
			leftSideRoad.sort(Comparator.comparingInt(field -> field.getCoordinates().getRow()));
		} else if(id == 2){
			rightSideRoad.sort((field1, field2) -> Integer.compare(field2.getCoordinates().getRow(), field1.getCoordinates().getRow()));
			leftSideRoad.sort(Comparator.comparingInt(field -> field.getCoordinates().getRow()));
		} else {
			rightSideRoad.sort(Comparator.comparingInt(field -> field.getCoordinates().getColumn()));
			rightSideRoad.sort((field1, field2) -> Integer.compare(field2.getCoordinates().getRow(), field1.getCoordinates().getRow()));
			leftSideRoad.sort((field1, field2) -> Integer.compare(field2.getCoordinates().getColumn(), field1.getCoordinates().column()));
			leftSideRoad.sort(Comparator.comparing(field -> field.getCoordinates().getRow()));
		}
		this.rightSideRoad = rightSideRoad;
		this.leftSideRoad = leftSideRoad;
	}

	@Override
	public String toString() {
		return "RoadSegment{" +
				"id=" + id +
				", rightSideRoad=" + rightSideRoad +
				", leftSideRoad=" + leftSideRoad +
				'}';
	}

	public int getMaxNumberOfVehicles() {
		return maxNumberOfVehicles;
	}

	public void setMaxNumberOfVehicles(int maxNumberOfVehicles) {
		this.maxNumberOfVehicles = maxNumberOfVehicles;
	}

	public List<Vehicle> getVehicleList() {
		return vehicleList;
	}

	public boolean addVehicle(Vehicle vehicle){
		if(vehicleList.size() < maxNumberOfVehicles){
			vehicleList.add(vehicle);
			return true;
		} else {
			return false;
		}
	}

	public boolean removeVehicle(Vehicle vehicle){
		if(!vehicleList.isEmpty()){
			vehicleList.remove(vehicle);
			return true;
		} else {
			return false;
		}
	}

	public void setSpeedLimit(int speedLimit) {
		this.speedLimit = speedLimit;
	}

	public int getSpeedLimit() {
		return speedLimit;
	}

	public List<Field> getRightSideRoad() {
		return rightSideRoad;
	}

	public List<Field> getLeftSideRoad() {
		return leftSideRoad;
	}

	public Field getRightStartingField(){
		return rightSideRoad.get(0);
	}

	public Field getLeftStartingField(){
		return leftSideRoad.get(0);
	}

	public Field getRightEndingField(){
		return rightSideRoad.get(rightSideRoad.size() - 1);
	}

	public Field getLeftEndingField(){
		return leftSideRoad.get(leftSideRoad.size() - 1);
	}
}
