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

package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Collection;
import java.util.Map;

/**
 * Creates ONE IndependentLogisticBiomassGrower and assigns every logisticLocalBiology to it.
 * Created by carrknight on 1/31/17.
 */
public class SimpleLogisticGrowerInitializer implements LogisticGrowerInitializer {


    private final DoubleParameter steepness;


    public SimpleLogisticGrowerInitializer(final DoubleParameter steepness) {
        this.steepness = steepness;
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

        final IndependentLogisticBiomassGrower grower = new IndependentLogisticBiomassGrower(
            steepness.applyAsDouble(random),
            species
        );

        //add all the biologies
        for (final BiomassLocalBiology biology : biologies)
            grower.getBiologies().add(biology);
        state.registerStartable(grower);

    }


}
