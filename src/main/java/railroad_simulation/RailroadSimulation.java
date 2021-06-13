package railroad_simulation;

import railroad_simulation.map.Map;
import railroad_simulation.map.RoadSegment;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class RailroadSimulation {
    public static final Logger LOGGER = Logger.getLogger("config");
    public static final Logger CONFIG_LOGGER = Logger.getLogger(RailroadSimulation.class.getName());
    public static String LOGS_FOLDER;
    public static String IMAGES_FOLDER;
    public static String ROUTE_HISTORY_FOLDER;
    public static String TRAINS_FOLDER;
    public static String MAP_FOLDER;

    private static WatchService watchService;
    private static WatchKey watchKey;
    private static Thread configWatcherThread;

    private static final File configFile = Paths.get("application.config").toFile();
    private static Properties applicationProperties;

    private static FileInputStream fileInputStream;
    private static InputStreamReader inputStreamReader;

    public static void main(String[] args){
        applicationProperties = new Properties();
        try {
            fileInputStream = new FileInputStream(configFile);
            inputStreamReader = new InputStreamReader(fileInputStream);
            applicationProperties.load(inputStreamReader);
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        updatePaths();
        configWatcherThread = new Thread(configFileWatcherRunnable);
        try {
            LOGGER.addHandler(new FileHandler(LOGS_FOLDER + File.separator + RailroadSimulation.class.getName(), 1024*1024, 3, true));
            CONFIG_LOGGER.addHandler(new FileHandler(LOGS_FOLDER + File.separator + "config", 1024*1024, 3, true));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Map.initializeMap();
        Map.readSegments();
        Map.setSegmentsOnStations();
        Map.setRoads();
        updateRoads();
        configWatcherThread.start();
        UILoader.main(args);
    }

    private static void updatePaths(){
        LOGS_FOLDER = applicationProperties.getProperty("logs");
        IMAGES_FOLDER = applicationProperties.getProperty("images");
        ROUTE_HISTORY_FOLDER = applicationProperties.getProperty("history");
        TRAINS_FOLDER = applicationProperties.getProperty("trains");
        MAP_FOLDER = applicationProperties.getProperty("map");
    }

    private static void updateRoads(){
        var speed1 = Integer.parseInt(applicationProperties.getProperty("speed1"));
        var speed2 = Integer.parseInt(applicationProperties.getProperty("speed2"));
        var speed3 = Integer.parseInt(applicationProperties.getProperty("speed3"));
        var number1 = Integer.parseInt(applicationProperties.getProperty("number1"));
        var number2 = Integer.parseInt(applicationProperties.getProperty("number2"));
        var number3 = Integer.parseInt(applicationProperties.getProperty("number3"));
        Map.getRoadSegmentList().forEach(roadSegment -> {
            if(roadSegment.getId() == 1){
                roadSegment.setSpeedLimit(speed1);
                roadSegment.setMaxNumberOfVehicles(number1);
            } else if (roadSegment.getId() == 2){
                roadSegment.setSpeedLimit(speed2);
                roadSegment.setMaxNumberOfVehicles(number2);
            } else {
                roadSegment.setSpeedLimit(speed3);
                roadSegment.setMaxNumberOfVehicles(number3);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static final Runnable configFileWatcherRunnable = () -> {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            watchKey = Paths.get("").register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
        } catch (IOException ioException) {
            RailroadSimulation.LOGGER.log(Level.SEVERE, ioException.getMessage(), ioException);
        }
        var valid = true;
        while (valid) {
            try {
                watchKey = watchService.take();
            } catch (InterruptedException exception) {
                RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                Thread.currentThread().interrupt();
            }
            for(WatchEvent<?> event : watchKey.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                WatchEvent<Path> pathWatchEvent = (WatchEvent<Path>) event;
                if (kind.equals(ENTRY_MODIFY)) {
                    var file = pathWatchEvent.context();
                    if(file.endsWith("application.config")) {
                        try {
                            sleep(100);
                            applicationProperties.load(new InputStreamReader(new FileInputStream(configFile)));
                            updatePaths();
                            updateRoads();
                        } catch (InterruptedException | IOException exception) {
                            Thread.currentThread().interrupt();
                            RailroadSimulation.LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
                        }
                    }
                }
            }
            valid = watchKey.reset();
        }
    };
}
