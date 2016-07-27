package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A diffusing logistic initializer, but one that has different maximum capacity according to the % of rocky terrain
 * in the map
 * Created by carrknight on 9/29/15.
 */
public class RockyLogisticInitializer extends AbstractBiologyInitializer
{



    private final DoubleParameter rockyCarryingCapacity;

    private final DoubleParameter sandyCarryingCapacity;


    private final DoubleParameter steepness;


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;

    /**
     * how many species are we having in this simulation?
     */
    private final int numberOfSpecies;

    public RockyLogisticInitializer(
            DoubleParameter rockyCarryingCapacity, DoubleParameter sandyCarryingCapacity,
            DoubleParameter steepness, double percentageLimitOnDailyMovement, double differentialPercentageToMove,
            int numberOfSpecies) {
        this.rockyCarryingCapacity = rockyCarryingCapacity;
        this.sandyCarryingCapacity = sandyCarryingCapacity;
        this.steepness = steepness;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.numberOfSpecies = numberOfSpecies;
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
            int species = biology.getSize();
            double carryingCapacityLevel =

                    (1-seaTile.getRockyPercentage()) *  sandyCarryingCapacity.apply(random)  +
                            seaTile.getRockyPercentage() * rockyCarryingCapacity.apply(random);

            double steepness = this.steepness.apply(random);

            return new LogisticLocalBiology(carryingCapacityLevel,species,steepness,random);
        }
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
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {
        BiomassDiffuser diffuser = new BiomassDiffuser(map,random,biology,
                                                       differentialPercentageToMove,
                                                       percentageLimitOnDailyMovement);
        model.scheduleEveryDay(diffuser, StepOrder.DAWN);
    }


    /**
     * Get a list of the species with their names. The size of this array determines the size of the model array
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        String[] generics = new String[numberOfSpecies];
        for(int i=0; i<numberOfSpecies; i++)
            generics[i] = "Species " + i;
        return generics;
    }

    public int getNumberOfSpecies() {
        return numberOfSpecies;
    }

    /**
     * Getter for property 'rockyCarryingCapacity'.
     *
     * @return Value for property 'rockyCarryingCapacity'.
     */
    public DoubleParameter getRockyCarryingCapacity() {
        return rockyCarryingCapacity;
    }

    /**
     * Getter for property 'sandyCarryingCapacity'.
     *
     * @return Value for property 'sandyCarryingCapacity'.
     */
    public DoubleParameter getSandyCarryingCapacity() {
        return sandyCarryingCapacity;
    }

    /**
     * Getter for property 'steepness'.
     *
     * @return Value for property 'steepness'.
     */
    public DoubleParameter getSteepness() {
        return steepness;
    }

    /**
     * Getter for property 'percentageLimitOnDailyMovement'.
     *
     * @return Value for property 'percentageLimitOnDailyMovement'.
     */
    public double getPercentageLimitOnDailyMovement() {
        return percentageLimitOnDailyMovement;
    }

    /**
     * Getter for property 'differentialPercentageToMove'.
     *
     * @return Value for property 'differentialPercentageToMove'.
     */
    public double getDifferentialPercentageToMove() {
        return differentialPercentageToMove;
    }
}
