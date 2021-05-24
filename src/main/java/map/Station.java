package map;

import java.util.ArrayList;
import java.util.List;

public class Station{
    private final String stationId;
    private final List<Field> stationFields;
    private List<Segment> stationSegments;

    public Station(String stationId) {
        this.stationId = stationId;
        stationFields = new ArrayList<>(4);
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
