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

package uk.ac.ox.oxfish.biology.events;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An unspecified event that is checked for every day. if it happens then it applies to some seatiles and it performs
 * some sort of operation
 * Created by carrknight on 10/7/16.
 */
public class BiologicalEvent implements Startable, Steppable {


    private static final long serialVersionUID = -8429759389362721433L;
    /**
     * this gets checked every step to see whether the event occurs today
     */
    private final Predicate<FishState> trigger;

    /**
     * this predicate can be used to bound the effect of the event to certain tiles only
     */
    private final Predicate<SeaTile> appliesHere;


    /**
     * this method applies the event to the seatile
     */
    private final Consumer<SeaTile> event;

    private Stoppable stoppable;


    public BiologicalEvent(
        final Predicate<FishState> trigger, final Predicate<SeaTile> appliesHere,
        final Consumer<SeaTile> event
    ) {
        this.trigger = trigger;
        this.appliesHere = appliesHere;
        this.event = event;
    }

    @Override
    public void step(final SimState state) {
        //if the trigger applies
        if (trigger.test(((FishState) state)))
            //for all the seatiles that qualify apply the event
            ((FishState) state).getMap().getAllSeaTilesExcludingLandAsList().stream().
                filter(appliesHere).forEach(event);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        stoppable = model.scheduleEveryDay(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (stoppable != null)
            stoppable.stop();
    }
}
