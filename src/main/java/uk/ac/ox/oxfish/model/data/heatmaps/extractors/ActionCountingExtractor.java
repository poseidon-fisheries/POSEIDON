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

package uk.ac.ox.oxfish.model.data.heatmaps.extractors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.monitors.observers.PurseSeinerActionObserver;

import java.util.function.ToDoubleFunction;

public final class ActionCountingExtractor<A extends PurseSeinerAction>
    implements PurseSeinerActionObserver<A>, ToDoubleFunction<SeaTile> {

    private final Class<A> observedClass;

    private final Multiset<SeaTile> actionsPerTile = HashMultiset.create();

    public ActionCountingExtractor(final Class<A> observedClass) {
        this.observedClass = observedClass;
    }

    /**
     * Returns the number of times an action was observed for the tile and resets the count to zero for that tile.
     */
    @Override public double applyAsDouble(final SeaTile seaTile) {
        return actionsPerTile.setCount(seaTile, 0);
    }

    @Override public void observe(final A action) {
        actionsPerTile.add(action.getLocation());
    }

    @Override public Class<A> getObservedClass() { return observedClass; }

}
