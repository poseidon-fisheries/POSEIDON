package uk.ac.ox.oxfish.model.regs.policymakers.sensors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

import java.util.function.Function;

/**
 * reads column name, returns the average of its last X observations
 */
public class PastAverageSensor implements Sensor<FishState,Double> {


    private final UnchangingPastSensor delegate;


    public PastAverageSensor(String indicatorColumnName, int yearsToLookBack) {

        delegate = new UnchangingPastSensor(
                indicatorColumnName,
                1d,
                yearsToLookBack
        );

    }


    @Override
    public Double scan(FishState system) {
        final Double scan = delegate.scan(system);
        delegate.setTargetSet(Double.NaN);
        return scan;

    }

    public String getIndicatorColumnName() {
        return delegate.getIndicatorColumnName();
    }

    public Function<Double, Double> getIndicatorTransformer() {
        return delegate.getIndicatorTransformer();
    }

    public void setIndicatorTransformer(Function<Double, Double> indicatorTransformer) {
        delegate.setIndicatorTransformer(indicatorTransformer);
    }

    public double getIndicatorMultiplier() {
        return delegate.getIndicatorMultiplier();
    }

    public int getYearsToLookBack() {
        return delegate.getYearsToLookBack();
    }

}
