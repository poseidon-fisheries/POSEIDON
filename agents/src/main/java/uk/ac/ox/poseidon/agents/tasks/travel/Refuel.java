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

package uk.ac.ox.poseidon.agents.tasks.travel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.fuel.FuelStation;
import uk.ac.ox.poseidon.agents.fuel.FuelStationGrid;
import uk.ac.ox.poseidon.agents.tasks.ExtendedTripTask;

import java.time.Duration;

import static java.util.Comparator.comparing;

/**
 * An {@link ExtendedTripTask} that refuels the vessel at the cheapest fuel station in its
 * current cell, running for that purchase's duration and charging its price to the trip account.
 */
@RequiredArgsConstructor
public class Refuel extends ExtendedTripTask {

    private final @NonNull FuelStationGrid fuelStationGrid;
    private Duration duration;

    /**
     * Buys fuel from the cheapest-per-litre fuel station in the vessel's current cell, charging
     * its price to the trip account and recording its duration.
     *
     * @throws IllegalStateException if there's no fuel station in the vessel's current cell
     */
    @Override
    public void start() {
        super.start();
        final FuelStation.Purchase purchase = fuelStationGrid
            .getObjectsAt(getAgent().getCell())
            .min(comparing(FuelStation::getPricePerLitre))
            .map(fuelStation -> fuelStation.refill(getAgent().getEngine().getFuelTank()))
            .orElseThrow(() -> new IllegalStateException(
                "No fuel stations found in cell: " + getAgent().getCell())
            );
        duration = purchase.getDuration();
        getTrip().getAccount().subtract(purchase.getPrice());
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
