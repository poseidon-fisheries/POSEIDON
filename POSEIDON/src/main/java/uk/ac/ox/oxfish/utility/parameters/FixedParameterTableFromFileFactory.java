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

package uk.ac.ox.oxfish.utility.parameters;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class FixedParameterTableFromFileFactory implements AlgorithmFactory<FixedParameterTable> {
    private InputPath parameterFile;

    @SuppressWarnings("unused")
    public FixedParameterTableFromFileFactory() {
    }

    public FixedParameterTableFromFileFactory(final InputPath parameterFile) {
        this.parameterFile = parameterFile;
    }

    @SuppressWarnings("unused")
    public InputPath getParameterFile() {
        return parameterFile;
    }

    @SuppressWarnings("unused")
    public void setParameterFile(final InputPath parameterFile) {
        this.parameterFile = parameterFile;
    }

    @Override
    public FixedParameterTable apply(final FishState fishState) {
        return new FixedParameterTable(
            recordStream(parameterFile.get()).collect(toImmutableTable(
                r -> r.getInt("year"),
                r -> r.getString("name"),
                r -> new FixedDoubleParameter(r.getDouble("value"))
            ))
        );
    }
}
