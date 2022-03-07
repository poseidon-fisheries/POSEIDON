/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.utility.parameters;

import ec.util.MersenneTwisterFast;
import org.junit.Test;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleConsumer;

import static org.junit.Assert.*;

public class WeibullDoubleParameterTest {


    @Test
    public void looksAboutRight() {
        WeibullDoubleParameter weibull = new WeibullDoubleParameter(10000,0.5);

        MersenneTwisterFast rng = new MersenneTwisterFast();
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.apply(rng));
        }
        assertTrue(stats.getAverage()>.45);
        assertTrue(stats.getAverage()<.55);
        assertTrue(stats.getMin()>.45);
        assertTrue(stats.getMax()<.55);


    }

    @Test
    public void looksAboutRight2() {
        WeibullDoubleParameter weibull = new WeibullDoubleParameter(0.5,10000);

        MersenneTwisterFast rng = new MersenneTwisterFast();
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.apply(rng));
        }
        assertTrue(stats.getAverage()>18000);
        assertTrue(stats.getAverage()<22000);



    }

    @Test
    public void randomSeedWorks() {
        WeibullDoubleParameter weibull = new WeibullDoubleParameter(0.5,10000);

        MersenneTwisterFast rng = new MersenneTwisterFast(0);
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.apply(rng));
        }
        double firstAverage = stats.getAverage();

        weibull.setShape(0.5);
        rng = new MersenneTwisterFast(0);
        stats = new DoubleSummaryStatistics();
        for (int samples = 0; samples < 10000; samples++) {
            stats.accept(weibull.apply(rng));
        }
        assertEquals(firstAverage,stats.getAverage(),.0001);




    }
}