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

package uk.ac.ox.poseidon.agents.tasks.fishing;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.catches.disposition.Disposition;
import uk.ac.ox.poseidon.agents.catches.disposition.DispositionProcess;
import uk.ac.ox.poseidon.agents.regulations.FishingAction;
import uk.ac.ox.poseidon.agents.tasks.ExtendedTask;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Fisheable;

import java.time.Duration;
import java.util.function.Supplier;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;

@RequiredArgsConstructor
public class Fish extends ExtendedTask {

    @NonNull private final Supplier<Fisheable> fisheableSupplier;
    @NonNull private final DispositionProcess dispositionProcess;

    private FishingAction action;

    @Override
    public void start() {
        action = new FishingAction(getVessel());
    }

    @Override
    protected Duration getDuration() {
        return action.getDuration();
    }

    @Override
    protected Status complete() {
        final Fisheable fisheable = fisheableSupplier.get();
        final Bucket grossCatch = getVessel().getGear().fish(fisheable);
        final Hold hold = getVessel().getHold();
        final Disposition disposition =
            dispositionProcess.partition(grossCatch, hold.getAvailableCapacityInKg());
        hold.addContent(disposition.getRetained());
        fisheable.release(disposition.getDiscardedAlive());
        getVessel().getEventManager().broadcast(
            new FishingEvent(getVessel(), grossCatch, disposition)
        );
        return SUCCEEDED;
    }

    @Override
    public void resetTask() {
        action = null;
    }

}
