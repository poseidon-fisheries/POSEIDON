package uk.ac.ox.oxfish.biology.aggregation;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;

public interface FishAggregation {
    LocalBiology getAggregatedBiology();
    void aggregateFish(VariableBiomassBasedBiology seaTileBiology, GlobalBiology globalBiology);
}
