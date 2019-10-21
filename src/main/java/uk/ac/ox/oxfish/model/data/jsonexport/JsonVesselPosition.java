package uk.ac.ox.oxfish.model.data.jsonexport;

public class JsonVesselPosition {
    int id;
    double longitudeDegrees;
    double latitudeDegrees;

    public JsonVesselPosition(int id, double longitudeDegrees, double latitudeDegrees) {
        this.id = id;
        this.longitudeDegrees = longitudeDegrees;
        this.latitudeDegrees = latitudeDegrees;
    }

}
