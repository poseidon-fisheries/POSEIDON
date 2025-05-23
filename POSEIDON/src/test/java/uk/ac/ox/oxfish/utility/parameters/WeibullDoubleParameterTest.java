/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.DoubleSummaryStatistics;

public class WeibullDoubleParameterTest {


    @Test
    public void looksAboutRight() {
        final WeibullDoubleParameter weibull = new WeibullDoubleParameter(10000, 0.5);

        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.applyAsDouble(rng));
        }
        Assertions.assertTrue(stats.getAverage() > .45);
        Assertions.assertTrue(stats.getAverage() < .55);
        Assertions.assertTrue(stats.getMin() > .45);
        Assertions.assertTrue(stats.getMax() < .55);


    }

    @Test
    public void looksAboutRight2() {
        final WeibullDoubleParameter weibull = new WeibullDoubleParameter(0.5, 10000);

        final MersenneTwisterFast rng = new MersenneTwisterFast();
        final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.applyAsDouble(rng));
        }
        Assertions.assertTrue(stats.getAverage() > 18000);
        Assertions.assertTrue(stats.getAverage() < 22000);


    }

    @Test
    public void randomSeedWorks() {
        final WeibullDoubleParameter weibull = new WeibullDoubleParameter(0.5, 10000);

        MersenneTwisterFast rng = new MersenneTwisterFast(0);
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.applyAsDouble(rng));
        }
        final double firstAverage = stats.getAverage();

        weibull.setShape(0.5);
        rng = new MersenneTwisterFast(0);
        stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.applyAsDouble(rng));
        }
        Assertions.assertEquals(firstAverage, stats.getAverage(), .0001);


    }
}
