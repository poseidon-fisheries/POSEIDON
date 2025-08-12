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

package uk.ac.ox.poseidon.agents.behaviours.port;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.SteppableAction;
import uk.ac.ox.poseidon.agents.market.Market;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.agents.market.Sale;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.hold.Hold;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Landing<C extends Content<C>> implements Behaviour {

    private final MarketGrid<C, ? extends Market<C>> marketGrid;
    private final Hold<C> hold;
    private final Supplier<Duration> durationSupplier;

    @Override
    public SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        return new Action(vessel, dateTime, durationSupplier.get());
    }

    @ToString(callSuper = true)
    private class Action extends SteppableAction {

        private Action(
            final Vessel vessel,
            final LocalDateTime start,
            final Duration duration
        ) {
            super(vessel, start, duration);
        }

        @Override
        public void complete(final LocalDateTime dateTime) {
            final Object destinationObject = checkNotNull(vessel.getDestination()).getObject();
            if (!(destinationObject instanceof final Port port)) {
                throw new RuntimeException(
                    "Vessel %s has arrived at destination %s which is not a port".formatted(
                        vessel.getId(),
                        destinationObject
                    )
                );
            } else {
                final List<? extends Market<C>> markets =
                    marketGrid.getObjectsAt(vessel.getCell()).toList();
                final Market<C> market = markets
                    .stream()
                    .filter(m -> m.getPort().equals(port))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException(
                        ("No market found for vessel %s in port %s at location %s. Markets there " +
                            "are: %s").formatted(
                            vessel.getId(),
                            port,
                            vessel.getCell(),
                            markets.stream().map(Market::getCode).toList()
                        )
                    ));
                final Sale<C> sale = market.sell(vessel, hold.extractContent(), dateTime);
                sale.summary().values().forEach(vessel.getAccount()::add);
                getVessel().popBehaviour();
            }
        }
    }
}
