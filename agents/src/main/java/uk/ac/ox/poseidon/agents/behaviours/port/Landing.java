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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
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
            final List<? extends Market<C>> markets =
                marketGrid.getObjectsAt(vessel.getCell()).toList();
            if (markets.size() == 1) {
                final Sale<C> sale = markets
                    .getFirst()
                    .sell(vessel, hold.extractContent(), dateTime);
                sale.summary().values().forEach(vessel.getAccount()::add);
            } else {
                throw new RuntimeException(
                    "Expected one market at location " +
                        vessel.getCell() +
                        " but found " +
                        markets.size()
                );
            }
            getVessel().popBehaviour();
        }
    }
}
