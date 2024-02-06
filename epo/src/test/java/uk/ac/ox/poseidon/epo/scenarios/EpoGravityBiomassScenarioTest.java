/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.epo.scenarios;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class EpoGravityBiomassScenarioTest {
    @Test
    public void noFishGetsCaughtAndThrownOverboardImmediately() {

        final EpoGravityBiomassScenario scenario = new EpoGravityBiomassScenario();
        scenario.useDummyData();

        final Regulation regulation = new FishingSeason(true, 100);
        scenario.getAdditionalStartables().put(
            "Catches checker",
            state -> model -> {
                model.getFishers().forEach(fisher -> fisher.setRegulation(regulation));
                state.scheduleEveryYear(simState -> {
                    final FishStateYearlyTimeSeries yearlyDataSet =
                        ((FishState) simState).getYearlyDataSet();
                    final double catches =
                        yearlyDataSet.getColumn("Skipjack tuna Catches (kg)").getLatest();
                    final double landings =
                        yearlyDataSet.getColumn("Skipjack tuna Landings").getLatest();
                    System.out.printf("Catches:    %.2f%n", catches);
                    System.out.printf("Landings:   %.2f%n", landings);
                    System.out.printf("Difference: %.2f%n", Math.abs(catches - landings));
                    Assertions.assertTrue(catches > 1000);
                    Assertions.assertTrue(landings > 1000);
                    Assertions.assertTrue(Math.abs(catches - landings) < EPSILON);
                }, StepOrder.AFTER_DATA);
            }
        );

        final FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        while (state.getYear() < 5) {
            state.schedule.step(state);
        }
        state.schedule.step(state);
    }

}
