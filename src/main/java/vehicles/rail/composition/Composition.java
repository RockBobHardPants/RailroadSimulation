package vehicles.rail.composition;

import MovableInterface.Movable;
import map.Field;
import map.Station;
import vehicles.rail.locomotive.Locomotive;
import vehicles.rail.wagon.Wagon;

import java.util.List;

public class Composition implements Movable {
    private List<Locomotive> locomotives;
    private List<Wagon> wagons;
    private Station destinationStation;

    public Composition(List<Locomotive> locomotives, List<Wagon> wagons, Station destinationStation) {
        this.locomotives = locomotives;
        this.wagons = wagons;
        this.destinationStation = destinationStation;
    }

    @Override
    public void go(Field currentField) {

    }
}
