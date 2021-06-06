import map.Map;

public class RailroadSimulation {
    public static void main(String[] args){
        Map.initializeMap();
        Map.readSegments();
        UILoader.main(args);
    }
}
