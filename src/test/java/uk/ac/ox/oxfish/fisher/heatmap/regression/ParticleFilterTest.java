package uk.ac.ox.oxfish.fisher.heatmap.regression;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.Belief;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.ParticleFilter;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 8/1/16.
 */
public class ParticleFilterTest
{



    @Test
    public void particle() throws Exception {

        MersenneTwisterFast random = new MersenneTwisterFast();
        ParticleFilter<Double> particleFilter = ParticleFilter.defaultParticleFilter(0, 1, .1, 100, random);

        Belief<Double> belief = particleFilter.getBelief();

        assertEquals(Belief.getSummaryStatistics(belief)[0], .5, .2);
        double earlyDeviation = Belief.getSummaryStatistics(belief)[1];

        for(int i=0; i<100; i++) {
            particleFilter.updateGivenEvidence(
                    FishStateUtilities.normalPDF(0.8,0.05), random
            );
            System.out.println(Belief.getSummaryStatistics(particleFilter.getBelief())[0]);
        }

        assertEquals(.8,Belief.getSummaryStatistics(particleFilter.getBelief())[0],.1);
        double midDeviation = Belief.getSummaryStatistics(particleFilter.getBelief())[1];
        assertTrue(midDeviation <earlyDeviation);

        for(int i=0; i<100; i++) {
            particleFilter.drift(random);
            System.out.println(Belief.getSummaryStatistics(particleFilter.getBelief())[0]);
        }
        assertEquals(Belief.getSummaryStatistics(particleFilter.getBelief())[0],.5,.2);
        double lateDeviation = Belief.getSummaryStatistics(particleFilter.getBelief())[1];
        assertTrue(lateDeviation > midDeviation);

    }
}