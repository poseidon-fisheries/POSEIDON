/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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

package uk.ac.ox.poseidon.agents.core;

import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.ActionCounter;
import uk.ac.ox.poseidon.agents.api.Agent;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounter;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AtomicLongMapYearlyActionCounter implements YearlyActionCounter {

    private final Map<? super Integer, ActionCounter> counts;

    private AtomicLongMapYearlyActionCounter(final Map<? super Integer, ActionCounter> counts) {
        this.counts = counts;
    }

    public static YearlyActionCounter create() {
        return new AtomicLongMapYearlyActionCounter(new HashMap<>());
    }

    @Override
    public long getCount(
        final int year,
        final Agent agent,
        final String actionCode
    ) {
        return getActionCounter(year).getCount(agent, actionCode);
    }

    private ActionCounter getActionCounter(final int year) {
        return counts.computeIfAbsent(year, __ -> AtomicLongMapActionCounter.create());
    }

    @Override
    public void observe(final Action action) {
        action.getDateTime().ifPresent(dateTime ->
            getActionCounter(dateTime.getYear()).observe(action)
        );
    }

    @Override
    public YearlyActionCounter copy() {
        return new AtomicLongMapYearlyActionCounter(
            counts.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().copy()
            ))
        );
    }
}
