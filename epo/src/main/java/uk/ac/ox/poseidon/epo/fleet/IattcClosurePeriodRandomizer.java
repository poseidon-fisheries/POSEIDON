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

package uk.ac.ox.poseidon.epo.fleet;

import sim.engine.Steppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;

import static java.time.Month.JULY;
import static uk.ac.ox.poseidon.epo.fleet.EpoPurseSeineVesselReader.chooseClosurePeriod;
import static uk.ac.ox.poseidon.epo.scenarios.EpoScenario.dayOfYear;

public class IattcClosurePeriodRandomizer implements AdditionalStartable {

    private final double proportionOfBoatsInClosureA;

    public IattcClosurePeriodRandomizer(final double proportionOfBoatsInClosureA) {
        this.proportionOfBoatsInClosureA = proportionOfBoatsInClosureA;
    }

    @Override
    public void start(final FishState fishState) {

        // Every year, on July 15th, purse seine vessels must choose
        // which temporal closure period they will observe.
        final int daysFromNow = 1 + dayOfYear(fishState.getCalendarYear(), JULY, 15);

        fishState.getFishers().forEach(fisher -> {

            final Steppable assignClosurePeriod = simState -> {
                final String closure = simState.random.nextDouble() < proportionOfBoatsInClosureA ? "A" : "B";
                if (fisher.getRegulation() instanceof MultipleRegulations) {
                    chooseClosurePeriod(fisher, closure);
                    ((MultipleRegulations) fisher.getRegulation()).reassignRegulations(fishState, fisher);
                }
            };

            fishState.scheduleOnceInXDays(assignClosurePeriod, StepOrder.DAWN, daysFromNow);
            fishState.scheduleOnceInXDays(
                simState -> fishState.scheduleEveryXDay(assignClosurePeriod, StepOrder.DAWN, 365),
                StepOrder.DAWN,
                daysFromNow
            );

        });
    }

}
