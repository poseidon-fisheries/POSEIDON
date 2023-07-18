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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.Belief;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.ParticleFilter;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Created by carrknight on 8/1/16.
 */
public class ParticleFilterTest {


    @Test
    public void particle() throws Exception {

        MersenneTwisterFast random = new MersenneTwisterFast();
        ParticleFilter<Double> particleFilter = ParticleFilter.defaultParticleFilter(0, 1, .1, 100, random);

        Belief<Double> belief = particleFilter.getBelief();

        Assertions.assertEquals(Belief.getSummaryStatistics(belief)[0], .5, .2);
        double earlyDeviation = Belief.getSummaryStatistics(belief)[1];

        for (int i = 0; i < 100; i++) {
            particleFilter.updateGivenEvidence(
                FishStateUtilities.normalPDF(0.8, 0.05), random
            );
            System.out.println(Belief.getSummaryStatistics(particleFilter.getBelief())[0]);
        }

        Assertions.assertEquals(.8, Belief.getSummaryStatistics(particleFilter.getBelief())[0], .1);
        double midDeviation = Belief.getSummaryStatistics(particleFilter.getBelief())[1];
        Assertions.assertTrue(midDeviation < earlyDeviation);

        for (int i = 0; i < 100; i++) {
            particleFilter.drift(random);
            System.out.println(Belief.getSummaryStatistics(particleFilter.getBelief())[0]);
        }
        Assertions.assertEquals(Belief.getSummaryStatistics(particleFilter.getBelief())[0], .5, .2);
        double lateDeviation = Belief.getSummaryStatistics(particleFilter.getBelief())[1];
        Assertions.assertTrue(lateDeviation > midDeviation);

    }
}