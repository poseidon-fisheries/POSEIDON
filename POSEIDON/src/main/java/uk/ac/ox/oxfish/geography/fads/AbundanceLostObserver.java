/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.ImmutableAbundance;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceLostEvent;
import uk.ac.ox.poseidon.common.api.Observer;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class AbundanceLostObserver implements Observer<AbundanceLostEvent> {

    private final Map<Species, ImmutableAbundance> abundanceLost = new HashMap<>();

    @Override
    public void observe(final AbundanceLostEvent abundanceLostEvent) {
        abundanceLostEvent
            .getAbundanceLost()
            .forEach((species, abundance) ->
                abundanceLost.put(species, get(species).add(abundance))
            );
    }

    public ImmutableAbundance get(final Species species) {
        return abundanceLost.computeIfAbsent(species, ImmutableAbundance::empty);
    }

    public AbundanceLocalBiology asBiology(final GlobalBiology globalBiology) {
        return new AbundanceLocalBiology(
            globalBiology.getSpecies().stream().collect(toImmutableMap(
                identity(),
                species -> get(species).asMatrix()
            ))
        );
    }

    public void clear() {
        abundanceLost.clear();
    }

}
