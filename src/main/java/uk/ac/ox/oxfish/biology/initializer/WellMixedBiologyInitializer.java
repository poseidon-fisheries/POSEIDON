package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
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
            DoubleParameter ratioFirstToSecondSpecies, DoubleParameter steepness, double percentageLimitOnDailyMovement,
            double differentialPercentageToMove) {
        super(-1,-1,
                                 Integer.MAX_VALUE,
                                 Integer.MAX_VALUE,
                                 true,
                                 firstSpeciesCapacity,
                                 ratioFirstToSecondSpecies,
                                 steepness,
                                 percentageLimitOnDailyMovement,
                                 differentialPercentageToMove);
    }



}
