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
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Each tile below water is filled with an independent logistic-growth. They all have the same carrying capacity
 * but each starts at a random level of biomass
 * Created by carrknight on 6/22/15.
 */
public class IndependentLogisticInitializer extends AbstractBiologyInitializer {


    private final DoubleParameter carryingCapacity;


    private final DoubleParameter minInitialCapacity;

    private final DoubleParameter maxInitialCapacity;

    private final Map<SeaTile, BiomassLocalBiology> biologies = new HashMap<>();

    private final LogisticGrowerInitializer grower;


    public IndependentLogisticInitializer(
        final DoubleParameter carryingCapacity,
        final DoubleParameter minInitialCapacity, final DoubleParameter maxInitialCapacity,
        final LogisticGrowerInitializer grower
    ) {
        this.carryingCapacity = carryingCapacity;
        this.minInitialCapacity = minInitialCapacity;
        this.maxInitialCapacity = maxInitialCapacity;
        this.grower = grower;
    }


    public IndependentLogisticInitializer(
        final DoubleParameter carryingCapacity, final LogisticGrowerInitializer grower
    ) {
        this(carryingCapacity, new FixedDoubleParameter(0), new FixedDoubleParameter(1d), grower);
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
        final GlobalBiology biology,
        final SeaTile seaTile,
        final MersenneTwisterFast random,
        final int mapHeightInCells,
        final int mapWidthInCells,
        final NauticalMap map
    ) {

        if (seaTile.isLand())
            return new EmptyLocalBiology();
        else {
            final int species = biology.getSize();
            final double carryingCapacityLevel = carryingCapacity.applyAsDouble(random);
            final double minCapacity = minInitialCapacity.applyAsDouble(random);
            final double maxCapacity = maxInitialCapacity.applyAsDouble(random);

            final BiomassLocalBiology local = new BiomassLocalBiology(carryingCapacityLevel, species, random,
                maxCapacity, minCapacity
            );
            biologies.put(seaTile, local);
            return local;
        }
    }

    /**
     * ignored
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random  mersenne randomizer
     * @param model
     */
    @Override
    public void processMap(
        final GlobalBiology biology, final NauticalMap map, final MersenneTwisterFast random, final FishState model
    ) {

        grower.initializeGrower(biologies, model, random, biology.getSpecie(0));


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
