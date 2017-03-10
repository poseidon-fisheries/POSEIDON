package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * Two species, they have the inverse preference (that is the second species uses sandy carrying capacity for rocky and viceversa)
 * Created by carrknight on 7/25/16.
 */
public class TwoSpeciesRockyLogisticInitializer extends RockyLogisticInitializer {


    private static final String[] NAMES = new String[]{
            "Red Species",
            "Blue Species"
    };

    public TwoSpeciesRockyLogisticInitializer(
            DoubleParameter rockyCarryingCapacity,
            DoubleParameter sandyCarryingCapacity,
            double percentageLimitOnDailyMovement,
            double differentialPercentageToMove,
            LogisticGrowerInitializer grower) {
        super(rockyCarryingCapacity, sandyCarryingCapacity, percentageLimitOnDailyMovement,
              differentialPercentageToMove, 2,grower);
    }


    /**
     * the carrying capacity is an average between the rocky and the sandy one depending on how rocky
     * the tile is
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {
        if(seaTile.getAltitude() >=0)
            return new EmptyLocalBiology();
        else
        {

            double carryingCapacityFirst =
                    (1-seaTile.getRockyPercentage()) *  getSandyCarryingCapacity().apply(random)  +
                            seaTile.getRockyPercentage() * getRockyCarryingCapacity().apply(random);

            double carryingCapacitySecond =
                    (1-seaTile.getRockyPercentage()) *  getRockyCarryingCapacity().apply(random)  +
                            seaTile.getRockyPercentage() * getSandyCarryingCapacity().apply(random);


            BiomassLocalBiology local = new BiomassLocalBiology(
                    new Double[]{
                            carryingCapacityFirst * random.nextDouble(),
                            carryingCapacitySecond * random.nextDouble()
                    },
                    new Double[]{
                            carryingCapacityFirst, carryingCapacitySecond
                    }
            );
            biologies.put(seaTile,local);
            return local;
        }
    }

    /**
     * Get a list of the species with their names. The size of this array determines the size of the model array
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        return NAMES;
    }
}
