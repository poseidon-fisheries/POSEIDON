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

import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.nio.charset.Charset;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FileDataSourceFactory extends GlobalScopeFactory<FileDataSource> {

    private Factory<Scope, ? extends Path> path;
    @Builder.Default private String encoding = UTF_8.name();

    public FileDataSourceFactory(final Factory<Scope, ? extends Path> path) {
        this.path = path;
    }

    @Override
    protected FileDataSource newInstance() {
        return new FileDataSource(path.get(GLOBAL_SCOPE).toFile(), Charset.forName(encoding));
    }

}
