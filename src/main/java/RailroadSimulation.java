import map.Map;

public class RailroadSimulation {
    public static void main(String[] args){
        Map.initializeMap();
        Map.readSegments();
        Map.setSegmentsOnStations();
        Map.setRoads();
        UILoader.main(args);
    }
}
