/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import com.google.common.collect.ImmutableMap;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observers;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class PortLocationValues implements LocationValues {

    private final Observers observers = new Observers();
    private Map<Int2D, Double> values;

    @Override
    public double getValueAt(final Int2D location) {
        return checkNotNull(values).getOrDefault(location, 0.0);
    }

    @Override
    public Set<Map.Entry<Int2D, Double>> getValues() {
        return values.entrySet();
    }

    @Override
    public Observers getObservers() {
        return observers;
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        values = ImmutableMap.of(
            fisher.getHomePort().getLocation().getGridLocation(),
            1.0
        );
    }

}
