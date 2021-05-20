package map;

import java.util.List;

public class Segment {
    private final String id;
    private final List<Field> path;

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
}
