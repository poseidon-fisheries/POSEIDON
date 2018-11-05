/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.EquallySpacedBertalanffyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class LinearSelectivityFilterTest {


    @Test
    public void malabaricus() {
        EquallySpacedBertalanffyFactory factory = new EquallySpacedBertalanffyFactory();
        factory.setAllometricAlpha(new FixedDoubleParameter(0.00853));
        factory.setAllometricBeta(new FixedDoubleParameter(3.137));
        factory.setkYearlyParameter(new FixedDoubleParameter(0.4438437));
        factory.setMaxLengthInCm(new FixedDoubleParameter(86));
        factory.setRecruitLengthInCm(new FixedDoubleParameter(6));
        factory.setNumberOfBins(20);

        Species species = new Species("malabaricus",
                                      factory.apply(mock(FishState.class))
                                      );


        LinearSelectivityFilter selectivity =
                new LinearSelectivityFilter(28,
                                            55,
                                            0.4438437,
                                            63.23406,
                                                                                      86
        );


        double[][] mortality = selectivity.computeSelectivity(species);

        System.out.println(Arrays.toString(mortality[0]));

        assertEquals(0,mortality[0][0],.0001);
        assertEquals(0.84956384,mortality[0][mortality[0].length-1],.0001);




    }


}