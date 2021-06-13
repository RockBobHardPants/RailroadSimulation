package railroad_simulation.map;

import railroad_simulation.RailroadSimulation;
import railroad_simulation.vehicles.rail.composition.Composition;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Station implements Serializable {
    private final String stationId;
    private final List<Field> stationFields;
    private transient List<RailroadSegment> stationSegments;
    private final java.util.Map<String, String> stationExitMap;
    private final List<Field> stationExitFields;

    public Station(String stationId) {
        this.stationId = stationId;
        stationSegments = new ArrayList<>();
        stationFields = new ArrayList<>(4);
        stationExitMap = new HashMap<>();
        stationExitFields = new ArrayList<>();
    }

    public void loadStationExits(){
        var properties = new Properties();
        Field field;
        int column;
        int row;
        String[] coordinates;
        try {
            properties.load(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader()
                      .getResourceAsStream("station_" + stationId.toLowerCase() + ".config"))));
            for(String propertyName : properties.stringPropertyNames()){
                stationExitMap.put(propertyName, properties.getProperty(propertyName));
                coordinates = properties.getProperty(propertyName).split(",");
                column = Integer.parseInt(coordinates[0]);
                row = Integer.parseInt(coordinates[1]);
                field = Map.getMapMatrix()[row][column];
                if(!stationExitFields.contains(field)) {
                    stationExitFields.add(field);
                }
            }
        } catch (Exception exception) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        }
    }

    public synchronized void notifyCrossing(Composition composition){
        var segment = stationSegments.stream()
                .filter(railroadSegment -> railroadSegment.hasComposition(composition)).findFirst();
        segment.ifPresent(RailroadSegment::updateCrossingState);
    }

    public void removeCompositionFromSegment(Composition composition){
        var entranceField = trainDirection(composition.getDepartureStation());
        var tempSegment = stationSegments.stream()
                .filter(segment -> segment.getSecondStation().equals(this))
                .collect(Collectors.toList())                                                                           //pronadji segmente kojima je ova stanica druga
                .stream().filter(segment -> segment.checkIfFieldInSegment(entranceField)).findFirst();                  //pronadji na osnovu ulaznog polja sa kojeg od tih segmenata je voz usao
        if(tempSegment.isPresent() && tempSegment.get().hasComposition(composition)){
            tempSegment.get().removeCompositionFromSegment(composition);                                                //obrisi taj voz iz liste vozova na pronadjenom segmentu
        }
    }

    public boolean safeToGo(Composition composition) {
        var exitField = trainDirection(composition.getDestinationStation());
        var oppositeSegment = stationSegments.stream()                                            //pronadji segmente kojima je ova stanica druga
                .filter(segment -> segment.getSecondStation().equals(this)).collect(Collectors.toList())                //pronadji na osnovu izlaznog polja koji od mogucih segmenata
                .stream().filter(segment -> segment.checkIfFieldInSegment(exitField)).findFirst();                      //jeste nasuprotan segment
        var nextSegment = stationSegments.stream()                                                //pronadji segmente kojima je ova stanica prva
                .filter(segment -> segment.getFirstStation().equals(this)).collect(Collectors.toList())                 //pronadji na osnovu izlaznog polja koji od mogucih segmenata
                .stream().filter(segment -> segment.checkIfFieldInSegment(exitField)).findFirst();                      //jeste sljedeci segment
        synchronized (this) {
            if (oppositeSegment.isPresent() && oppositeSegment.get().hasComposition()) {
                return false;
            } else if (nextSegment.isPresent() && !nextSegment.get().hasComposition()) {
                composition.setTemporaryMovementSpeed(0);
                nextSegment.get().addCompositionOnSegment(composition);
                return true;
            } else if (nextSegment.isPresent() && nextSegment.get().hasComposition()){
                int minSpeed = composition.getMovementSpeed();
                var waitTime = 0;
                for (Composition composition1 : nextSegment.get().getCompositionsOnSegment()){
                    minSpeed = Math.min(composition.getMovementSpeed(), composition1.getMovementSpeed());
                    waitTime = (composition1.getLength() + 3) * (10_000 / composition1.getMovementSpeed());
                }
                composition.setTemporaryMovementSpeed(minSpeed);
                composition.setWaitingTime(waitTime);
                nextSegment.get().addCompositionOnSegment(composition);
                return true;
            }
            return false;
        }
    }

    public Field trainDirection(Station destinationStation){
        String coordinates = stationExitMap.get(destinationStation.stationId);
        var column = Integer.parseInt(coordinates.split(",")[0]);
        var row = Integer.parseInt(coordinates.split(",")[1]);
        return Map.getMapMatrix()[row][column];
    }

    public void addSegment(RailroadSegment segment){
        stationSegments.add(segment);
    }

    public List<Field> getStationFields() {
        return stationFields;
    }

    public void addStationField(Field field){
        if(!stationFields.contains(field) && stationFields.size() <= 4) {
            this.stationFields.add(field);
        }
    }

    public String getStationId() {
        return stationId;
    }

    @Override
    public String toString() {
        return "Station: " + stationId;
    }

    public List<RailroadSegment> getStationSegments() {
        return stationSegments;
    }

    public void setStationSegments(List<RailroadSegment> stationSegments) {
        this.stationSegments = stationSegments;
    }

    public List<Field> getStationExitFields() {
        return stationExitFields;
    }

    public java.util.Map<String, String> getStationExitMap() {
        return stationExitMap;
    }
}
