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

package uk.ac.ox.poseidon.io;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.io.paths.PathFactory;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.io.paths.Factories.path;

@SuppressFBWarnings("DMI")
class PathFactoryTest {

    private static final String FOLDERS = "/a/b/c/";
    private static final String FILENAME = "file.txt";

    @Test
    void newInstanceSimpleFile() {
        final PathFactory<Scope> pathFactory = path(FILENAME);
        assertEquals(Path.of(FILENAME), pathFactory.get(mock(Scope.class)));
    }

    @Test
    void newInstanceFileInFolder() {
        final PathFactory<Scope> pathFactory = path(FOLDERS + FILENAME);
        assertEquals(Path.of(FOLDERS + FILENAME), pathFactory.get(mock(Scope.class)));
    }

    @Test
    void newInstanceWithParent() {
        final PathFactory<Scope> pathFactory = path(FOLDERS).plus(FILENAME);
        assertEquals(Path.of(FOLDERS + FILENAME), pathFactory.get(mock(Scope.class)));
    }

}
