/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Collection;
import java.util.Map;

public class CommonLogisticGrowerInitializer implements LogisticGrowerInitializer {

    private final DoubleParameter steepness;
    private final double distributionalWeight;


    public CommonLogisticGrowerInitializer(final DoubleParameter steepness, final double distributeProportionally) {
        this.steepness = steepness;
        this.distributionalWeight = distributeProportionally;
    }

    @Override
    public void initializeGrower(
        final Map<SeaTile, BiomassLocalBiology> tiles,
        final FishState state,
        final MersenneTwisterFast random,
        final Species species
    ) {

        final Collection<BiomassLocalBiology> biologies = tiles.values();
        if (biologies.isEmpty())
            return;
        //initialize the malthusian parameter

        final CommonLogisticGrower grower = new CommonLogisticGrower(
            steepness.applyAsDouble(random),
            species,
            distributionalWeight
        );

        //add all the biologies
        for (final BiomassLocalBiology biology : biologies)
            grower.getBiologies().add(biology);
        state.registerStartable(grower);

    }
}
