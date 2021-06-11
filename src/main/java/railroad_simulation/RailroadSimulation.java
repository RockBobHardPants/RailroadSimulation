package railroad_simulation;

import railroad_simulation.map.Map;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class RailroadSimulation {
    public static final Logger LOGGER = Logger.getLogger("config");
    public static final Logger CONFIG_LOGGER = Logger.getLogger(RailroadSimulation.class.getName());
    public static void main(String[] args){
        try {
            LOGGER.addHandler(new FileHandler("logs" + File.separator + RailroadSimulation.class.getName(), 1024*1024, 3, true));
            CONFIG_LOGGER.addHandler(new FileHandler("logs" + File.separator + "config", 1024*1024, 3, true));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Map.initializeMap();
        Map.readSegments();
        Map.setSegmentsOnStations();
        Map.setRoads();
        UILoader.main(args);
    }
}
