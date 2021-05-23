package vehicles.rail.composition;

import MovableInterface.Movable;
import map.Field;
import map.Station;
import vehicles.rail.locomotive.Locomotive;
import vehicles.rail.wagon.Wagon;

import java.util.List;

public class Composition extends Thread {
    private final Locomotive frontLocomotive;
    private final Locomotive rearLocomotive;
    private final List<Wagon> wagons;
    private final Station destinationStation;

    public Composition(Locomotive frontLocomotive, Locomotive rearLocomotive, List<Wagon> wagons, Station destinationStation) {
        this.frontLocomotive = frontLocomotive;
        this.rearLocomotive = rearLocomotive;
        this.wagons = wagons;
        this.destinationStation = destinationStation;
    }

}
