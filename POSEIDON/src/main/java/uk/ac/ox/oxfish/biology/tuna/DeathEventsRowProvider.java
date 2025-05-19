/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.common.api.Observer;

import java.util.Collection;
import java.util.List;

public class DeathEventsRowProvider implements
    RowProvider,
    Observer<AbundanceMortalityProcess.DeathEvent> {

    private static final ImmutableList<String> HEADERS =
        ImmutableList.of("step", "species_code", "sub", "bin", "deaths");

    private final FishState fishState;
    private ImmutableList.Builder<ImmutableList<Object>> rows = new ImmutableList.Builder<>();

    @SuppressWarnings("unchecked")
    public DeathEventsRowProvider(final FishState fishState) {
        this.fishState = fishState;
        fishState
            .viewStartables()
            .stream()
            .filter(ScheduledBiologicalProcesses.class::isInstance)
            .map(ScheduledBiologicalProcesses.class::cast)
            .flatMap(scheduledBiologicalProcesses ->
                scheduledBiologicalProcesses
                    .getSchedule()
                    .values()
                    .stream()
                    .flatMap(processes -> ((Collection<?>) processes).stream())
            )
            .distinct()
            .filter(AbundanceMortalityProcess.class::isInstance)
            .forEach(process ->
                ((AbundanceMortalityProcess) process)
                    .getDeathEventsObservers()
                    .add(this)
            );
    }

    @Override
    public List<String> getHeaders() {
        return HEADERS;
    }

    @Override
    public Iterable<? extends List<?>> getRows() {
        final ImmutableList<ImmutableList<Object>> rows = this.rows.build();
        this.rows = new ImmutableList.Builder<>();
        return rows;
    }

    @Override
    public boolean isEveryStep() {
        return true;
    }

    @Override
    public void observe(final AbundanceMortalityProcess.DeathEvent deathEvent) {
        rows.add(ImmutableList.of(
            fishState.getStep(),
            deathEvent.species.getCode(),
            deathEvent.sub,
            deathEvent.bin,
            deathEvent.deaths
        ));
    }
}
