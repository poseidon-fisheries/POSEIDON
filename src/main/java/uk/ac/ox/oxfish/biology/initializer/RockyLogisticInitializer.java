/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * A diffusing logistic initializer, but one that has different maximum capacity according to the % of rocky terrain
 * in the map
 * Created by carrknight on 9/29/15.
 */
public class RockyLogisticInitializer extends AbstractBiologyInitializer
{



    private final DoubleParameter rockyCarryingCapacity;

    private final DoubleParameter sandyCarryingCapacity;


    private final LogisticGrowerInitializer grower;


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

    /**
     * get the list of all the logistic local biologies
     */
    protected Map<SeaTile,BiomassLocalBiology> biologies = new HashMap<>();

    public RockyLogisticInitializer(
            DoubleParameter rockyCarryingCapacity, DoubleParameter sandyCarryingCapacity,
            double percentageLimitOnDailyMovement, double differentialPercentageToMove,
            int numberOfSpecies,
            LogisticGrowerInitializer grower) {
        this.rockyCarryingCapacity = rockyCarryingCapacity;
        this.sandyCarryingCapacity = sandyCarryingCapacity;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.numberOfSpecies = numberOfSpecies;
        this.grower = grower;
    }

    /**
     * the carrying capacity is an average between the rocky and the sandy one depending on how rocky
     * the tile is
     *  @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells, NauticalMap map) {
        if(seaTile.isLand())
            return new EmptyLocalBiology();
        else
        {
            int species = biology.getSize();
            double carryingCapacityLevel =

                    (1-seaTile.getRockyPercentage()) *  sandyCarryingCapacity.apply(random)  +
                            seaTile.getRockyPercentage() * rockyCarryingCapacity.apply(random);


            BiomassLocalBiology local = new BiomassLocalBiology(carryingCapacityLevel, species, random);
            biologies.put(seaTile,local);
            return local;
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





        BiomassDiffuserContainer diffuser = new BiomassDiffuserContainer(map, random, biology,
                                                                         differentialPercentageToMove,
                                                                         percentageLimitOnDailyMovement);
        model.scheduleEveryDay(diffuser, StepOrder.DAWN);

        for(Species species : biology.getSpecies())
            grower.initializeGrower(biologies,model,random,species);
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
