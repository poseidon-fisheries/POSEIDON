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

package uk.ac.ox.poseidon.fishing.tasks.travel;

import static com.google.common.base.Preconditions.checkState;

import java.time.Duration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.fuel.FuelStation;
import uk.ac.ox.poseidon.fishing.fuel.FuelStationGrid;
import uk.ac.ox.poseidon.fishing.tasks.ExtendedTripTask;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.tanks.FuelTank;

/**
 * An {@link ExtendedTripTask} that fills the vessel's tank at the fuel station
 * of the port it is docked at, running for that purchase's duration and
 * charging its price to the trip account.
 */
@RequiredArgsConstructor
public class Refuel extends ExtendedTripTask {

  private final @NonNull VesselComponent<? extends FuelTank> fuelTank;
  private final @NonNull FuelStationGrid fuelStationGrid;
  private Duration duration;

  /**
   * Buys fuel from the station of the port the vessel is docked at, charging
   * its price to the trip account and recording its duration.
   *
   * @throws IllegalStateException if the vessel is not docked, or its port
   *                               does not have exactly one station
   */
  @Override
  public void start() {
    super.start();
    final FuelStation.Purchase purchase = fuelStationFor(getVessel()).refill(
      fuelTank.of(getVessel())
    );
    duration = purchase.getDuration();
    getTrip().getAccount().subtract(purchase.getPrice());
  }

  /**
   * @return the one fuel station of the port the vessel is docked at
   * @throws IllegalStateException if the vessel is not docked, or its port
   *                               does not have exactly one station
   */
  private FuelStation fuelStationFor(final Vessel vessel) {
    checkState(
      vessel.isDocked(),
      "Vessel %s is refuelling while not docked.",
      vessel.getId()
    );
    return fuelStationGrid.getFuelStation(vessel.getPortDockedAt());
  }

  /** @return the purchase's duration, computed at {@link #start()} */
  @Override
  protected Duration getDuration() {
    return duration;
  }

  /** @return always {@code SUCCEEDED} */
  @Override
  protected Status complete() {
    return Status.SUCCEEDED;
  }
}
