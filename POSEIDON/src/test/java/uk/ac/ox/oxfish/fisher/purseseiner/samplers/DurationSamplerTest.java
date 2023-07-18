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

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;

import java.util.function.DoubleSupplier;

import static java.lang.Math.E;
import static junit.framework.TestCase.assertEquals;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class DurationSamplerTest {

    @Test
    public void nextDuration() {

        final DoubleSupplier durationSampler = new DurationSampler(
            new MersenneTwisterFast(),
            1.0,
            Double.MIN_VALUE
        );

        assertEquals(
            E,
            durationSampler.getAsDouble(),
            EPSILON
        );
    }

}