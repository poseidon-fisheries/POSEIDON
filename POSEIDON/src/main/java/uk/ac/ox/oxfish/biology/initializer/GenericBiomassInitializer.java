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
import uk.ac.ox.oxfish.biology.initializer.allocator.AllocatorManager;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tries using as many separate objects as possible to keep biomass initialization easy;
 * Created by carrknight on 6/30/17.
 */
public class GenericBiomassInitializer extends AbstractBiologyInitializer {

    private final List<DoubleParameter> carryingCapacity;


    private final DoubleParameter minInitialCapacity;

    private final DoubleParameter maxInitialCapacity;


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;

    private final Map<SeaTile, BiomassLocalBiology> biologies =
        new HashMap<>();

    private final LogisticGrowerInitializer grower;

    /**
     * list of allocators (its size determines the number of species)
     */
    private final List<BiomassAllocator> allocators;

    private AllocatorManager manager;


    public GenericBiomassInitializer(
        final List<DoubleParameter> carryingCapacity,
        final DoubleParameter minInitialCapacity,
        final DoubleParameter maxInitialCapacity, final double percentageLimitOnDailyMovement,
        final double differentialPercentageToMove,
        final LogisticGrowerInitializer grower,
        final List<BiomassAllocator> allocators
    ) {
        this.carryingCapacity = carryingCapacity;
        this.minInitialCapacity = minInitialCapacity;
        this.maxInitialCapacity = maxInitialCapacity;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.grower = grower;
        this.allocators = allocators;
    }

    /**
     * Get a list of the species with their names. The size of this array determines the size of the model array
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        final String[] names = new String[allocators.size()];
        for (int i = 0; i < allocators.size(); i++)
            names[i] = "Species " + i;
        return names;
    }


    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
        final GlobalBiology biology, final SeaTile seaTile,
        final MersenneTwisterFast random, final int mapHeightInCells,
        final int mapWidthInCells, final NauticalMap map
    ) {

        //if there is no manager, we need to create and start it now
        if (manager == null) {
            //assume order of allocators is the same as order of species
            final Map<Species, BiomassAllocator> allocatorMap = new LinkedHashMap<>();
            for (final Species species : biology.getSpecies())
                allocatorMap.put(species, allocators.get(species.getIndex()));

            manager = new AllocatorManager(
                false,
                allocatorMap,
                biology
            );
            manager.start(map, random);
        }

        //create carrying capcities and put them in
        final double[] carringCapacities = new double[biology.getSize()];
        final double[] currentCapacity = new double[biology.getSize()];
        for (final Species species : biology.getSpecies()) {
            final double k = carryingCapacity.get(species.getIndex()).applyAsDouble(random) *
                manager.getWeight(species, seaTile, map, random);
            carringCapacities[species.getIndex()] = k;

            final double min = minInitialCapacity.applyAsDouble(random);
            final double max = maxInitialCapacity.applyAsDouble(random);
            currentCapacity[species.getIndex()] =
                ((max - min) * random.nextDouble(true, true) + min)
                    * k;
        }
        final BiomassLocalBiology local = new BiomassLocalBiology(currentCapacity, carringCapacities);
        biologies.put(seaTile, local);

        return local;

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
        final GlobalBiology biology, final NauticalMap map, final MersenneTwisterFast random, final FishState model
    ) {

        for (final Species species : biology.getSpecies())
            grower.initializeGrower(biologies, model, random, species);


        final BiomassDiffuserContainer diffuser = new BiomassDiffuserContainer(map, random, biology,
            differentialPercentageToMove,
            percentageLimitOnDailyMovement
        );

        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);


    }
}
