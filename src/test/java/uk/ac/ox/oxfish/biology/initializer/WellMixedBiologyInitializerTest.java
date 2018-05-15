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
import org.junit.Test;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class WellMixedBiologyInitializerTest {


    @Test
    public void initializerMixesWell() throws Exception {

        WellMixedBiologyInitializer initializer =
                new WellMixedBiologyInitializer(new FixedDoubleParameter(60),
                                                new FixedDoubleParameter(.25),
                                                .01,.01,
                                                new SimpleLogisticGrowerInitializer(new FixedDoubleParameter(.8d))
                );



        SeaTile tile = mock(SeaTile.class);
        VariableBiomassBasedBiology biology = (VariableBiomassBasedBiology) initializer.generateLocal(mock(GlobalBiology.class),
                                                                                                      tile,
                                                                                                      new MersenneTwisterFast(), 50,
                                                                                                      50,
                                                                                                      mock(NauticalMap.class)
        );


        Species zero = new Species("zero"); zero.resetIndexTo(0);
        Species one = new Species("one"); one.resetIndexTo(1);


        assertEquals(biology.getCarryingCapacity(zero),60,.001);
        assertEquals(biology.getCarryingCapacity(one),20,.001);

    }
}