/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.port;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.agents.behaviours.BehaviourFactory;
import uk.ac.ox.poseidon.agents.market.Market;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.agents.vessels.hold.Hold;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.Duration;
import java.util.function.Supplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LandingBehaviourFactory<C extends Content<C>> extends BehaviourFactory<Landing<C>> {

    private Factory<? extends MarketGrid<C, ? extends Market<C>>> marketGrid;
    private VesselScopeFactory<? extends Hold<C>> hold;
    private Factory<? extends Supplier<Duration>> durationSupplier;

    @Override
    protected Landing<C> newInstance(
        final Simulation simulation,
        final Vessel vessel
    ) {
        return new Landing<>(
            marketGrid.get(simulation),
            hold.get(simulation, vessel),
            durationSupplier.get(simulation)
        );
    }
}
