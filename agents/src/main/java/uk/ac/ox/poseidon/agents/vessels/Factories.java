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

import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.tasks.Behaviour;
import uk.ac.ox.poseidon.agents.vessels.accounts.Account;
import uk.ac.ox.poseidon.agents.vessels.engines.Engine;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.ports.Port;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Factories for a vessel's scope, its fleet, and the events that create/update vessels. */
public class Factories {

    private Factories() {}

    /**
     * @param delegate the factory to resolve once per vessel
     * @return a {@link VesselScopeFactory} for {@code T}
     * @see PerVesselFactory
     */
    public static <T> PerVesselFactory<T> perVessel(
        final Factory<? super VesselScope, ? extends T> delegate
    ) {
        return new PerVesselFactory<>(delegate);
    }

    /**
     * @param vesselField the field vessels move within
     * @param portGrid    the fleet's ports
     * @param marketGrid  the fleet's markets
     * @return a {@link SimulationScopeFactory} for a {@link Fleet}
     * @see Fleet
     */
    public static FleetFactory fleet(
        final Factory<? super SimulationScope, ? extends VesselField> vesselField,
        final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
        final Factory<? super SimulationScope, ? extends MarketGrid> marketGrid
    ) {
        return new FleetFactory(vesselField, portGrid, marketGrid);
    }

    /**
     * @param prefix the prefix to prepend to the vessel's own id
     * @return a {@link VesselScopeFactory} for a simple vessel name
     * @see PrefixedIdFactory
     */
    public static PrefixedIdFactory prefixedId(final String prefix) {
        return new PrefixedIdFactory(prefix);
    }

    /**
     * @param fleet     the fleet the vessel belongs (or will belong) to
     * @param id        the vessel's id
     * @param name      the vessel's name
     * @param portCode  the vessel's home port code
     * @param tags      tags to set on the vessel
     * @param behaviour builds the vessel's behaviour
     * @param hold      builds the vessel's hold
     * @param gear      builds the vessel's gear
     * @param engine    builds the vessel's engine
     * @return a {@link SimulationScopeFactory} for a {@link FleetEvent.Type#ACTIVATION} event
     * @see VesselActivationFactory
     */
    public static VesselActivationFactory vesselActivation(
        final Factory<? super SimulationScope, ? extends Fleet> fleet,
        final String id,
        final String name,
        final String portCode,
        final Map<String, Factory<? super SimulationScope, ?>> tags,
        final Factory<? super VesselScope, ? extends Behaviour> behaviour,
        final Factory<? super VesselScope, ? extends Hold> hold,
        final Factory<? super VesselScope, ? extends Gear> gear,
        final Factory<? super VesselScope, ? extends Engine> engine
    ) {
        return new VesselActivationFactory(
            fleet, id, name, portCode, tags, behaviour, hold, gear, engine
        );
    }

    /**
     * @param vesselField             the field vessels move within
     * @param portGrid                the vessels' ports
     * @param marketGrid              the vessels' markets
     * @param vesselIdSupplier        supplies each new vessel's id
     * @param name                    builds each vessel's name
     * @param account                 builds each vessel's account
     * @param homePort                builds each vessel's home port
     * @param hold                    builds each vessel's hold
     * @param gear                    builds each vessel's gear
     * @param engine                  builds each vessel's engine
     * @param behaviour               builds each vessel's behaviour
     * @param extraFactories          extra per-vessel components to build
     * @param numberOfVesselsToCreate how many vessels to create
     * @return a {@link SimulationScopeFactory} for a {@link VesselCreator}
     * @see VesselCreator
     */
    public static VesselCreatorFactory vesselCreator(
        final Factory<? super SimulationScope, ? extends VesselField> vesselField,
        final Factory<? super SimulationScope, ? extends PortGrid> portGrid,
        final Factory<? super SimulationScope, ? extends MarketGrid> marketGrid,
        final Factory<? super SimulationScope, ? extends Supplier<String>> vesselIdSupplier,
        final Factory<? super VesselScope, ? extends String> name,
        final Factory<? super VesselScope, ? extends Account> account,
        final Factory<? super VesselScope, ? extends Port> homePort,
        final Factory<? super VesselScope, ? extends Hold> hold,
        final Factory<? super VesselScope, ? extends Gear> gear,
        final Factory<? super VesselScope, ? extends Engine> engine,
        final Factory<? super VesselScope, ? extends Behaviour> behaviour,
        final List<Factory<? super VesselScope, ?>> extraFactories,
        final int numberOfVesselsToCreate
    ) {
        return new VesselCreatorFactory(
            vesselField, portGrid, marketGrid, vesselIdSupplier,
            name, account, homePort, hold, gear, engine,
            behaviour, extraFactories, numberOfVesselsToCreate
        );
    }
}
