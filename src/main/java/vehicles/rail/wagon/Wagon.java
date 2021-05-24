package vehicles.rail.wagon;

public abstract class Wagon {
    protected String label;
    protected Double length;

    public Wagon(String label, Double length) {
        this.label = label;
        this.length = length;
    }
}
