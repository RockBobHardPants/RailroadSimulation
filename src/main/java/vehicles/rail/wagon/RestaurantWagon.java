package vehicles.rail.wagon;

public class RestaurantWagon extends Wagon{
    private String description;

    public RestaurantWagon(String description, String label, Double length) {
        super(label, length);
        this.description = description;
    }
}
