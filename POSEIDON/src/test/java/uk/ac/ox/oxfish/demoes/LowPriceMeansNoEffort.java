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

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.experiments.EffortThrottling;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;


public class LowPriceMeansNoEffort {


    /**
     * if the price doesn't pay for trips, just stay home.
     *
     * @throws Exception
     */
    @Test
    public void priceIsTooLowAgentsStopFishing() throws Exception {


        //sets very low price
        FixedPriceMarketFactory market = new FixedPriceMarketFactory();
        market.setMarketPrice(new FixedDoubleParameter(2.0));

        FishState state = EffortThrottling.effortThrottling(40, market, System.currentTimeMillis(),
            new UniformDoubleParameter(0.001, 1), null, null
        );

        //probability should be very low!
        Assert.assertTrue(state.getDailyDataSet().getLatestObservation("Probability to leave port") < .001);

    }
}
