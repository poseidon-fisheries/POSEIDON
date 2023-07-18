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

package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;

import static org.mockito.Mockito.mock;


public class ProtectedAreasOnlyFactoryTest {


    @Test
    public void simpleFactoryTest() throws Exception {

        ProtectedAreasOnlyFactory factory = new ProtectedAreasOnlyFactory();
        final ProtectedAreasOnly first = factory.apply(mock(FishState.class));
        Assertions.assertTrue(first != null);

        final ProtectedAreasOnly second = factory.apply(mock(FishState.class));
        //they should be equal on account of being a singleton
        Assertions.assertEquals(first, second);

    }

}