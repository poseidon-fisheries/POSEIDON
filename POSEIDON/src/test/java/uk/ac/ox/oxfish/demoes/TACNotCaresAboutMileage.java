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

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.experiments.MarketFirstDemo;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;


public class TACNotCaresAboutMileage {


    @Test
    public void tacNotCaresAboutMileage() throws Exception {

        final FishState state =
            MarketFirstDemo.generateAndRunMarketDemo(MarketFirstDemo.MarketDemoPolicy.TAC,
                new FixedDoubleParameter(.1),
                new UniformDoubleParameter(0, 20),
                Paths.get("runs", "market1", "tacOil.csv").toFile(),
                5, System.currentTimeMillis(), true
            );

        state.getRandom().setSeed(0);

        //the correlation ought to be very small
        final Species species = state.getSpecies().get(0);

        final double[] mileage = new double[state.getFishers().size()];
        final double[] catches = new double[state.getFishers().size()];

        int i = 0;
        for (final Fisher fisher : state.getFishers()) {
            mileage[i] = (((RandomCatchabilityTrawl) fisher.getGear()).getGasPerHourFished());
            catches[i] = fisher.getLatestYearlyObservation(
                species + " " + AbstractMarket.LANDINGS_COLUMN_NAME);

            i++;
        }

        final double correlation = FishStateUtilities.computeCorrelation(mileage, catches);
        System.out.println("the correlation between mileage and TAC is: " + correlation);
        System.out.println("Ideally it should be, in absolute value, less than .3");
        assertTrue(correlation < .3);
        assertTrue(correlation > -.3);


    }
}
