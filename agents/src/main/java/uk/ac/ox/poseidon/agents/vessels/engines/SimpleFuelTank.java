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

package uk.ac.ox.poseidon.agents.vessels.engines;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@ToString
@EqualsAndHashCode
public class SimpleFuelTank implements FuelTank {

    private static final double TOLERANCE_IN_LITRES = 1e-6;

    private final double capacityInLitres;
    private double currentFuelInLitres;

    public SimpleFuelTank(
        final double capacityInLitres,
        final double currentFuelInLitres
    ) {
        checkArgument(capacityInLitres >= 0, "capacityInLitres must be >= 0");
        checkArgument(currentFuelInLitres >= 0, "currentFuelInLitres must be >= 0");
        checkArgument(
            currentFuelInLitres <= capacityInLitres,
            "currentFuelInLitres (%s) must be <= capacityInLitres (%s)".formatted(
                currentFuelInLitres, capacityInLitres
            )
        );
        this.capacityInLitres = capacityInLitres;
        this.currentFuelInLitres = currentFuelInLitres;
    }

    @Override
    public void addFuel(final double litres) {
        checkArgument(litres >= 0, "litres must be >= 0");
        final double newFuelLevel = currentFuelInLitres + litres;
        if (newFuelLevel <= capacityInLitres) {
            currentFuelInLitres = newFuelLevel;
        } else {
            checkArgument(
                newFuelLevel <= capacityInLitres + TOLERANCE_IN_LITRES,
                ("adding %s litres of fuel to the current amount of %s " +
                    "would exceed tank capacity of %s").formatted(
                    litres, currentFuelInLitres, capacityInLitres
                )
            );
            currentFuelInLitres = capacityInLitres;
        }
    }

    @Override
    public void consumeFuel(final double litres) {
        checkArgument(litres >= 0, "litres must be >= 0");
        final double newFuelLevel = currentFuelInLitres - litres;
        if (newFuelLevel >= 0) {
            currentFuelInLitres = newFuelLevel;
        } else {
            checkArgument(
                newFuelLevel >= -TOLERANCE_IN_LITRES,
                "cannot consume %s litres with only %s available".formatted(
                    litres,
                    currentFuelInLitres
                )
            );
            currentFuelInLitres = 0;
        }
    }
}
