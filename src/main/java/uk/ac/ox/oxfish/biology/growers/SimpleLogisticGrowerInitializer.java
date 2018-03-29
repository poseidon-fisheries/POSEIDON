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

package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Collection;
import java.util.Map;

/**
 * Creates ONE IndependentLogisticBiomassGrower and assigns every logisticLocalBiology to it.
 * Created by carrknight on 1/31/17.
 */
public class SimpleLogisticGrowerInitializer implements LogisticGrowerInitializer {


    private final DoubleParameter steepness;


    public SimpleLogisticGrowerInitializer(DoubleParameter steepness) {
        this.steepness = steepness;
    }

    @Override
    public void initializeGrower(
            Map<SeaTile, BiomassLocalBiology> tiles, FishState state, MersenneTwisterFast random, Species species)
    {

        Collection<BiomassLocalBiology> biologies = tiles.values();
        if(biologies.isEmpty())
            return;
        //initialize the malthusian parameter

        IndependentLogisticBiomassGrower grower = new IndependentLogisticBiomassGrower(
                steepness.apply(random),
                species);

        //add all the biologies
        for(BiomassLocalBiology biology : biologies)
            grower.getBiologies().add(biology);
        state.registerStartable(grower);

    }


}
