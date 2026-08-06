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
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.stream.Collectors.groupingBy;

/**
 * Schedules the full replacement of {@code biomassGrids}' contents, species by species, with the
 * dated snapshots given by {@code timeIndexedBiomassGrids} — meant to replace a biological grower
 * entirely, not run alongside one. Unlike
 * {@link uk.ac.ox.poseidon.core.schedule.TemporalSchedule#scheduleByDateTime}, entries dated before
 * the simulation's effective start are not all replayed in order: since each update is a full
 * replacement rather than a delta, only the single most recent pre-start snapshot can affect the
 * final state, so earlier ones are dropped rather than scheduled.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TimeIndexedBiomassGridUpdatesFactory
    extends SimulationScopeFactory<FisheableBiomassGrids> {

    private Factory<? super SimulationScope, ? extends FisheableBiomassGrids> biomassGrids;
    private Factory<? super SimulationScope, ? extends Map<LocalDateTime, ? extends List<? extends SpeciesGrid>>>
        timeIndexedBiomassGrids;

    @Override
    protected FisheableBiomassGrids newInstance(final SimulationScope scope) {

        final FisheableBiomassGrids target = biomassGrids.get(scope);
        final Map<LocalDateTime, ? extends List<? extends SpeciesGrid>> timeIndexedBiomassGrids =
            this.timeIndexedBiomassGrids.get(scope);
        final TemporalSchedule schedule = scope.getSimulation().getTemporalSchedule();

        // Mirrors the minimumDateTime computation in TemporalSchedule.scheduleByDateTime, which
        // has no public accessor for it.
        final var minimumDateTime = schedule.getTime() < TemporalSchedule.EPOCH
            ? schedule.toDateTime(TemporalSchedule.EPOCH)
            : schedule.getDateTime();

        final Map<Boolean, List<Entry<LocalDateTime, ? extends List<? extends SpeciesGrid>>>>
            entriesBeforeAndAfter = timeIndexedBiomassGrids
            .entrySet()
            .stream()
            .collect(groupingBy(entry -> entry.getKey().isBefore(minimumDateTime)));

        // Before start: only the latest snapshot can affect the final state, so drop the rest.
        Optional.ofNullable(entriesBeforeAndAfter.get(true))
            .flatMap(entriesBefore -> entriesBefore.stream().max(
                (entryA, entryB) -> entryA.getKey().compareTo(entryB.getKey())
            ))
            .ifPresent(entry -> schedule.scheduleOnce(new BiomassGridUpdate(entry.getValue(), target)));

        // At/after start: schedule each individually, at its own date-time.
        Optional.ofNullable(entriesBeforeAndAfter.get(false))
            .ifPresent(entriesAfter -> entriesAfter.forEach(entry ->
                schedule.scheduleOnce(
                    entry.getKey(),
                    new BiomassGridUpdate(entry.getValue(), target)
                )
            ));

        return target;
    }

}
