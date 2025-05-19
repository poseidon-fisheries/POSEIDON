/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

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
        final DoubleParameter rockyCarryingCapacity,
        final DoubleParameter sandyCarryingCapacity,
        final double percentageLimitOnDailyMovement,
        final double differentialPercentageToMove,
        final LogisticGrowerInitializer grower
    ) {
        super(rockyCarryingCapacity, sandyCarryingCapacity, percentageLimitOnDailyMovement,
            differentialPercentageToMove, 2, grower
        );
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

            final double carryingCapacityFirst =
                (1 - seaTile.getRockyPercentage()) * getSandyCarryingCapacity().applyAsDouble(random) +
                    seaTile.getRockyPercentage() * getRockyCarryingCapacity().applyAsDouble(random);

            final double carryingCapacitySecond =
                (1 - seaTile.getRockyPercentage()) * getRockyCarryingCapacity().applyAsDouble(random) +
                    seaTile.getRockyPercentage() * getSandyCarryingCapacity().applyAsDouble(random);


            final BiomassLocalBiology local = new BiomassLocalBiology(
                new double[]{
                    carryingCapacityFirst * random.nextDouble(),
                    carryingCapacitySecond * random.nextDouble()
                },
                new double[]{
                    carryingCapacityFirst, carryingCapacitySecond
                }
            );
            biologies.put(seaTile, local);
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
