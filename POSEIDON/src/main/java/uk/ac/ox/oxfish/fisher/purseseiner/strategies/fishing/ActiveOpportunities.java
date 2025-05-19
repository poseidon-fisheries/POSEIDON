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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import static java.util.stream.IntStream.range;

public class ActiveOpportunities implements Startable, Steppable {

    private static final long serialVersionUID = -1764676443556080828L;
    private final Multimap<Integer, Int2D> opportunities = HashMultimap.create();
    private boolean isStarted = false;

    boolean hasOpportunity(final Int2D gridLocation, final int step) {
        return opportunities.containsEntry(step, gridLocation);
    }

    void addOpportunity(final Int2D gridLocation, final int step, final int duration) {
        range(step, step + duration).forEach(t -> opportunities.put(t, gridLocation));
    }

    @Override
    public void step(final SimState simState) {
        opportunities.removeAll(((FishState) simState).getStep() - 1);
    }

    @Override
    public void start(final FishState model) {
        if (isStarted) throw new IllegalStateException(this + "Already started");
        model.scheduleEveryDay(this, StepOrder.DAWN);
        isStarted = true;
    }
}
