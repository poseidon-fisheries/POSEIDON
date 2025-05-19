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

package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;

/**
 * Whenever the target policy is 1 or above, make sure all the limits on new entrants are removed.
 * Viceversa when policy is below 1, make sure no new entrants are allowed
 */
public class CloseReopenOnEffortDecorator implements Actuator<FishState, Double> {


    private final Actuator<FishState, Double> delegate;

    public CloseReopenOnEffortDecorator(Actuator<FishState, Double> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void apply(FishState subject, Double policy, FishState model) {

        if (policy >= 1)
            for (EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(false);
            }
        if (policy < 1)
            for (EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(true);
            }

        delegate.apply(subject, policy, model);
    }
}
