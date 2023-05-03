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

import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborTransductionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.tripbased.ProfitFunctionRegression;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 7/21/16.
 */
public class ProfitFunctionRegressionTest {


    @Test
    public void regressionTest() throws Exception {
        Log.info("Makes sure that the catches/hr regressed are correct!");



        //build a full model with one fisher
        FishState state = new FishState(System.currentTimeMillis());

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(1);
        FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();
        biologyInitializer.setBiologySmoothingIndex(new FixedDoubleParameter(0d));
        scenario.setBiologyInitializer(biologyInitializer);
        FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(.01));
        scenario.setGear(gear);

        state.setScenario(scenario);
        state.start();

        //profit regression uses nearest neighbor
        ProfitFunctionRegression regression = new ProfitFunctionRegression(
                new ProfitFunction(24*5),
                new NearestNeighborTransductionFactory(),
                state
        );
        Fisher fisher = state.getFishers().get(0);
        fisher.addTripListener(new TripListener() {
            @Override
            public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
                regression.addObservation(new GeographicalObservation<>(record.getMostFishedTileInTrip(),
                                                                        state.getHoursSinceStart(),
                                                                        record),fisher,state
                );
            }
        });


        //make him go to 20,20
        SeaTile target = state.getMap().getSeaTile(20, 20);
        fisher.setDestinationStrategy(new FavoriteDestinationStrategy(target));
        for(int day=0; day<10; day++)
            state.schedule.step(state);
        //if I predict the catches it ought to be exactly the same as what I get at 20,20
        double predictedCatchesPerHour = regression.apply(state.getMap().getSeaTile(20, 20))[0];
        assertEquals(predictedCatchesPerHour,
                     target.getBiomass(state.getSpecies().get(0)) * .01,.001);

    }
}