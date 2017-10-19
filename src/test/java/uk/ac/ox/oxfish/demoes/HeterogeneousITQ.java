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

package uk.ac.ox.oxfish.demoes;

import com.esotericsoftware.minlog.Log;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.experiments.TwoPopulations;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;

import java.util.DoubleSummaryStatistics;

/**
 * Created by carrknight on 3/8/16.
 */
public class HeterogeneousITQ {


    private final static double LOW_PRICE = 0;

    private final static double HIGH_PRICE = 0.5;
    public static final int RUNS = 5;


    @Test
    public void largerBoatsGetMoreCatchesAsGasPriceGoUp() throws Exception {
        Log.info("Here we replicate the fleet heterogeneity example in " +
                         "http://carrknight.github.io/assets/oxfish/heterogeneous.html  ; " +
                         "The idea is that larger boats, when gas is only spent travelling, " +
                         "are more efficient so that as gas prices go up they end up buying more quotas" +
                         "from smaller boats"
        );


        //low price
        DoubleSummaryStatistics averageBigLandings =new DoubleSummaryStatistics();
        DoubleSummaryStatistics averageSmallLandings =new DoubleSummaryStatistics();
        for(int run = 0; run< RUNS; run++)
        {

            FishState state = TwoPopulations.itqExample(LOW_PRICE);
            Species species = state.getSpecies().get(0);
            for(Fisher fisher : state.getFishers())
            {
                if(Math.abs(fisher.getMaximumHold()-10)<.1)
                {
                    averageSmallLandings.accept(
                            fisher.getLatestYearlyObservation(species + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
                    );
                }
                else
                {
                    assert Math.abs(fisher.getMaximumHold()-500)<.1 : fisher.getMaximumHold();
                    averageBigLandings.accept(
                            fisher.getLatestYearlyObservation(species + " " +AbstractMarket.LANDINGS_COLUMN_NAME)
                    );
                }
            }

        }

        double proportion = averageBigLandings.getAverage() / (averageBigLandings.getAverage() +
                averageSmallLandings.getAverage());
        Log.info("When prices are low, I expect the proportion of catches going to the big fishers" +
                         " to be less than .7, it is in fact: " + proportion);
        Assert.assertTrue(proportion <.7);


        //high price
        averageBigLandings =new DoubleSummaryStatistics();
        averageSmallLandings =new DoubleSummaryStatistics();
        for(int run=0; run<RUNS; run++)
        {

            FishState state = TwoPopulations.itqExample(HIGH_PRICE);
            Species species = state.getSpecies().get(0);
            for(Fisher fisher : state.getFishers())
            {
                if(Math.abs(fisher.getMaximumHold()-10)<.1)
                {
                    averageSmallLandings.accept(
                            fisher.getLatestYearlyObservation(species + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
                    );
                }
                else
                {
                    assert Math.abs(fisher.getMaximumHold()-500)<.11;
                    averageBigLandings.accept(
                            fisher.getLatestYearlyObservation(species + " " +AbstractMarket.LANDINGS_COLUMN_NAME)
                    );
                }
            }

        }

        proportion = averageBigLandings.getAverage() / (averageBigLandings.getAverage() +
                averageSmallLandings.getAverage());
        Log.info("When prices are high, I expect the proportion of catches going to the big fishers" +
                         " to be more than .8, it is in fact: " + proportion);
        Assert.assertTrue(proportion >.8);





    }
}
