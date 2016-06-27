package uk.ac.ox.oxfish.fisher.selfanalysis.heatmap;

import java.util.Objects;

/**
 * An immutable object useful for regression. It is compared with respect to time
 * Created by carrknight on 6/27/16.
 */
public class GeographicalObservation implements Comparable<GeographicalObservation>
{

    private final double x;

    private final double y;

    private final double time;

    private final double value;

    public GeographicalObservation(double x, double y, double time, double value) {
        this.x = x;
        this.y = y;
        this.time = time;
        this.value = value;
    }


    /**
     * Getter for property 'x'.
     *
     * @return Value for property 'x'.
     */
    public double getX() {
        return x;
    }

    /**
     * Getter for property 'y'.
     *
     * @return Value for property 'y'.
     */
    public double getY() {
        return y;
    }

    /**
     * Getter for property 'time'.
     *
     * @return Value for property 'time'.
     */
    public double getTime() {
        return time;
    }

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     */
    public double getValue() {
        return value;
    }

    /**
     * Note: this class has a natural ordering that is
     * inconsistent with equals: the ordering is over time alone
     */
    @Override
    public int compareTo(GeographicalObservation o) {
        return Double.compare(this.getTime(),o.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeographicalObservation that = (GeographicalObservation) o;
        return Double.compare(that.x, x) == 0 &&
                Double.compare(that.y, y) == 0 &&
                Double.compare(that.time, time) == 0 &&
                Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, time, value);
    }
}
