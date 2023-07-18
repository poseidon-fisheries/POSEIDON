/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.maximization.generic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static uk.ac.ox.oxfish.maximization.generic.SymmetricMeanAbsolutePercentageErrorMeasure.INSTANCE;

public class SymmetricMeanAbsolutePercentageErrorMeasureTest {

    @Test
    public void test() {

        // At = 100 and Ft = 110 give SMAPE = 4.76%
        Assertions.assertEquals(4.76, INSTANCE.applyAsDouble(100, 110), 0.005);

        // At = 100 and Ft = 90 give SMAPE = 5.26%.
        Assertions.assertEquals(5.26, INSTANCE.applyAsDouble(100, 90), 0.005);

    }

}