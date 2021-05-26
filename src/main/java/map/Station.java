package map;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Station{
    private final String stationId;
    private final List<Field> stationFields;
    private List<Segment> stationSegments;
    public Map<String, String> stationExitMap;
    private List<Field> stationExitFields;

    public Station(String stationId) {
        this.stationId = stationId;
        stationFields = new ArrayList<>(4);
        stationExitMap = new HashMap<>();
        loadStationExits();
    }

    private void loadStationExits(){
        var properties = new Properties();
        try {
            properties.load(new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("station_" + stationId.toLowerCase() + ".config"))));
            for(String propertyName : properties.stringPropertyNames()){
                stationExitMap.put(propertyName, properties.getProperty(propertyName));
                //TODO parsiraj koordinate u intove i pokupi iz Util.map
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

//    public Field getDepartureField(Station destinationStation){
//
//    }

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
}
