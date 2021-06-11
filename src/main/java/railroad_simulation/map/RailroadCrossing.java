package railroad_simulation.map;

public class RailroadCrossing {
	private final int id;
	private boolean safeToCross;
	private Field rightSideField;
	private Field leftSideField;

	public RailroadCrossing(int id) {
		this.id = id;
		safeToCross = true;
	}

	public void setRightSideField(Field rightSideField) {
		this.rightSideField = rightSideField;
	}

	public void setLeftSideField(Field leftSideField) {
		this.leftSideField = leftSideField;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "RailroadCrossing " + id;
	}

	public boolean isSafeToCross() {
		return safeToCross;
	}

	public void setSafeToCross(boolean safeToCross) {
		rightSideField.setElectricity(!safeToCross);
		leftSideField.setElectricity(!safeToCross);
		this.safeToCross = safeToCross;
	}

	public boolean checkCrossingFields(Field field){
		return field.equals(rightSideField) || field.equals(leftSideField);
	}
}
