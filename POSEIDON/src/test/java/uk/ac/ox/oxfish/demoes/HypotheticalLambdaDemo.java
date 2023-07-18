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

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.MovingAveragePredictor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import static org.junit.Assert.assertTrue;


public class HypotheticalLambdaDemo {


    @Test
    public void lambdaEstimation() throws Exception {


        PrototypeScenario scenario = new PrototypeScenario();
        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                for (Fisher fisher : model.getFishers()) {


                    //reset predictors to moving averages
                    fisher.setDailyCatchesPredictor(0, MovingAveragePredictor.dailyMAPredictor(
                        "Predicted Daily Catches",
                        fisher1 -> fisher1.getDailyCounter().getLandingsPerSpecie(
                            0),
                        90
                    ));
                    fisher.setProfitPerUnitPredictor(0, MovingAveragePredictor.perTripMAPredictor(
                        "Predicted Unit Profit",
                        fisher1 -> fisher1.getLastFinishedTrip().getUnitProfitPerSpecie(0),
                        30
                    ));


                    //create a lambda gatherer
                    fisher.getDailyData().registerGatherer("Reservation Lambda Owning 1000 quotas",
                        fisher1 -> {
                            if (state.getDayOfTheYear() == 365)
                                return Double.NaN;
                            double probability = 1 - fisher1.probabilityDailyCatchesBelowLevel(
                                0,
                                1000 / (365 - state.getDayOfTheYear())
                            );
                            return (probability * fisher1.predictUnitProfit(0));
                        }, Double.NaN
                    );


                }

            }

            @Override
            public void turnOff() {

            }
        });

        state.start();

        while (state.getYear() < 4)
            state.schedule.step(state);


        //write first histogram
        while (state.getDayOfTheYear() != 100)
            state.schedule.step(state);


        double averageLambda = state.getFishers()
            .stream()
            .mapToDouble(
                value -> Math.max(0,
                    value.getDailyData().getLatestObservation("Reservation Lambda Owning 1000 quotas")))
            .sum() / 100;


        //lambda estimated is higher than 3
        System.out.println("mid year lambda: " + averageLambda);
        assertTrue(averageLambda > 3);

        while (state.getDayOfTheYear() != 360)
            state.schedule.step(state);


        //at the end of the year, lambda is lower than 1
        averageLambda = state.getFishers().stream().mapToDouble(
            value -> value.getDailyData().getLatestObservation("Reservation Lambda Owning 1000 quotas")).sum() / 100;
        System.out.println("end year lambda: " + averageLambda);
        assertTrue(averageLambda < 1);


    }
}
