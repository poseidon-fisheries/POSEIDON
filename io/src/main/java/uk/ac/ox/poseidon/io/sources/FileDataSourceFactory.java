/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.io.sources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.charset.Charset;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FileDataSourceFactory<S extends Scope>
    extends RelativeScopeFactory<S, FileDataSource> {

    private static final String DEFAULT_ENCODING = UTF_8.name();

    private Factory<S, ? extends Path> path;
    private String encoding;

    public FileDataSourceFactory(final Factory<S, ? extends Path> path) {
        this.path = path;
        this.encoding = DEFAULT_ENCODING;
    }

    @Override
    protected FileDataSource newInstance(final S scope) {
        return new FileDataSource(path.get(scope).toFile(), Charset.forName(encoding));
    }

}
