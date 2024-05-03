/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
package uk.ac.ox.poseidon.r.adaptors.datasets;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayColumn<T> extends RColumn<T> {
    private final T[] array;

    public ArrayColumn(
        final String name,
        final T[] array
    ) {
        super(name);
        this.array = array;
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.stream(array).iterator();
    }

    @Override
    public Object toArray() {
        return array;
    }
}
