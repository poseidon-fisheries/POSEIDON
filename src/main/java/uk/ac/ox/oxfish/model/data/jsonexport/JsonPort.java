package uk.ac.ox.oxfish.model.data.jsonexport;

public class JsonPort {
    private final double longitudeDegrees;
    private final double latitudeDegrees;
    private final String name;

    public JsonPort(String name, double longitudeDegrees, double latitudeDegrees) {
        this.name = name;
        this.longitudeDegrees = longitudeDegrees;
        this.latitudeDegrees = latitudeDegrees;
    }


    /**
     * Getter for property 'longitudeDegrees'.
     *
     * @return Value for property 'longitudeDegrees'.
     */
    public double getLongitudeDegrees() {
        return longitudeDegrees;
    }

    /**
     * Getter for property 'latitudeDegrees'.
     *
     * @return Value for property 'latitudeDegrees'.
     */
    public double getLatitudeDegrees() {
        return latitudeDegrees;
    }

    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }
}
