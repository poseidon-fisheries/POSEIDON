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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.regulations.ActiveFadLimits;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static com.google.common.collect.Streams.stream;
import static uk.ac.ox.oxfish.model.StepOrder.DAWN;
import static uk.ac.ox.oxfish.model.StepOrder.FISHER_PHASE;

public abstract class FadDeactivationStrategy implements FisherStartable, Steppable {
    private static final long serialVersionUID = 718758206768136198L;
    private FadManager fadManager;

    public FadManager getFadManager() {
        return fadManager;
    }

    @Override
    public void start(
        final FishState fishState,
        final Fisher fisher
    ) {
        this.fadManager = FadManager.getFadManager(fisher);
        // This is convoluted because we can't use FishState::scheduleEveryYear,
        // which schedules stuff on December 31st. We need to wait for the first
        // actual day of the simulation _and then_ schedule every 365 days.
        fishState.scheduleOnceInXDays(
            simState -> ((FishState) simState).scheduleEveryXDay(
                this,
                FISHER_PHASE,
                365
            ),
            DAWN,
            1
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;
        stream(extractActiveFadsLimit(fishState.getRegulations(), fishState.getCalendarYear()))
            .filter(limit -> limit < fadManager.getNumberOfActiveFads())
            .forEach(limit ->
                deactivate(fadManager.getNumberOfActiveFads() - limit)
            );
    }

    protected abstract void deactivate(int numberOfFadsToDeactivate);

    @SuppressWarnings("UnstableApiUsage")
    private OptionalInt extractActiveFadsLimit(
        final Regulations regulations,
        final int calendarYear
    ) {
        return regulations
            .getSubRegulations()
            .stream()
            .filter(ActiveFadLimits.class::isInstance)
            .map(ActiveFadLimits.class::cast)
            .map(ActiveFadLimits::getLimitsPerYearAndClass)
            .flatMapToInt(limits ->
                stream(Optional.ofNullable(limits.get(calendarYear)))
                    .flatMap(yearlyLimits -> yearlyLimits.entrySet().stream())
                    .filter(classLimitEntry -> this.fadManager
                        .getFisher()
                        .getTags()
                        .contains("class " + classLimitEntry.getKey()))
                    .mapToInt(Map.Entry::getValue)
            )
            .min();
    }
}
