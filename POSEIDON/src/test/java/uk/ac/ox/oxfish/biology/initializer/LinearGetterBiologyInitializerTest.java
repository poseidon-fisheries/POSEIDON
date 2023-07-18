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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 2/6/17.
 */
public class LinearGetterBiologyInitializerTest {


    @Test
    public void simpleFunction() throws Exception {

        final FishState state = mock(FishState.class, RETURNS_DEEP_STUBS);
        //0,0 with 0% rocky
        final SeaTile zeroZero = new SeaTile(0, 0, -100, new TileHabitat(0));
        //5,5 with 50% rocky
        final SeaTile fiveFive = new SeaTile(5, 5, -100, new TileHabitat(.5));

        final LinearGetterBiologyInitializer initializer = new LinearGetterBiologyInitializer(
            100,
            1,
            2,
            -1,
            0,
            10,
            0,
            0,
            0
        );


        final GlobalBiology globalBiology = initializer.generateGlobal(new MersenneTwisterFast(), state);
        zeroZero.setBiology(initializer.generateLocal(globalBiology,
            zeroZero,
            new MersenneTwisterFast(),
            5, 5, mock(NauticalMap.class)
        ));
        fiveFive.setBiology(initializer.generateLocal(globalBiology,
            fiveFive,
            new MersenneTwisterFast(),
            5, 5, mock(NauticalMap.class)
        ));

        zeroZero.start(state);
        fiveFive.start(state);

        when(state.getDayOfTheYear()).thenReturn(0);
        Assertions.assertEquals(zeroZero.getBiomass(globalBiology.getSpecie(0)), 100d, 0.001d);
        //100 + 5 + 10 + 10*.5
        Assertions.assertEquals(fiveFive.getBiomass(globalBiology.getSpecie(0)), 120d, 0.001d);


        when(state.getDayOfTheYear()).thenReturn(2);
        Assertions.assertEquals(zeroZero.getBiomass(globalBiology.getSpecie(0)), 100d, 0.001d);
        //100 + 5 + 10 + 10*.5 - 10
        Assertions.assertEquals(fiveFive.getBiomass(globalBiology.getSpecie(0)), 110d, 0.001d);

    }
}