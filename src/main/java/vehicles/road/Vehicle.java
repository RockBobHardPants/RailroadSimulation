package vehicles.road;

import map.Field;
import map.Map;

import java.util.Date;

public abstract class Vehicle extends Thread{
    protected String manufacturer;
    protected String model;
    protected Date dateOfManufacture;
    protected double movementSpeed;
    protected Field currentField;
    protected Field previousField;
    protected boolean direction;
    protected boolean updated;
    protected boolean finished;

    public void move(){
        var tempField = currentField;
        currentField = Map.getNextFieldVehicle(currentField, previousField, direction);
        previousField = tempField;
        updated = true;
    }

    @Override
    public void run() {
        while (!finished){

        }
    }
}
