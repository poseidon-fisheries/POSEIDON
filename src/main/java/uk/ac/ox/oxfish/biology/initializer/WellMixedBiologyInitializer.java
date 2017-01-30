package uk.ac.ox.oxfish.biology.initializer;

import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A facade for a TwoSpeciesBoxInitializer where there is a single box covering everything and where
 * species live together
 * Created by carrknight on 10/8/15.
 */
public class WellMixedBiologyInitializer extends TwoSpeciesBoxInitializer
{

    public WellMixedBiologyInitializer(
            DoubleParameter firstSpeciesCapacity,
            DoubleParameter ratioFirstToSecondSpecies, double percentageLimitOnDailyMovement,
            double differentialPercentageToMove,
            LogisticGrowerInitializer grower) {
        super(-1,-1,
              Integer.MAX_VALUE,
              Integer.MAX_VALUE,
              true,
              firstSpeciesCapacity,
              ratioFirstToSecondSpecies,
              percentageLimitOnDailyMovement,
              differentialPercentageToMove,
              grower);
    }



}
