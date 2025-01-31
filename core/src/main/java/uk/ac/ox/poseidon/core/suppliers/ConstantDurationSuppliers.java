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

package uk.ac.ox.poseidon.core.suppliers;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.time.DurationFactory;
import uk.ac.ox.poseidon.core.utils.ConstantSupplierFactory;

import java.time.Duration;
import java.util.function.Supplier;

public class ConstantDurationSuppliers {
    public static final Factory<Supplier<Duration>> ONE_DAY_DURATION_SUPPLIER =
        new ConstantSupplierFactory<>(new DurationFactory(1, 0, 0, 0));
    public static final Factory<Supplier<Duration>> ONE_HOUR_DURATION_SUPPLIER =
        new ConstantSupplierFactory<>(new DurationFactory(1, 0, 0, 0));
}
