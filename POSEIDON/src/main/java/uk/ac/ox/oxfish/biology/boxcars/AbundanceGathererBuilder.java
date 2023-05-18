package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class AbundanceGathererBuilder implements AlgorithmFactory<AbundanceGatherers> {

    private int observationDay = 365;

    public int getObservationDay() {
        return observationDay;
    }

    public void setObservationDay(int observationDay) {
        this.observationDay = observationDay;
    }

    @Override
    public AbundanceGatherers apply(FishState fishState) {
        return new AbundanceGatherers(observationDay);
    }
}
