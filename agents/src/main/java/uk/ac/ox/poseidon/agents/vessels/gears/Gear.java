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

package uk.ac.ox.poseidon.agents.vessels.gears;

import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;

import java.time.Duration;
import java.util.function.Supplier;

/** A vessel's fishing gear: how it catches, how long that takes, and what it costs in fuel. */
public interface Gear {

    /** @return this gear's identifying code */
    String getCode();

    /** @return litres of fuel burned per hour of fishing; {@code 0} unless overridden */
    default double getLitresOfFuelConsumedPerHourOfFishing() {
        return 0;
    }

    /** @return supplies how long one fishing event with this gear takes */
    Supplier<Duration> getDurationSupplier();

    /** @return what this gear catches from {@code fisheable}, removed from it in the process */
    Bucket fish(Fisheable fisheable);

    /** @return {@code true} if this gear can actually be fished with */
    boolean isActive();

}
