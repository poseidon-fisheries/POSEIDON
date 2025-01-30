/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.behaviours.fishing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.SteppableAction;
import uk.ac.ox.poseidon.agents.behaviours.SteppableGridAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.FishingGear;
import uk.ac.ox.poseidon.agents.vessels.hold.Hold;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.Fisheable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@AllArgsConstructor
public class Fishing<C extends Content<C>> implements Behaviour {

    protected final FishingGear<C> fishingGear;
    protected final Hold<C> hold;
    private final Supplier<Fisheable<C>> fisheableSupplier;

    @Override
    public SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        return new Action(vessel, dateTime, fishingGear.nextDuration());
    }

    @Getter
    @ToString(callSuper = true)
    public class Action extends SteppableGridAction implements FishingAction {

        Bucket<C> fishCaught;

        public Action(
            final Vessel vessel,
            final LocalDateTime start,
            final Duration duration
        ) {
            super(vessel, start, duration, vessel.getCurrentCell());
        }

        @Override
        protected void complete(
            final LocalDateTime dateTime
        ) {
            fishCaught = fishingGear.fish(fisheableSupplier.get());
            hold.addContent(fishCaught);
            vessel.popBehaviour();
        }
    }

}
