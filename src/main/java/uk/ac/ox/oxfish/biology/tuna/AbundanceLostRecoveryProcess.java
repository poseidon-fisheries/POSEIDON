/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.tuna;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

import java.util.Map.Entry;
import java.util.Optional;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.geography.fads.AbundanceLostObserver;
import uk.ac.ox.oxfish.model.FishState;

/**
 * This class puts back the abundance lost by FADs drifting out or losing their fish over
 * non-habitable areas.
 */
public class AbundanceLostRecoveryProcess implements BiologicalProcess<AbundanceLocalBiology> {

    @Override
    public Optional<AbundanceLocalBiology> process(
        final FishState fishState,
        final AbundanceLocalBiology aggregatedBiology
    ) {
        final AbundanceLostObserver abundanceLostObserver =
            fishState.getFadMap().getAbundanceLostObserver();
        final AbundanceLocalBiology newAggregatedAbundance = new AbundanceLocalBiology(
            aggregatedBiology
                .getAbundance()
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> abundanceLostObserver
                        .get(entry.getKey())
                        .add(entry.getValue())
                        .asMatrix()
                ))
        );
        abundanceLostObserver.clear();
        return Optional.of(newAggregatedAbundance);
    }
}
