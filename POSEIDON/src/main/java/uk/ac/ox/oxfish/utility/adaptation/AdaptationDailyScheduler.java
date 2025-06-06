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

package uk.ac.ox.oxfish.utility.adaptation;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * A list of adaptation to step every x days
 * Created by carrknight on 8/10/15.
 */
public class AdaptationDailyScheduler implements FisherStartable, Steppable {


    private static final long serialVersionUID = 6907572869572067231L;
    private final List<Adaptation> adaptations = new LinkedList<>();

    private final int period;

    private FishState model;

    private Fisher agent;

    private Stoppable stoppable;

    public AdaptationDailyScheduler(final int period) {
        this.period = period;
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {

        this.model = model;
        this.agent = fisher;

        //if there is anything to "adapt"
        if (!adaptations.isEmpty()) {
            for (final Adaptation a : adaptations)
                a.start(model, fisher);

            stoppable = model.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE, period);

        }

    }


    /**
     * add an adaptation algorithm to the list. Start it if we have already started
     */
    public void registerAdaptation(final Adaptation adaptation) {

        adaptations.add(adaptation);
        if (model != null) {
            adaptation.start(model, agent);
            if (stoppable == null)
                stoppable = model.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE, period);
        }

    }


    public void removeAdaptation(final Adaptation adaptation) {
        adaptations.remove(adaptation);
    }

    @Override
    public void turnOff(final Fisher fisher) {
        if (stoppable != null)
            stoppable.stop();
        stoppable = null;
        adaptations.clear();
    }

    @Override
    public void step(final SimState simState) {
        if (adaptations.size() > 1)
            Collections.shuffle(adaptations, new Random(model.getRandom().nextLong()));
        for (final Adaptation a : adaptations)
            a.adapt(agent, ((FishState) simState), agent.grabRandomizer());
    }
}
