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

package uk.ac.ox.oxfish.biology.complicated.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 7/7/17.
 */
public class ListMeristicFactoryTest {


    @Test
    public void buildsAllright() throws Exception {

        ListMeristicFactory factory = new ListMeristicFactory();
        factory.setMortalityRate(new FixedDoubleParameter(.2));
        factory.setWeightsPerBin("1,2,3,4,5,6");
        //factory.setMaturityPerBin("1,2,3,4,5,6");
        FromListMeristics meristics = factory.apply(mock(FishState.class));

        Assertions.assertEquals(meristics.getMaxAge(), 5);
        Assertions.assertEquals(meristics.getWeight(FishStateUtilities.MALE, 2), 3, .001);


    }
}
