package railroad_simulation.vehicles.rail.wagon;

public class SpecialPurposeWagon extends Wagon {
    private final String description;

    public SpecialPurposeWagon(String label, String description, Double length) {
        super(label, length);
        this.description = description;
    }

    @Override
    protected void setWagonImage() {

    }

    public String getDescription() {
        return description;
    }
}
