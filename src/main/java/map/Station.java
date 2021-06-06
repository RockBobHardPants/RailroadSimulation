package map;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Station{
    private final String stationId;
    private final List<Field> stationFields;
    private List<Segment> stationSegments;
    private final java.util.Map<String, String> stationExitMap;
    private final List<Field> stationExitFields;

    public Station(String stationId) {
        this.stationId = stationId;
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.out.println(stationId + " = " + stationExitMap);
    }

    public Field trainDirection(Station destinationStation){
        String coordinates = stationExitMap.get(destinationStation.stationId);
        var column = Integer.parseInt(coordinates.split(",")[0]);
        var row = Integer.parseInt(coordinates.split(",")[1]);
        return Map.getMapMatrix()[row][column];
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
        return "Station{" +
                "stationId='" + stationId + '\'' +
                ", stationSegments=" + stationSegments;
    }

    public List<Segment> getStationSegments() {
        return stationSegments;
    }

    public void setStationSegments(List<Segment> stationSegments) {
        this.stationSegments = stationSegments;
    }

    public List<Field> getStationExitFields() {
        return stationExitFields;
    }

    public java.util.Map<String, String> getStationExitMap() {
        return stationExitMap;
    }
}
