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

package uk.ac.ox.poseidon.agents.behaviours.fishing;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
import uk.ac.ox.poseidon.agents.behaviours.SteppableAction;
import uk.ac.ox.poseidon.agents.behaviours.disposition.Disposition;
import uk.ac.ox.poseidon.agents.behaviours.disposition.DispositionProcess;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.FishingGear;
import uk.ac.ox.poseidon.agents.vessels.hold.Hold;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;
import uk.ac.ox.poseidon.biology.Fisheable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Supplier;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Fishing<C extends Content<C>> implements Behaviour {

    @NonNull protected final FishingGear<C> fishingGear;
    @NonNull protected final Hold<C> hold;
    @NonNull private final Supplier<Fisheable<C>> fisheableSupplier;
    @NonNull private final Regulations regulations;
    @NonNull private final DispositionProcess<C> dispositionProcess;

    @Override
    public SteppableAction nextAction(
        final Vessel vessel,
        final LocalDateTime dateTime
    ) {
        final var action = new Action(vessel, dateTime, fishingGear.getDurationSupplier().get());
        return regulations.isPermitted(action) ? action : null;
    }

    @Getter
    @ToString(callSuper = true)
    public class Action extends SteppableAction implements FishingAction {

        private Bucket<C> grossCatch;
        private Disposition<C> disposition;

        public Action(
            final Vessel vessel,
            final LocalDateTime start,
            final Duration duration
        ) {
            super(vessel, start, duration);
        }

        @Override
        protected void complete(
            final LocalDateTime dateTime
        ) {
            final Fisheable<C> fisheable = fisheableSupplier.get();
            grossCatch = fishingGear.fish(fisheable);
            disposition = dispositionProcess.partition(
                grossCatch,
                hold.getAvailableCapacityInKg()
            );
            hold.addContent(disposition.getRetained());
            fisheable.release(disposition.getDiscardedAlive());
            vessel.popBehaviour();
        }

    }

}
