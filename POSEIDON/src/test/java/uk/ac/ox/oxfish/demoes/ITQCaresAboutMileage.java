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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.experiments.MarketFirstDemo;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.Iterator;
import java.util.stream.DoubleStream;

public class ITQCaresAboutMileage {

    @Test
    public void itqCaresAboutMileage() throws Exception {

        long seed = System.currentTimeMillis();
        FishState state =
            MarketFirstDemo.generateAndRunMarketDemo(MarketFirstDemo.MarketDemoPolicy.ITQ,
                new FixedDoubleParameter(.1),
                new UniformDoubleParameter(0, 20),
                null,
                5, seed, true
            );

        // the correlation ought to be very small
        Species species = state.getSpecies().get(0);

        double[] mileage = new double[state.getFishers().size()];
        double[] catches = new double[state.getFishers().size()];

        int i = 0;
        for (Fisher fisher : state.getFishers()) {
            mileage[i] = (((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished());
            catches[i] = fisher.getLatestYearlyObservation(
                species + " " + AbstractMarket.LANDINGS_COLUMN_NAME);

            i++;
        }

        System.out.println("seed " + seed);
        System.out.println("Correlation: " +
            Double.toString(FishStateUtilities.computeCorrelation(mileage, catches)));
        // efficiency is 100%
        Assertions.assertEquals(400000.0, DoubleStream.of(catches).sum(), .1);

        // make sure the same number of landings is recorded in the market
        DataColumn marketData = state.getAllMarketsForThisSpecie(species).get(0).getData().getColumn(
            AbstractMarket.LANDINGS_COLUMN_NAME);
        Iterator<Double> doubleIterator = marketData.descendingIterator();
        double landedCatches = 0;
        for (i = 0; i < 365; i++) {

            landedCatches += doubleIterator.next();
        }
        // sum up the last 365 days of observations
        Assertions.assertEquals(400000, landedCatches, .1);

        Assertions.assertTrue(FishStateUtilities.computeCorrelation(mileage, catches) < -.45);

    }
}
