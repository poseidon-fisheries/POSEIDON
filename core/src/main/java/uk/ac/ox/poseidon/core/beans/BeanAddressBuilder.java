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

package uk.ac.ox.poseidon.core.beans;

public class BeanAddressBuilder implements AddressBuilder {

    private static final char NESTED = '.';
    private static final char MAPPED_START = '(';
    private static final char MAPPED_END = ')';
    private static final char INDEXED_START = '[';
    private static final char INDEXED_END = ']';
    private final String address;

    public BeanAddressBuilder() {
        this("");
    }

    public BeanAddressBuilder(final String address) {
        this.address = address;
    }

    @Override
    public AddressBuilder add(final String element) {
        return address.isEmpty()
            ? new BeanAddressBuilder(element)
            : new BeanAddressBuilder(address + NESTED + element);
    }

    @Override
    public AddressBuilder addKey(final String key) {
        if (address.isEmpty()) {
            throw new IllegalStateException("Cannot add key to empty address builder.");
        } else {
            return new BeanAddressBuilder(address + MAPPED_START + key + MAPPED_END);
        }
    }

    @Override
    public AddressBuilder addIndex(final long index) {
        if (address.isEmpty()) {
            throw new IllegalStateException("Cannot add index to empty address builder.");
        } else {
            return new BeanAddressBuilder(address + INDEXED_START + index + INDEXED_END);
        }
    }

    @Override
    public String get() {
        return address;
    }
}
