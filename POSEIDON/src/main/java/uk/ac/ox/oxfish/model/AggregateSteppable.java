/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;

import java.util.LinkedList;
import java.util.List;

/**
 * Aggregate steppable is a collection of steppables that are pulsed without randomization. It's a simple hack to speed up
 * model.schedule. <p>
 * The problem is simple: MASON schedule randomizes everything all the time. When there are a lot of steppables the
 * randomization becomes expensive and painfully slow. This is due to the fact that the heap is simply too crowded. <p>
 * But many StepOrder of the model do not need randomization and can happen in any order (data-collection for example).
 * Those steppables then are aggregated in one of these objects so that the MASON steppable only has to deal with it.
 * Created by carrknight on 8/14/15.
 */
public class AggregateSteppable implements Steppable {

    private static final long serialVersionUID = 1437081239429619037L;
    List<Steppable> steppableList = new LinkedList<>();


    @Override
    public void step(final SimState simState) {
        for (final Steppable steppable : steppableList)
            steppable.step(simState);
    }


    public Stoppable add(final Steppable steppable) {
        steppableList.add(steppable);
        return () -> remove(steppable);
    }

    /**
     *
     */
    public boolean remove(final Steppable o) {
        return steppableList.remove(o);
    }
}
