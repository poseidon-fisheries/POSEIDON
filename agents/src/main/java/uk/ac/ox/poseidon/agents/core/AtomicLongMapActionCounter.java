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

import com.google.common.util.concurrent.AtomicLongMap;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.agents.api.ActionCounter;
import uk.ac.ox.poseidon.agents.api.Agent;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class AtomicLongMapActionCounter implements ActionCounter {

    private final Map<? super Agent, AtomicLongMap<String>> counts;

    private AtomicLongMapActionCounter(final Map<? super Agent, AtomicLongMap<String>> counts) {
        this.counts = counts;
    }

    public static ActionCounter create() {
        return new AtomicLongMapActionCounter(new HashMap<>());
    }

    @Override
    public void observe(final Action action) {
        countsFor(action.getAgent()).incrementAndGet(action.getCode());
    }

    private AtomicLongMap<String> countsFor(final Agent agent) {
        return counts.computeIfAbsent(agent, __ -> AtomicLongMap.create());
    }

    @Override
    public long getCount(final Agent agent, final String actionCode) {
        return countsFor(agent).get(actionCode);
    }

    @Override
    public ActionCounter copy() {
        return new AtomicLongMapActionCounter(
            counts.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                entry -> AtomicLongMap.create(entry.getValue().asMap())
            ))
        );
    }
}
