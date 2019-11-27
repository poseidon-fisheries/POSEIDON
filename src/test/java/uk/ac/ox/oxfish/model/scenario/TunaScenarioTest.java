/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.scenario;

import org.junit.Assert;
import org.junit.Test;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import static org.junit.Assert.*;

public class TunaScenarioTest {


    @Test
    public void noFishGetsCaughtAndThrownOverboardImmediately() {
        TunaScenario scenario = new TunaScenario();
        scenario.getPlugins().add(
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState state) {

                        return new AdditionalStartable(){
                            /**
                             * this gets called by the fish-state right after the scenario has started. It's useful
                             * to set up steppables
                             * or just to percolate a reference to the model
                             *
                             * @param model the model
                             */
                            @Override
                            public void start(FishState model) {
                                for (Fisher fisher : model.getFishers()) {
                                    fisher.setRegulation(new FishingSeason(true, 100));
                                }
                                state.scheduleEveryYear(new Steppable() {
                                    @Override
                                    public void step(SimState simState) {

                                        Double catches = ((FishState) simState).getYearlyDataSet().getColumn(
                                                "Skipjack tuna Catches (kg)").getLatest();

                                        Double landings = ((FishState) simState).getYearlyDataSet().getColumn(
                                                "Skipjack tuna Landings").getLatest();


                                        System.out.println(catches);
                                        System.out.println(landings);
                                        Assert.assertTrue(catches>1000);
                                        Assert.assertTrue(landings>1000);
                                        Assert.assertTrue(Math.abs(catches - landings)<.01);
                                        System.out.println(Math.abs(catches - landings));
                                    }
                                }, StepOrder.AFTER_DATA);

                            }
                        };


                    };
                }
        );


        FishState state = new FishState();
        state.setScenario(scenario);;

        state.start();;

        while(state.getYear()<5)
            state.schedule.step(state);

        state.schedule.step(state);
    }
}