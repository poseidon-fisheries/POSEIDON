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

package uk.ac.ox.oxfish.biology.boxcars;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;

public class EquallySpacedBertalanffyFactoryTest {


    @Test
    public void onehundredBins() throws Exception {


        EquallySpacedBertalanffyFactory factory = new EquallySpacedBertalanffyFactory();
        factory.setkYearlyParameter(new FixedDoubleParameter(0.364));
        factory.setMaxLengthInCm(new FixedDoubleParameter(113));
        factory.setRecruitLengthInCm(new FixedDoubleParameter(10));
        factory.setNumberOfBins(100);

        GrowthBinByList species = factory.apply(mock(FishState.class));

        //numbers come from fixedBoxcar.R
        Assert.assertEquals(species.getLength(0,0),10.0,.0001);
        Assert.assertEquals(species.getLength(0,5),15.2020202020202,.0001);
        Assert.assertEquals(species.getLength(0,99),113,.0001);


    }
}