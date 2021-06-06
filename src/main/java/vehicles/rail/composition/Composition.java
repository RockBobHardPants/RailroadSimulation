package vehicles.rail.composition;

import map.Field;
import map.FieldType;
import map.Station;
import map.Map;
import vehicles.rail.locomotive.Locomotive;
import vehicles.rail.wagon.Wagon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Composition extends Thread {
    private final String label;
    private final Locomotive frontLocomotive;
    private final Locomotive rearLocomotive;
    private final List<Wagon> wagonList;
    private final Station destinationStation;
    private final Station departureStation;
    private final List<Station> stationList;
    private Station currentStation;
    private Field currentField;
    private Field previousField;
    private List<Field> occupiedFields;
    private boolean departed;
    private boolean finished;
    private boolean updated;
    private boolean stationExit;
    private boolean wholeCompositionInStation;
    private final int movementSpeed;
    private boolean inStation;

    public Composition(String label, Locomotive frontLocomotive, Locomotive rearLocomotive, List<Wagon> wagonList,
                       List<Station> stationList, int movementSpeed) {
        this.label = label;
        this.frontLocomotive = frontLocomotive;
        this.rearLocomotive = rearLocomotive;
        this.wagonList = wagonList;
        this.destinationStation = stationList.get(stationList.size() - 1);
        this.currentField = stationList.get(0).getStationFields().get(0);
        this.departureStation = stationList.get(0);
        this.movementSpeed = movementSpeed;
        occupiedFields = new ArrayList<>();
        this.stationList = stationList;
        this.stationList.remove(0);
        this.stationList.remove(stationList.size() - 1);
        departed = false;
    }

    //TODO update polja nakon sto voz dodje do stanice neke
    private void setFieldsForItems(){
        Field tempField = null;
        frontLocomotive.setCurrentField(currentField);
        if(previousField != null){
            tempField = previousField;
            if(wagonList != null) {
                for (Wagon wagon : wagonList) {
                    wagon.setCurrentField(tempField);
                    tempField = wagon.getPreviousField();
                }
            }
        }
        if(rearLocomotive != null) {
            rearLocomotive.setCurrentField(tempField);
        }
    }

    public void move(){
        var tempField = currentField;
        if(departureStation.getStationFields().stream().noneMatch(field -> currentField.equals(field)) || currentField.getFieldType().equals(FieldType.STATION))
            currentField = Map.getNextFieldTrain(currentField, previousField);
        previousField = tempField;
        setFieldsForItems();
    }

    //TODO kada currentField dodje do stationExitField, prebaci ga na stationField[0] i nastavi updateovati ostatak kompozicije do ocupiedFields.size()
    public boolean checkStationField(){
        return currentField.getFieldType().equals(FieldType.STATION);
    }

    public boolean checkIsFinished() {
        if(destinationStation.getStationExitFields().stream().anyMatch(stationExitField -> currentField.equals(stationExitField))){
            currentStation = destinationStation;
            finished = true;
        }
        return false;
    }

    private boolean everythingInStation(){
        if(frontLocomotive.getCurrentField().getFieldType().equals(FieldType.STATION)){
            if()
        }
    }

    public void moveTrainIntoStation(){
        setCurrentField(currentStation.getStationFields().get(1));
        boolean everythingInStation = false;
        while(!everythingInStation){
            frontLocomotive.setCurrentField(currentField);

        }
    }

    private boolean checkIsStationExit(){
        Optional<Station> optionalStation = stationList.stream().filter(station ->
                                        station.getStationExitFields().stream().anyMatch(field -> currentField.equals(field))).findFirst();
        if(optionalStation.isPresent()){
            currentStation = optionalStation.get();
            return true;
        }
        return false;
    }


    private void updateOccupiedFields(){
        occupiedFields.clear();
        occupiedFields.add(frontLocomotive.getCurrentField());
        if (wagonList != null){
            wagonList.forEach(wagon -> occupiedFields.add(wagon.getCurrentField()));
        }
        if (rearLocomotive != null){
            occupiedFields.add(rearLocomotive.getCurrentField());
        }
    }

    private Station checkWhichStationIsIt(){
        for(Station station : stationList){
            if(station.getStationFields().stream().anyMatch(field -> currentField.equals(field)))
                return station;
        }
        return null;
    }

    private boolean checkIfDepartureStationField(){
        return departureStation.getStationFields().stream().anyMatch(field -> currentField.equals(field));
    }

    //TODO: ako je stationExit, postavi na true i pakuj voz komad po komad u stanicu, tek kad zavrsi stavi na false,
    // dok je true, nista drugo se ne izvrsava osim pakovanja u stanicu, a tek kada zavrsi sve radi wait(3000), nakon
    // cega pita kuda dalje, ako je moguce uopste.
    @Override
    public void run() {
        while (!finished) {
            synchronized (Map.getMapMatrix()[currentField.getCoordinates().getRow()][currentField.getCoordinates().getColumn()]) {
                if(!departed && checkIfDepartureStationField()){
                    setCurrentField(departureStation.trainDirection(destinationStation));
                    departed = true;
                }
                if(checkIsStationExit()){
                    setCurrentField(currentStation.getStationFields().get(0));
                    stationExit = true;
                }
                if(!stationExit  && checkStationField()){
                    setCurrentField(currentStation.trainDirection(destinationStation));
                    stationList.remove(currentStation);
                }
                if(!stationExit && checkIsFinished()){
                    break;
                }
                move();

                updated = true;
                try {
                    sleep(movementSpeed * 10L);
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

    public List<Field> getOccupiedFields() {
        return occupiedFields;
    }

    public boolean isWholeCompositionInStation() {
        return wholeCompositionInStation;
    }

    public String getLabel() {
        return label;
    }
}
