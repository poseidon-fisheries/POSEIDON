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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class MinimumSetValuesFromFileFactory implements AlgorithmFactory<MinimumSetValues> {
    private static final CacheByFile<MinimumSetValues> cache = new CacheByFile<>(path ->
        new MapBasedMinimumSetValues(
            recordStream(path)
                .collect(groupingBy(
                    record -> record.getInt("year"),
                    groupingBy(
                        record -> ActionClass.valueOf(record.getString("action_type")),
                        mapping(
                            record -> record.getDouble("value"),
                            collectingAndThen(toList(), objects -> objects.get(0))
                        )
                    )
                ))
        )
    );
    private InputPath file;

    @SuppressWarnings("unused")
    public MinimumSetValuesFromFileFactory() {
    }

    public MinimumSetValuesFromFileFactory(final InputPath file) {
        this.file = file;
    }

    public InputPath getFile() {
        return file;
    }

    public void setFile(final InputPath file) {
        this.file = file;
    }

    @Override
    public MinimumSetValues apply(final FishState fishState) {
        return cache.apply(file.get());
    }
}
