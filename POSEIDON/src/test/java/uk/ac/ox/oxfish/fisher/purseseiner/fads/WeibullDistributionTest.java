/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

public class WeibullDistributionTest {

    //We have extreme parameters for the Bigeye carrying capacity distribution
    //This is simple code to just check to see what the effect is on the carrying capacity.
    @Test
    public void testWeibull() {
        final WeibullDoubleParameter weibullDoubleParameter = new WeibullDoubleParameter(.0001, 4286);
        final MersenneTwisterFast mersenneTwisterFast = new MersenneTwisterFast();
        int nInf = 0;
        int nZero = 0;
        int nFinite = 0;
        for (int i = 0; i < 100000; i++) {
            final double rNum = weibullDoubleParameter.applyAsDouble(mersenneTwisterFast);
            if (rNum < 1) nZero++;
            if (rNum > 10000000) nInf++;
            if (rNum >= 1 && rNum <= 10000000) {
                nFinite++;
                System.out.println(rNum);
            }
        }
        System.out.println(nInf);
        System.out.println(nZero);
        System.out.println(nFinite);


    }
}
