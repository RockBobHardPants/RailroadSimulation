package vehicles.road;

import map.Field;

public class Car extends Vehicle{
    private int numberOfDoors;

    public Car(Field currentField, boolean direction){
        this.currentField = currentField;
        this.direction = direction;

    }
}
