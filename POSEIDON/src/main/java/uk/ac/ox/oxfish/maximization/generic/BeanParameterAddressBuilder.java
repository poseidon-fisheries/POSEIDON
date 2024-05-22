/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.maximization.generic;

public class BeanParameterAddressBuilder implements ParameterAddressBuilder {

    private static final char NESTED = '.';
    private static final char MAPPED_START = '(';
    private static final char MAPPED_END = ')';
    private static final char INDEXED_START = '[';
    private static final char INDEXED_END = ']';
    private final String address;

    public BeanParameterAddressBuilder() {
        this("");
    }

    public BeanParameterAddressBuilder(final String address) {
        this.address = address;
    }

    @Override
    public ParameterAddressBuilder add(final String element) {
        return address.isEmpty()
            ? new BeanParameterAddressBuilder(element)
            : new BeanParameterAddressBuilder(address + NESTED + element);
    }

    @Override
    public ParameterAddressBuilder addKey(final String key) {
        if (address.isEmpty()) {
            throw new IllegalStateException("Cannot add key to empty parameter address builder.");
        } else {
            return new BeanParameterAddressBuilder(address + MAPPED_START + key + MAPPED_END);
        }
    }

    @Override
    public ParameterAddressBuilder addIndex(final long index) {
        if (address.isEmpty()) {
            throw new IllegalStateException("Cannot add index to empty parameter address builder.");
        } else {
            return new BeanParameterAddressBuilder(address + INDEXED_START + index + INDEXED_END);
        }
    }

    @Override
    public String get() {
        return address;
    }
}
