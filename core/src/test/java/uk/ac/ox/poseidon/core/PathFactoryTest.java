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
 *
 */

package uk.ac.ox.poseidon.core;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PathFactoryTest {

    private static final String FOLDERS = "/a/b/c/";
    private static final String FILENAME = "file.txt";

    @Test
    void newInstanceSimpleFile() {
        final PathFactory pathFactory = PathFactory.from(FILENAME);
        assertNull(pathFactory.getParent());
        assertEquals(ImmutableList.of(FILENAME), pathFactory.getPathElements());
        assertEquals(Path.of(FILENAME), pathFactory.newInstance(null));
    }

    @Test
    void newInstanceFileInFolder() {
        final PathFactory pathFactory = PathFactory.from(FOLDERS + FILENAME);
        assertNull(pathFactory.getParent());
        assertEquals(ImmutableList.of("/", "a", "b", "c", FILENAME), pathFactory.getPathElements());
        assertEquals(Path.of(FOLDERS + FILENAME), pathFactory.newInstance(null));
    }

    @Test
    void newInstanceWithParent() {
        final PathFactory pathFactory = PathFactory.from(PathFactory.from(FOLDERS), FILENAME);
        assertEquals(Path.of(FOLDERS), pathFactory.getParent().get(null));
        assertEquals(ImmutableList.of(FILENAME), pathFactory.getPathElements());
        assertEquals(Path.of(FOLDERS + FILENAME), pathFactory.newInstance(null));
    }

}
