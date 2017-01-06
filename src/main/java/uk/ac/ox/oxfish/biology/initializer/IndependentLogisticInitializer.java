package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Each tile below water is filled with an independent logistic-growth. They all have the same carrying capacity
 * but each starts at a random level of biomass
 * Created by carrknight on 6/22/15.
 */
public class IndependentLogisticInitializer extends AbstractBiologyInitializer {


    private final DoubleParameter carryingCapacity;

    private final DoubleParameter steepness;

    private final DoubleParameter minInitialCapacity;

    private final DoubleParameter maxInitialCapacity;


    public IndependentLogisticInitializer(
            DoubleParameter carryingCapacity, DoubleParameter steepness,
            DoubleParameter minInitialCapacity, DoubleParameter maxInitialCapacity) {
        this.carryingCapacity = carryingCapacity;
        this.steepness = steepness;
        this.minInitialCapacity = minInitialCapacity;
        this.maxInitialCapacity = maxInitialCapacity;
    }


    public IndependentLogisticInitializer(
            DoubleParameter carryingCapacity, DoubleParameter steepness) {
        this(carryingCapacity,steepness,new FixedDoubleParameter(0),new FixedDoubleParameter(1d));
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

        if (seaTile.getAltitude() >= 0)
            return new EmptyLocalBiology();
        else
        {
            int species = biology.getSize();
            double carryingCapacityLevel = carryingCapacity.apply(random);
            double steepness = this.steepness.apply(random);
            double minCapacity = minInitialCapacity.apply(random);
            double maxCapacity = maxInitialCapacity.apply(random);

            return new LogisticLocalBiology(carryingCapacityLevel,species,steepness,random,
                                            maxCapacity,minCapacity);
        }
    }

    /**
     * ignored
     *  @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random mersenne randomizer
     * @param model
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {


    }

    public DoubleParameter getSteepness() {
        return steepness;
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }


    /**
     * Singleton array with "Species 0" in it
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        return new String[]{"Species 0"};
    }


}
