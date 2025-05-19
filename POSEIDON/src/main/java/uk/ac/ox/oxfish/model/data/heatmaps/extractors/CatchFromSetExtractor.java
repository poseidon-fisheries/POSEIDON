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

package uk.ac.ox.oxfish.model.data.heatmaps.extractors;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.monitors.observers.PurseSeinerActionObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

public final class CatchFromSetExtractor<A extends AbstractSetAction>
    extends PurseSeinerActionObserver<A>
    implements ToDoubleFunction<SeaTile> {

    private final Species species;
    private final Map<SeaTile, Double> catches = new HashMap<>();

    public CatchFromSetExtractor(final Class<A> observedClass, final Species species) {
        super(observedClass);
        this.species = species;
    }

    @Override
    public double applyAsDouble(final SeaTile seaTile) {
        return Optional.ofNullable(catches.remove(seaTile)).orElse(0.0);
    }

    @Override
    public void observe(final A setAction) {
        setAction.getCatchesKept().ifPresent(catchesKept ->
            catches.put(setAction.getLocation(), catchesKept.getWeightCaught(this.species))
        );
    }

}
