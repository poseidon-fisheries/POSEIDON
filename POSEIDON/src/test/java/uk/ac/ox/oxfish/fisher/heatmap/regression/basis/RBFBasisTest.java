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

package uk.ac.ox.oxfish.fisher.heatmap.regression.basis;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 3/7/17.
 */
public class RBFBasisTest {

    @Test
    public void computesCorrectly() throws Exception {

        RBFBasis basis = new RBFBasis(10, 2, 2);
        double similiarity = basis.evaluate(new double[]{1, 1});

        assertEquals(similiarity, .1141512, .0001);

    }
}