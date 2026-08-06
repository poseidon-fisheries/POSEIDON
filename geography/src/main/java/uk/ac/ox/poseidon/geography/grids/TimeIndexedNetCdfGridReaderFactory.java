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

package uk.ac.ox.poseidon.geography.grids;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * Resolves {@code ncFilePath} and the dimension names against scope once (cheap — no file I/O),
 * and returns a {@link Supplier} whose {@code get()} opens a fresh, independent
 * {@link TimeIndexedNetCdfGridReader} on every call. Memoizing the supplier (as every
 * {@code Factory} does) is safe because it holds no open resource itself; memoizing an open
 * reader instead would risk a second caller receiving an already-closed file handle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TimeIndexedNetCdfGridReaderFactory<S extends Scope>
    extends RelativeScopeFactory<S, Supplier<TimeIndexedNetCdfGridReader>> {

    private Factory<? super S, ? extends Path> ncFilePath;
    private String timeDimensionName;
    private String latitudeDimensionName;
    private String longitudeDimensionName;

    @Override
    protected Supplier<TimeIndexedNetCdfGridReader> newInstance(final S scope) {
        final Path resolvedPath = ncFilePath.get(scope);
        return () -> new TimeIndexedNetCdfGridReader(
            resolvedPath, timeDimensionName, latitudeDimensionName, longitudeDimensionName
        );
    }

}
