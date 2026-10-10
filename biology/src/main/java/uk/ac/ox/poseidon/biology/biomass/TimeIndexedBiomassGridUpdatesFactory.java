/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Map.entry;

/**
 * Turns the dated snapshots given by {@code timeIndexedBiomassGrids} into
 * {@link BiomassGridUpdate}s that fully replace {@code biomassGrids}' contents, species by
 * species, each dated at its snapshot's date-time — meant to replace a biological grower
 * entirely, not run alongside one. Nothing is scheduled here: pass the result to
 * {@link uk.ac.ox.poseidon.core.schedule.Factories#scheduledByDateTime(Factory)} for the
 * snapshots to apply over time. Built via
 * {@link Factories#timeIndexedBiomassGridUpdates(Factory, Factory)}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TimeIndexedBiomassGridUpdatesFactory<S extends Scope>
    extends RelativeScopeFactory<S, List<Entry<LocalDateTime, BiomassGridUpdate>>> {

    private Factory<? super S, ? extends FisheableBiomassGrids> biomassGrids;
    private Factory<? super S, ? extends Map<LocalDateTime, ? extends List<? extends SpeciesGrid>>>
        timeIndexedBiomassGrids;

    @Override
    protected List<Entry<LocalDateTime, BiomassGridUpdate>> newInstance(
        final S scope
    ) {
        final FisheableBiomassGrids target = biomassGrids.get(scope);
        return timeIndexedBiomassGrids
            .get(scope)
            .entrySet()
            .stream()
            .map(snapshot -> entry(
                snapshot.getKey(),
                new BiomassGridUpdate(snapshot.getValue(), target)
            ))
            .collect(toImmutableList());
    }

}
