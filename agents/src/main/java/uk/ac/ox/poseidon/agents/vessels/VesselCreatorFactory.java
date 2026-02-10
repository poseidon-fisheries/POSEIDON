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

package uk.ac.ox.poseidon.agents.vessels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VesselCreatorFactory extends SimulationScopeFactory<VesselCreator> {

    private Factory<? super SimulationScope, ? extends VesselField> vesselField;
    private Factory<? super SimulationScope, ? extends PortGrid> portGrid;
    private Factory<? super SimulationScope, ? extends MarketGrid> marketGrid;
    private Factory<? super SimulationScope, ? extends Supplier<String>> vesselIdSupplier;

    private Factory<? super VesselScope, ? extends String> name;
    private Factory<? super VesselScope, ? extends Account> account;
    private Factory<? super VesselScope, ? extends Port> homePort;
    private Factory<? super VesselScope, ? extends Hold> hold;
    private Factory<? super VesselScope, ? extends Gear> gear;
    private Factory<? super VesselScope, ? extends Engine> engine;
    private Factory<? super VesselScope, ? extends Behaviour> behaviour;

    private int numberOfVesselsToCreate;

    @Override
    protected VesselCreator newInstance(final SimulationScope scope) {
        return new VesselCreator(
            scope.getSimulation().getEventManager(),
            checkNotNull(vesselField).get(scope),
            checkNotNull(portGrid).get(scope),
            checkNotNull(marketGrid).get(scope),
            checkNotNull(vesselIdSupplier).get(scope),
            checkNotNull(name),
            checkNotNull(account),
            checkNotNull(homePort),
            checkNotNull(hold),
            checkNotNull(gear),
            checkNotNull(engine),
            checkNotNull(behaviour),
            numberOfVesselsToCreate
        );
    }
}
