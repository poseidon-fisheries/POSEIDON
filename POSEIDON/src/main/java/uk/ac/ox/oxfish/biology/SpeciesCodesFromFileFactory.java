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

package uk.ac.ox.oxfish.biology;

import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.nio.file.Path;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableBiMap.toImmutableBiMap;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

/**
 * Builds a SpeciesCodes map from a CSV file. The CSV file columns must be:
 * <ul>
 *  <li>{@code species_code}</li>
 *  <li>{@code species_name}</li>
 * </ul>
 */
public class SpeciesCodesFromFileFactory implements AlgorithmFactory<SpeciesCodes>, Supplier<SpeciesCodes> {

    private static final CacheByFile<SpeciesCodes> cache =
        new CacheByFile<>(SpeciesCodesFromFileFactory::getSpeciesCodes);
    private InputPath speciesCodeFile;

    @SuppressWarnings("unused")
    public SpeciesCodesFromFileFactory() {
    }

    public SpeciesCodesFromFileFactory(
        final InputPath speciesCodeFile
    ) {
        this.speciesCodeFile = speciesCodeFile;
    }

    private static SpeciesCodes getSpeciesCodes(final Path path) {
        return new SpeciesCodes(
            recordStream(path).collect(toImmutableBiMap(
                r -> r.getString("species_code"),
                r -> r.getString("species_name")
            ))
        );
    }

    @SuppressWarnings("unused")
    public InputPath getSpeciesCodeFile() {
        return speciesCodeFile;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodeFile(final InputPath speciesCodeFile) {
        this.speciesCodeFile = speciesCodeFile;
    }

    @Override
    public SpeciesCodes apply(final FishState fishState) {
        return get();
    }

    @Override
    public SpeciesCodes get() {
        return cache.apply(speciesCodeFile.get());
    }
}
