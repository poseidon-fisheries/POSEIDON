/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.InactiveBehaviour;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.ForwardingEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

@AllArgsConstructor
public class Fleet {

    private final BiMap<String, Vessel> vesselsById = HashBiMap.create();
    private final TemporalSchedule schedule;
    private final EventManager eventManager;
    private final VesselField vesselField;

    @Getter
    private final PortGrid portGrid;

    @Getter
    private final MarketGrid marketGrid;

    public Optional<Vessel> getVessel(final String vesselId) {
        return Optional.ofNullable(vesselsById.get(vesselId));
    }

    public Set<Vessel> getVessels() {
        return Collections.unmodifiableSet(vesselsById.values());
    }

    public Vessel createVessel(
        final String vesselId
    ) {
        checkState(
            !vesselsById.containsKey(vesselId),
            "Vessel %s already exists", vesselId
        );
        final Vessel vessel = new Vessel(
            schedule,
            new ForwardingEventManager(eventManager),
            InactiveBehaviour.INACTIVE_BEHAVIOUR,
            vesselId,
            vesselField,
            portGrid,
            marketGrid
        );
        vessel.setAccount(new Account());
        vesselsById.put(vesselId, vessel);
        return vessel;
    }

}
