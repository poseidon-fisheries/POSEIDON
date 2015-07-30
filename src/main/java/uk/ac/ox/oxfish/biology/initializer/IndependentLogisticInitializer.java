package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Arrays;

/**
 * Each tile below water is filled with an independent logistic-growth. They all have the same carrying capacity
 * but each starts at a random level of biomass
 * Created by carrknight on 6/22/15.
 */
public class IndependentLogisticInitializer implements BiologyInitializer {


    private final DoubleParameter carryingCapacity;

    private final DoubleParameter steepness;


    public IndependentLogisticInitializer(
            DoubleParameter carryingCapacity, DoubleParameter steepness) {
        this.carryingCapacity = carryingCapacity;
        this.steepness = steepness;
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
    public LocalBiology generate(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {

        if (seaTile.getAltitude() >= 0)
            return new EmptyLocalBiology();
        else
        {
            int species = biology.getSize();
            Double[] carryingCapacities = new Double[species];
            Double tileCarryingCapacity = carryingCapacity.apply(random);
            Arrays.fill(carryingCapacities, tileCarryingCapacity);
            Double[] currentCapacities = new Double[species];
            Double[] malthusians = new Double[species];
            for(int i=0; i<currentCapacities.length; i++)
            {
                currentCapacities[i] = random.nextDouble(true, true) * tileCarryingCapacity;
                malthusians[i] =  steepness.apply(random);
            }
            return new IndependentLogisticLocalBiology(currentCapacities,carryingCapacities,malthusians);
        }
    };

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
}
