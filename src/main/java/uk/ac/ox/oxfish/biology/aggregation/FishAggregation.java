package uk.ac.ox.oxfish.biology.aggregation;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.SeaTile;

public interface FishAggregation {
    LocalBiology getAggregatedBiology();
    void aggregateFish(SeaTile seaTile, GlobalBiology globalBiology);
}
