package vehicles.road;

import MovableInterface.Movable;
import map.Field;

import java.util.Date;

public class Vehicle implements Movable {
    private String manufacturer;
    private String model;
    private Date dateOfManufacture;
    private Double movementSpeed;

    @Override
    public void go(Field currentField) {

    }
}
