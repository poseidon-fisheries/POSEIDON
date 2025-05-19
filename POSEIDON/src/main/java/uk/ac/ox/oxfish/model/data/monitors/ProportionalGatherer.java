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

package uk.ac.ox.oxfish.model.data.monitors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;

import javax.measure.Quantity;

import static tech.units.indriya.AbstractUnit.ONE;

public class ProportionalGatherer<G, O, V, Q extends Quantity<Q>> extends MonitorDecorator<O, V, Q> {

    private final GroupingMonitor<G, O, V, Q> delegate;

    public ProportionalGatherer(final GroupingMonitor<G, O, V, Q> delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public void registerWith(final TimeSeries<FishState> timeSeries) {
        delegate.getSubMonitors().values().forEach(subMonitor -> {
            if (subMonitor.getBaseName() != null)
                timeSeries.registerGatherer(
                    "Proportion of " + subMonitor.getBaseName(),
                    fishState ->
                        subMonitor.getAccumulator().applyAsDouble(fishState) /
                            getAccumulator().applyAsDouble(fishState),
                    0.0,
                    ONE,
                    "Proportion"
                );
        });
        super.registerWith(timeSeries);
    }

}
