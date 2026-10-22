/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.tasks.fishing;

import static com.badlogic.gdx.ai.btree.Task.Status.SUCCEEDED;
import static com.google.common.base.Preconditions.checkState;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.fishing.catches.disposition.Disposition;
import uk.ac.ox.poseidon.fishing.catches.disposition.DispositionProcess;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.ExtendedTripTask;
import uk.ac.ox.poseidon.fishing.vessels.FishingTrip;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.fishing.vessels.holds.Hold;
import uk.ac.ox.poseidon.fishing.vessels.tanks.FuelTank;

/**
 * An {@link ExtendedTripTask} that fishes a single {@link Fisheable} using the
 * vessel's gear: runs for the gear's own duration, then sorts the catch via
 * {@code dispositionProcess}, adds the retained portion to the hold, releases
 * what's discarded alive back to the fisheable, broadcasts a
 * {@link FishingEvent}, and takes the fuel the gear burns per hour of fishing
 * from the tank.
 */
@RequiredArgsConstructor
public class Fishing extends ExtendedTripTask {

  @NonNull
  private final VesselComponent<? extends Gear> gear;

  @NonNull
  private final VesselComponent<? extends Hold> hold;

  @NonNull
  private final VesselComponent<? extends FuelTank> fuelTank;

  @NonNull
  private final Function<? super Vessel, Fisheable> fisheable;

  @NonNull
  private final DispositionProcess dispositionProcess;

  /** The clock, which dates the start of each fishing action. */
  @NonNull
  private final Function<? super Vessel, LocalDateTime> currentDateTime;

  private ExtendedFishingAction action;

  /**
   * Starts a new {@link ExtendedFishingAction} for the current vessel, cell and
   * gear.
   *
   * @throws IllegalStateException if the vessel is not on a
   *                               {@link FishingTrip}, which records the
   *                               fishing events
   */
  @Override
  public void start() {
    super.start();
    checkState(
      getTrip() instanceof FishingTrip,
      "Vessel %s is fishing while not on a fishing trip.",
      getVessel().getId()
    );
    action = new ExtendedFishingAction(
      getVessel(),
      currentDateTime.apply(getVessel()),
      gear.of(getVessel())
    );
  }

  /**
   * @return {@link #action}'s duration (fixed for the life of this fishing
   *         event)
   */
  @Override
  protected Duration getDuration() {
    return action.getDuration();
  }

  /**
   * Fishes the fisheable {@link #fisheable} supplies, sorts the catch
   * via {@link #dispositionProcess}, retains what fits in the hold, releases
   * the rest alive back to the fisheable, broadcasts a {@link FishingEvent},
   * and consumes fuel for the elapsed duration.
   *
   * @return always {@code SUCCEEDED}
   */
  @Override
  protected Status complete() {
    final Fisheable fisheable = this.fisheable.apply(getVessel());
    final Gear gear = this.gear.of(getVessel());
    final Bucket grossCatch = gear.fish(fisheable);
    final Hold hold = this.hold.of(getVessel());
    final Disposition disposition = dispositionProcess.partition(
      grossCatch,
      hold.getAvailableCapacityInKg()
    );
    hold.addContent(disposition.getRetained());
    fisheable.release(disposition.getDiscardedAlive());
    getTrip()
      .getEventManager()
      .broadcast(
        new FishingEvent(action, new FishingOutcome(grossCatch, disposition))
      );
    final double fuelPerHour = gear.getLitresOfFuelConsumedPerHourOfFishing();
    if (fuelPerHour > 0) {
      final double hours = getDuration().toSeconds() / 3600.0;
      fuelTank.of(getVessel()).consumeFuel(fuelPerHour * hours);
    }
    return SUCCEEDED;
  }

  /** Clears {@link #action}, so the next run starts a fresh fishing action. */
  @Override
  public void resetTask() {
    action = null;
  }
}
