package vehicles.rail.wagon;

public class PassengerWagon extends Wagon{
    private WagonType wagonType;
    private int numberOfPersons;

    public PassengerWagon(WagonType wagonType, int numberOfPersons, String label, Double length) {
        super(label, length);
        this.wagonType = wagonType;
        this.numberOfPersons = numberOfPersons;
    }
}
