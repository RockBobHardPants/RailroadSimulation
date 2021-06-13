package railroad_simulation.vehicles.rail.composition;

import railroad_simulation.RailroadSimulation;
import railroad_simulation.map.*;
import railroad_simulation.util.Util;
import railroad_simulation.vehicles.rail.locomotive.Locomotive;
import railroad_simulation.vehicles.rail.locomotive.LocomotiveDrive;
import railroad_simulation.vehicles.rail.wagon.Wagon;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class Composition extends Thread {
    private final String label;
    private final Locomotive frontLocomotive;
    private final Locomotive rearLocomotive;
    private final List<Wagon> wagonList;
    private final int length;
    private final Station destinationStation;
    private final Station departureStation;
    private final List<Station> stationList;
    private Station currentStation;
    private Field currentField;
    private Field previousField;
    private Field nextField;
    private Field lastCompositionItemField;
    private boolean departed;
    private boolean finished;
    private boolean updated;
    private boolean stationExit;
    private final int movementSpeed;
    private int temporaryMovementSpeed;
    private boolean readyToGo;
    private boolean everythingInStation;
    private boolean waiting;
    private boolean passedCrossing;
    private int waitingTime;
    private int crossingFieldsPassed = 0;
    private final RouteHistory routeHistory;
    boolean movedOnce;

    public Composition(String label, Locomotive frontLocomotive, Locomotive rearLocomotive, List<Wagon> wagonList,
                       List<Station> stationList, int movementSpeed) {
        this.label = label;
        this.frontLocomotive = frontLocomotive;
        this.rearLocomotive = rearLocomotive;
        this.wagonList = wagonList;
        this.destinationStation = stationList.get(stationList.size() - 1);
        this.currentField = stationList.get(0).getStationFields().get(0);
        this.departureStation = stationList.get(0);
        currentStation = departureStation;
        this.movementSpeed = movementSpeed;
        this.stationList = stationList;
        this.stationList.remove(0);
        this.stationList.remove(stationList.size() - 1);
        this.routeHistory = new RouteHistory(this.label);
        movedOnce = false;
        departed = false;
        waitingTime = 0;
        var tempLength = 1;
        if(rearLocomotive != null){
            tempLength++;
        }
        if(wagonList != null){
            tempLength = wagonList.size();
        }
        length = tempLength;
    }

    private void setFieldsForItems(){
        Field tempPreviousField = null;
        frontLocomotive.setCurrentField(currentField);
        if(previousField != null){
            frontLocomotive.setPreviousField(previousField);
            tempPreviousField = previousField;
            if(wagonList != null) {
                for (Wagon wagon : wagonList) {
                    wagon.setPreviousField(wagon.getCurrentField());
                    wagon.setCurrentField(tempPreviousField);
                    tempPreviousField = wagon.getPreviousField();
                }
            }
        }
        if(rearLocomotive != null) {
            if(wagonList == null) {
                tempPreviousField = rearLocomotive.getCurrentField();
                rearLocomotive.setCurrentField(previousField);
                rearLocomotive.setPreviousField(tempPreviousField);
            } else {
                tempPreviousField = rearLocomotive.getCurrentField();
                rearLocomotive.setCurrentField(wagonList.get(wagonList.size() - 1).getPreviousField());
                rearLocomotive.setPreviousField(tempPreviousField);
            }
            tempPreviousField = rearLocomotive.getPreviousField();
        }
        lastCompositionItemField = tempPreviousField;
        if(lastCompositionItemField != null){
            checkIfCrossingPassed();
        }
    }

    public void move(){
        var tempField = currentField;
        nextField = Map.getNextFieldTrain(currentField, previousField);
        if(frontLocomotive.getLocomotiveDrive().equals(LocomotiveDrive.ELECTRIC)){
            if(nextField != null && nextField.getElectricity()){
                if(lastCompositionItemField == null || lastCompositionItemField.getElectricity()){
                    currentField = nextField;
                    previousField = tempField;
                    setFieldsForItems();
                    routeHistory.addField(currentField);
                }
            }
        } else {
            currentField = nextField;
            previousField = tempField;
            setFieldsForItems();
            routeHistory.addField(currentField);
        }
    }


    private void checkIfCrossingPassed() {
        var crossing = Map.getRailroadCrossingList().stream()
                .filter(railroadCrossing -> railroadCrossing.checkCrossingFields(lastCompositionItemField)).findFirst();
        if(crossing.isPresent() && crossing.get().checkCrossingFields(lastCompositionItemField)){
            crossingFieldsPassed++;
        }
    }

    public boolean checkStationField(){
        return currentField.getFieldType().equals(FieldType.STATION);
    }

    private boolean everythingInStation(){
        var frontIn = false;
        var rearIn = false;
        var wagonsIn = false;
        if(frontLocomotive.getCurrentField().getFieldType().equals(FieldType.STATION)){
            frontIn = true;
        }
        if(rearLocomotive == null || rearLocomotive.getCurrentField().getFieldType().equals(FieldType.STATION)){
            rearIn = true;
        }
        if(wagonList == null || wagonList.stream().allMatch(wagon -> wagon.getCurrentField().getFieldType().equals(FieldType.STATION))){
            wagonsIn = true;
        }
        return frontIn && rearIn && wagonsIn;
    }

    public void moveTrainIntoStation(){
        if(!currentField.equals(currentStation.getStationFields().get(1))) {
            setCurrentField(currentStation.getStationFields().get(1));
            setPreviousField(currentStation.trainDirection(departureStation));
        } else {
            setPreviousField(currentStation.getStationFields().get(1));
        }
        setFieldsForItems();
    }

    private boolean checkIsStationExit(){
        Optional<Station> optionalStation = stationList.stream().filter(station ->
                                        station.getStationExitFields().stream().anyMatch(field -> currentField.equals(field))).findFirst();
        if(optionalStation.isPresent()){
            currentStation = optionalStation.get();
            return true;
        } else if (destinationStation.getStationExitFields().stream().anyMatch(field -> currentField.equals(field))){
            currentStation = destinationStation;
            return true;
        }
        return false;
    }

    private boolean checkIfDepartureStationField(){
        return departureStation.getStationFields().stream().anyMatch(field -> currentField.equals(field));
    }

    @Override
    public void run() {
        while (!finished) {
            try {
            if(!departed && checkIfDepartureStationField()){                                                            //Ako je na pocetnoj stanici i ako je trenutno polje tipa STATION
                if(departureStation.safeToGo(this)) {
                    routeHistory.addStations(departureStation);
                    if(waitingTime == 0) {
                        sleepAtStationExit(departureStation);
                    } else {
                        routeHistory.addTime(waitingTime);
                        sleep(waitingTime);
                        sleepAtStationExit(departureStation);
                        waitingTime = 0;
                    }
                    waiting = false;
                    departed = true;
                } else {
                    waiting = true;
                }
            }
            if(checkIsStationExit()){                                                                                   //Provjera da li se voz nalazi na ulaznom/izlaznom polju neke stanice
                stationExit = true;                                                                                     //Koja nije destination ili departure
            }
            if(stationExit){                                                                                            //Ako se nalazi na ulazu u stanicu
                moveTrainIntoStation();                                                                                 //Ubaci element voza koji nije u stanicu, u stanicu
                if(everythingInStation()){                                                                              //Provjeri da li je sve u stanici
                    if(currentStation.equals(destinationStation)){                                                      //Ako je trenutna stanica krajnja, zavrsi sve
                        destinationStation.removeCompositionFromSegment(this);
                        updated = true;
                        finished = true;
                    }
                    everythingInStation = true;
                    stationExit = false;
                }
            }
            if(!stationExit && checkStationField()){
                routeHistory.addStations(currentStation);
                if(everythingInStation && readyToGo) {
                    currentStation.removeCompositionFromSegment(this);
                    if(currentStation.equals(destinationStation)){
                        break;
                    }
                    if(currentStation.safeToGo(this)) {
                        if(waitingTime == 0) {
                            sleepAtStationExit(currentStation);
                        } else {
                            routeHistory.addTime(waitingTime);
                            sleep(waitingTime);
                            sleepAtStationExit(currentStation);
                            currentStation.safeToGo(this);
                            waitingTime = 0;
                        }
                        stationList.remove(currentStation);
                        readyToGo = false;
                        everythingInStation = false;
                        waiting = false;
                        passedCrossing = false;
                    } else {
                        waiting = true;
                    }
                }
            }
            if(!stationExit && !everythingInStation && departed) {
                move();
                if(crossingFieldsPassed == 2 && movedOnce){
                    passedCrossing = true;
                    crossingFieldsPassed = 0;
                    currentStation.notifyCrossing(this);
                    movedOnce = false;
                } else if (crossingFieldsPassed == 2){
                    movedOnce = true;
                }
            }
            if(!waiting) {
                updated = true;
            }
                if(waiting){
                    routeHistory.addTime(200);
                    sleep(200);
                }
                if(!stationExit && everythingInStation && !waiting){
                    routeHistory.addTime(3000);
                    sleep(3000);
                    readyToGo = true;
                } else {
                    if(temporaryMovementSpeed != 0){
                        routeHistory.addTime(10_000 / temporaryMovementSpeed);
                        sleep(10_000 / temporaryMovementSpeed);
                    } else {
                        routeHistory.addTime(10_000 / movementSpeed);
                        sleep(10_000 / movementSpeed);
                    }
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
            }
            updated = false;
        }
        Util.serializeRouteHistory(label, routeHistory);
    }

    private void sleepAtStationExit(Station currentStation) throws InterruptedException {
        setCurrentField(currentStation.trainDirection(destinationStation));
        routeHistory.addField(currentField);
        updated = true;
        if(temporaryMovementSpeed != 0){
            routeHistory.addTime(10_000 / temporaryMovementSpeed);
            sleep(10_000 / temporaryMovementSpeed);
        } else {
            routeHistory.addTime(10_000 / movementSpeed);
            sleep(10_000 / movementSpeed);
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
        if(temporaryMovementSpeed != 0){
            return temporaryMovementSpeed;
        }
        return movementSpeed;
    }

    public void setTemporaryMovementSpeed(int movementSpeed){
        this.temporaryMovementSpeed = movementSpeed;
    }

    public String getLabel() {
        return label;
    }

    public int getLength() {
        return length;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public boolean isPassedCrossing() {
        return passedCrossing;
    }

    public RouteHistory getRouteHistory() {
        return routeHistory;
    }

    @Override
    public String toString() {
        return "Composition{" +
                "label='" + label + '\'' +
                '}';
    }
}
