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
import org.junit.jupiter.api.Test;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishOnceFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class NotGoingTooFar {

    @Test
    public void neverReallyGoBeyondWhereItIsNecessary() throws Exception {


        final FishState state = new FishState(System.currentTimeMillis());

        //world split in half
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 1, 0, 100000, 2));
        state.setScenario(scenario);
        //world split in half
        //scenario.setBiologyInitializer(OpportunityCostsDemo.FIXED_AND_SPLIT_BIOLOGY);
        FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();
        biologyInitializer.setMaximumBiomass(new FixedDoubleParameter(50000));
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setFishingStrategy(new FishOnceFactory());


        state.start();

        //find the highest X (rightmost) seatile that has biomass above 10000. There is never any point fishing left of there
        int limitX = 0;
        for (int i = 0; i < 50; i++) {
            if (state.getMap().getSeaTile(i, 0).getBiomass(state.getSpecies().get(0)) > 10000)
                limitX = i;
            else break;
        }


        while (state.getYear() < 1)
            state.schedule.step(state);

        int bestTows = 0;
        int worstTows = 0; //they still happen due to exploratory noise


        while (state.getYear() < 2) {
            state.schedule.step(state);
            IntGrid2D hotspots = state.getMap().getDailyTrawlsMap();

            for (int x = 0; x < limitX; x++) {
                worstTows += hotspots.get(x, 0);
            }
            //the tows must happen on the area before 100000 is hit
            for (int x = limitX; x < 50; x++) {
                bestTows += hotspots.get(x, 0);
            }
        }

        System.out.println(bestTows + " ---- " + worstTows + " ---- " + limitX);
        Assert.assertTrue(bestTows > 9 * worstTows);


    }
}
