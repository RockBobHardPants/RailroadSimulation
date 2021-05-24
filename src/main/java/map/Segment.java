package map;

import java.util.List;

public class Segment {
    private final String id;
    private final List<Field> path;
    private boolean vehicleOnSegment;

    public Segment(String id, List<Field> path) {
        this.id = id;
        this.path = path;
    }

    public List<Field> getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public boolean isVehicleOnSegment() {
        return vehicleOnSegment;
    }

    public void setVehicleOnSegment(boolean vehicleOnSegment) {
        this.vehicleOnSegment = vehicleOnSegment;
    }

    @Override
    public String toString() {
        return "Segment" + id;
    }
}
