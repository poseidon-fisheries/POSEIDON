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
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.agents.behaviours.Behaviour;
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
public class DefaultFishingBehaviour<C extends Content<C>> implements Behaviour {

    protected final FishingGear<C> fishingGear;
    protected final Hold<C> hold;
    private final Supplier<Fisheable<C>> fisheableSupplier;
    private final Behaviour afterFishingBehaviour;

    @Override
    public Action newAction(
        final LocalDateTime dateTime,
        final Vessel vessel
    ) {
        return new Fishing(dateTime, fishingGear.nextDuration(), vessel);
    }

    @Getter
    @ToString(callSuper = true)
    public class Fishing extends FishingAction {

        Bucket<C> fishCaught;

        private Fishing(
            final LocalDateTime start,
            final Duration duration,
            final Vessel vessel
        ) {
            super(start, duration, vessel, vessel.getCurrentCell());
        }

        @Override
        public double getTotalBiomassCaught() {
            return fishCaught.getTotalBiomass().getValue();
        }

        @Override
        protected Action complete(
            final LocalDateTime dateTime
        ) {
            fishCaught = fishingGear.fish(fisheableSupplier.get());
            hold.store(fishCaught);
            return afterFishingBehaviour.newAction(dateTime, getVessel());
        }
    }

}
