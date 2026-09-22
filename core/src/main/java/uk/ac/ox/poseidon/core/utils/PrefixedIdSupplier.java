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

package uk.ac.ox.poseidon.core.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * A {@link Supplier} of unique string ids, each the given prefix followed by an incrementing
 * counter (starting at 0). Built via {@link Factories#prefixedIdSupplier(String)} in this package.
 */
@RequiredArgsConstructor
public class PrefixedIdSupplier implements Supplier<String> {

    @Getter
    private final String prefix;

    private final AtomicLong counter;

    /**
     * @param prefix the prefix prepended to every generated id, with a fresh counter starting at 0
     */
    public PrefixedIdSupplier(final String prefix) {
        this.prefix = prefix;
        this.counter = new AtomicLong();
    }

    @Override
    public String get() {
        return prefix + counter.getAndIncrement();
    }
}
