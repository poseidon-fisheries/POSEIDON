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

package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.FishState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FixedDataLastStepTargetFromFile implements DataTarget {


    private static final long serialVersionUID = 6998271990016935372L;
    private final FixedDataLastStepTarget delegate = new FixedDataLastStepTarget();
    private String pathToCsvFile;


    public FixedDataLastStepTargetFromFile() {
    }

    @Override
    public double computeError(final FishState model) {


        try {
            final List<String> strings = Files.readAllLines(Paths.get(pathToCsvFile));
            delegate.setFixedTarget(Double.parseDouble(
                strings.get(strings.size() - 1)));

        } catch (final IOException e) {

            throw new RuntimeException("can't read " + pathToCsvFile + " because of " + e);
        }
        return delegate.computeError(model);
    }

    public String getPathToCsvFile() {
        return pathToCsvFile;
    }

    public void setPathToCsvFile(final String pathToCsvFile) {
        this.pathToCsvFile = pathToCsvFile;
    }

    public String getYearlyDataColumnName() {
        return delegate.getColumnName();
    }

    public void setYearlyDataColumnName(final String columnName) {
        delegate.setColumnName(columnName);
    }

    public double getExponent() {
        return delegate.getExponent();
    }

    public void setExponent(final double exponent) {
        delegate.setExponent(exponent);
    }
}
