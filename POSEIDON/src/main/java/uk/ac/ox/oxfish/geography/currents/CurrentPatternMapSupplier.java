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
package uk.ac.ox.oxfish.geography.currents;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class CurrentPatternMapSupplier implements Supplier<Map<CurrentPattern, Path>> {

    public static final CurrentPatternMapSupplier EMPTY =
        new CurrentPatternMapSupplier(null, ImmutableMap.of());

    private InputPath inputPath;
    private Map<String, Path> currentFiles;

    @SuppressWarnings("unused")
    public CurrentPatternMapSupplier() {
    }

    public CurrentPatternMapSupplier(
        final InputPath inputPath,
        final Map<String, Path> currentFiles
    ) {
        this.inputPath = inputPath;
        this.currentFiles = currentFiles;
    }

    @SuppressWarnings("unused")
    public InputPath getInputFolder() {
        return inputPath;
    }

    @SuppressWarnings("unused")
    public void setInputFolder(final InputPath inputPath) {
        this.inputPath = inputPath;
    }

    public Map<String, Path> getCurrentFiles() {
        return currentFiles;
    }

    public void setCurrentFiles(final Map<String, Path> currentFiles) {
        this.currentFiles = currentFiles;
    }

    @Override
    public Map<CurrentPattern, Path> get() {
        return currentFiles.entrySet().stream().collect(toImmutableMap(
            entry -> CurrentPattern.valueOf(entry.getKey()),
            entry -> inputPath.get().resolve(entry.getValue())
        ));
    }
}
