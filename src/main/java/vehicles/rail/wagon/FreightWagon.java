package vehicles.rail.wagon;

public class FreightWagon extends Wagon{
    private Double loadCapacity;

    public FreightWagon(Double loadCapacity, String label, Double length) {
        super(label, length);
        this.loadCapacity = loadCapacity;
    }

    @Override
    protected void setWagonImage(){

    }
}
