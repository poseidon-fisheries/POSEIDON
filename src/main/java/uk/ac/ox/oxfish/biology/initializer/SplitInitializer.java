package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A diffusing logistic initializer with 2 species: 1 lives on the top and one at the bottom of the map
 * Created by carrknight on 9/22/15.
 */
public class SplitInitializer extends AbstractBiologyInitializer {

    private final  DiffusingLogisticInitializer delegate;

    public SplitInitializer(DoubleParameter carryingCapacity, DoubleParameter steepness,
                                  double percentageLimitOnDailyMovement,
                                  double differentialPercentageToMove) {
        delegate = new DiffusingLogisticInitializer(carryingCapacity, steepness,
                                                    percentageLimitOnDailyMovement,
                                                    differentialPercentageToMove);
    }

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
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

        if(seaTile.getAltitude() < 0) {
            LogisticLocalBiology generated = (LogisticLocalBiology) delegate.generateLocal(biology,
                                                                                           seaTile,
                                                                                           random,
                                                                                           mapHeightInCells,
                                                                                           mapWidthInCells);

            //make the map split in half
            if (seaTile.getGridY() < mapHeightInCells / 2) {

                generated.setCarryingCapacity(biology.getSpecie(1), 0d);
            }
            else
                generated.setCarryingCapacity(biology.getSpecie(0), 0d);


            return generated;
        }
        else
            return delegate.generateLocal(biology,
                                          seaTile,
                                          random,
                                          mapHeightInCells,
                                          mapWidthInCells);

    }

    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {

        delegate.processMap(biology, map, random, model);


    }

    /**
     * "Species 0" and "Species 1"
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{"Species 0", "Species 1"};
    }
}
