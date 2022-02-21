package uk.ac.ox.oxfish.geography.fads;

import com.vividsolutions.jts.geom.Coordinate;

public class FadSetObservation{

    private final Coordinate locationInData;

    private final double[] biomassCaughtInData;

    private final int simulatedDay;

    public FadSetObservation(Coordinate locationInData, double[] biomassCaughtInData, int simulatedDay) {
        this.locationInData = locationInData;
        this.biomassCaughtInData = biomassCaughtInData;
        this.simulatedDay = simulatedDay;
    }

    public Coordinate getLocationInData() {
        return locationInData;
    }

    public double[] getBiomassCaughtInData() {
        return biomassCaughtInData;
    }

    public int getSimulatedDay() {
        return simulatedDay;
    }
}