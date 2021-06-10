package map;

import vehicles.rail.composition.Composition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RailroadSegment {
    private final String id;
    private final Station firstStation;
    private final Station secondStation;
    private final List<Field> path;
    private final List<Composition> compositionsOnSegment;
    private final RailroadCrossing railroadCrossing;

    public RailroadSegment(String id, List<Field> path) {
        this.id = id;
        this.path = path;
        compositionsOnSegment = new ArrayList<>();
        firstStation = Map.getStationList().stream().filter(station -> station.getStationId().equals(id.split("-")[0])).findFirst().orElse(null);
        secondStation = Map.getStationList().stream().filter(station -> station.getStationId().equals(id.split("-")[1])).findFirst().orElse(null);
        List<Field> crossingFields = path.stream().filter(field -> field.getFieldType().equals(FieldType.INTERSECTION)).collect(Collectors.toList());
        var crossingField = crossingFields.stream().findFirst();
        if(crossingField.isPresent()){
            int roadCode = crossingField.get().getRoadCode();
            railroadCrossing = Map.getRailroadCrossingList().stream()
                    .filter(railroadCrossing1 -> railroadCrossing1.getId() == roadCode).findFirst().orElse(null);
        } else {
            railroadCrossing = null;
        }
    }

    public void updateCrossingState(){
        railroadCrossing.setSafeToCross(compositionsOnSegment.stream().allMatch(Composition::isPassedCrossing));
    }

    public List<Field> getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public List<Composition> getCompositionsOnSegment(){
        return compositionsOnSegment;
    }

    public void addCompositionOnSegment(Composition composition){
        compositionsOnSegment.add(composition);
        if(railroadCrossing != null){
            railroadCrossing.setSafeToCross(false);
        }
    }

    public void removeCompositionFromSegment(Composition composition){
        compositionsOnSegment.remove(composition);
        if(railroadCrossing != null && compositionsOnSegment.isEmpty()){
            railroadCrossing.setSafeToCross(true);
        }
    }

    public boolean hasComposition(){
        return !compositionsOnSegment.isEmpty();
    }

    public boolean hasComposition(Composition composition){
        return compositionsOnSegment.contains(composition);
    }

    @Override
    public String toString() {
        return "Segment" + id + " Crossing " + railroadCrossing;
    }

    public boolean checkIfFieldInSegment(Field field){
        return path.stream().anyMatch(field1 -> field1.equals(field));
    }

    public Station getFirstStation() {
        return firstStation;
    }

    public Station getSecondStation() {
        return secondStation;
    }
}
