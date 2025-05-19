/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import sim.util.Int2D;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;

import java.util.Map.Entry;
import java.util.Set;

public interface LocationValues extends FisherStartable {

    default double getValueAt(
        final int gridX,
        final int gridY
    ) {
        return getValueAt(new Int2D(gridX, gridY));
    }

    double getValueAt(Int2D location);

    Set<Entry<Int2D, Double>> getValues();

    Observers getObservers();

}
