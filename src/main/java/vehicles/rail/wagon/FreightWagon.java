package vehicles.rail.wagon;

public class FreightWagon extends Wagon{
    private double loadCapacity;

    public FreightWagon(double loadCapacity, String label, Double length) {
        super(label, length);
        this.loadCapacity = loadCapacity;
    }

    @Override
    protected void setWagonImage(){

    }
}
