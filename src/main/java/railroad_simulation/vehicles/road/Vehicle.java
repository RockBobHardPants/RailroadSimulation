package railroad_simulation.vehicles.road;

import javafx.scene.image.ImageView;
import railroad_simulation.RailroadSimulation;
import railroad_simulation.map.Field;
import railroad_simulation.map.FieldType;
import railroad_simulation.map.Map;
import railroad_simulation.map.RoadSegment;

import java.util.Date;
import java.util.logging.Level;

public abstract class Vehicle extends Thread{
    protected String manufacturer;
    protected String model;
    protected Date dateOfManufacture;
    protected int movementSpeed;
    protected Field currentField;
    protected Field previousField;
    protected Field nextField;
    protected RoadSegment roadSegment;
    protected String roadSide;
    protected int roadCode;
    protected boolean updated;
    protected boolean finished;
    protected boolean waitAtCrossing;

    public void move(){
        waitAtCrossing = false;
        var tempField = currentField;
        nextField = Map.getNextFieldVehicle(currentField, previousField, roadSide);
        if(nextField.getFieldType().equals(FieldType.INTERSECTION)){
            var crossing = Map.getRailroadCrossingList().stream()
                    .filter(railroadCrossing -> railroadCrossing.getId() == roadCode).findFirst();
            if(crossing.isPresent() && crossing.get().isSafeToCross()){
                nextField.setVehicleOnField(this);
                currentField.removeVehicleFromField();
                currentField = nextField;
                previousField = tempField;
            } else {
                waitAtCrossing = true;
            }
        } else if(nextField.getVehicleOnField() == null){
            nextField.setVehicleOnField(this);
            currentField.removeVehicleFromField();
            currentField = nextField;
            previousField = tempField;
        }
    }

    private boolean checkIsFinished(){
        if(roadSide.equals(Map.RIGHT)){
            return roadSegment.getRightEndingField().equals(currentField);
        } else {
            return roadSegment.getLeftEndingField().equals(currentField);
        }
    }

    @Override
    public void run() {
        while (!finished){
            synchronized (Map.getMapMatrix()[currentField.getCoordinates().getRow()][currentField.getCoordinates().getColumn()]) {
                synchronized (Map.getMapMatrix()[nextField.getCoordinates().getRow()][nextField.getCoordinates().getColumn()]) {
                    try {
                        move();
                        if (waitAtCrossing) {
                            sleep(100);
                        } else {
                            sleep(10_000 / movementSpeed);
                        }
                        if(checkIsFinished()){
                            currentField.removeVehicleFromField();
                            finished = true;
                        }
                        updated = true;
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                    }
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
