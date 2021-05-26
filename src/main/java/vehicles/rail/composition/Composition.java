package vehicles.rail.composition;

import map.Field;
import map.FieldType;
import map.Station;
import util.Util;
import vehicles.rail.locomotive.Locomotive;
import vehicles.rail.wagon.Wagon;

import java.util.List;

public class Composition extends Thread {
    private final Locomotive frontLocomotive;
    private final Locomotive rearLocomotive;
    private final List<Wagon> wagonList;
    private final Station destinationStation;
    private final Station departureStation;
    private Field currentField;
    private Field previousField;
    private boolean finished;
    private boolean updated;
    private final int movementSpeed;

    public Composition(Locomotive frontLocomotive, Locomotive rearLocomotive, List<Wagon> wagonList,
                       Station destinationStation, Field currentField, Station departureStation, int movementSpeed) {
        this.frontLocomotive = frontLocomotive;
        this.rearLocomotive = rearLocomotive;
        this.wagonList = wagonList;
        this.destinationStation = destinationStation;
        this.currentField = currentField;
        this.departureStation = departureStation;
        this.movementSpeed = movementSpeed;
    }

    private void setFieldsForItems(){
        Field tempField = null;
        frontLocomotive.setCurrentField(currentField);
        if(previousField != null){
            tempField = previousField;
            for(Wagon wagon : wagonList){
                wagon.setCurrentField(tempField);
                tempField = wagon.getPreviousField();
            }
        }
        if(rearLocomotive != null) {
            rearLocomotive.setCurrentField(tempField);
        }
    }

    public void move(){
        var tempField = currentField;
        currentField = Util.getNextField(currentField, previousField);
        previousField = tempField;
        updated = true;
        setFieldsForItems();
    }

    public boolean checkStationField(){
        return currentField.getFieldType().equals(FieldType.STATION);
    }

    public void checkIsFinished() {
        System.out.println();
        if(destinationStation.getStationExitFields().stream().anyMatch(stationExitField -> currentField.equals(stationExitField))){
            finished = true;
            System.out.println(isFinished());
        }
    }

    @Override
    public void run() {
        while (!finished) {
            synchronized (this) {
                move();
                if(checkStationField()){
                    try {
                        wait(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
                checkIsFinished();
                if(finished){
                    //TODO implementirati uslov za zaustavljanje na posljednjoj stanici
                    //TODO Prestati iscrtavati kompoziciju po zaustavljanju
                }
                try {
                    wait(movementSpeed * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        }
    }

    public Locomotive getFrontLocomotive() {
        return frontLocomotive;
    }

    public Locomotive getRearLocomotive() {
        return rearLocomotive;
    }

    public List<Wagon> getWagonList() {
        return wagonList;
    }

    public Station getDestinationStation() {
        return destinationStation;
    }

    public Field getCurrentField() {
        return currentField;
    }

    public void setCurrentField(Field currentField) {
        this.currentField = currentField;
        setPreviousField(null);
    }

    public Field getPreviousField() {
        return previousField;
    }

    public void setPreviousField(Field previousField) {
        this.previousField = previousField;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public Station getDepartureStation() {
        return departureStation;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }
}
