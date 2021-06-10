package vehicles.road;

import javafx.scene.image.ImageView;
import map.Field;
import map.FieldType;
import map.Map;
import map.RailroadCrossing;

import java.util.Date;

public abstract class Vehicle extends Thread{
    protected String manufacturer;
    protected String model;
    protected Date dateOfManufacture;
    protected double movementSpeed;
    protected Field currentField;
    protected Field previousField;
    protected Field nextField;
    protected String roadSide;
    protected int roadCode;
    protected boolean direction;                                                                                        //direction == true ako se krece od juga prema sjeveru
    protected boolean updated;
    protected boolean finished;
    protected boolean waitAtCrossing;

    public void move(){
        waitAtCrossing = false;
        var tempField = currentField;
        nextField = Map.getNextFieldVehicle(currentField, previousField, direction, roadSide);
        if(nextField.getFieldType().equals(FieldType.INTERSECTION)){
            var crossing = Map.getRailroadCrossingList().stream()
                    .filter(railroadCrossing -> railroadCrossing.getId() == roadCode).findFirst();
            if(crossing.isPresent() && crossing.get().isSafeToCross()){
                currentField = nextField;
                previousField = tempField;
            } else {
                waitAtCrossing = true;
            }
        } else {
            currentField = nextField;
            previousField = tempField;
        }
    }

    @Override
    public void run() {
        while (!finished){
            synchronized (Map.getMapMatrix()[currentField.getCoordinates().getRow()][currentField.getCoordinates().getColumn()]) {
                try {
                    move();
                    updated = true;
                    if(waitAtCrossing){
                        sleep(100);
                    } else {
                        sleep(400);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isUpdated(){
        return updated;
    }

    public Field getCurrentField() {
        return currentField;
    }

    public abstract ImageView getImageView();
}
