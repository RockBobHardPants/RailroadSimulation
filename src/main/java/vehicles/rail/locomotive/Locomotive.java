package vehicles.rail.locomotive;

import MovableInterface.Movable;
import map.Field;
import map.Station;

public class Locomotive implements Movable {
    private LocomotiveDrive locomotiveDrive;
    private LocomotiveType locomotiveType;
    private Double power;
    private String label;
    private Station destinationStation;
    private Field currentField;

    public Locomotive(LocomotiveDrive locomotiveDrive, LocomotiveType locomotiveType, Double power, String label, Station destinationStation) {
        this.locomotiveDrive = locomotiveDrive;
        this.locomotiveType = locomotiveType;
        this.power = power;
        this.label = label;
        this.destinationStation = destinationStation;
    }

    @Override
    public void go(Field currentField) {

    }

    public Field getCurrentField() {
        return currentField;
    }

    public void setCurrentField(Field currentField) {
        this.currentField = currentField;
    }
}
