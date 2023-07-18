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

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.Belief;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.Particle;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 8/1/16.
 */
public class BeliefTest {


    @Test
    public void beliefsFormCorrectly() throws Exception {


        LinkedList<Particle<Double>> particles = new LinkedList<>();

        particles.add(new Particle<>(3d));
        particles.add(new Particle<>(3d));
        particles.add(new Particle<>(4d));
        particles.add(new Particle<>(5d));

        Belief<Double> belief = new Belief<>(particles);
        double[] summary = Belief.getSummaryStatistics(belief);
        assertEquals(summary[0], 3.75, .001);
        assertEquals(summary[1], 0.82916, .001);

        LinkedList<Double> sample = belief.sample(new MersenneTwisterFast(), 100);
        for (Double temp : sample)
            assertTrue(temp == 3 || temp == 4 || temp == 5);

    }
}