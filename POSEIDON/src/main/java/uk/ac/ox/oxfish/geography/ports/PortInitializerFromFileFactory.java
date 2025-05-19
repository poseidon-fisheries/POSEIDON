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

package uk.ac.ox.oxfish.geography.ports;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.function.Supplier;

public class PortInitializerFromFileFactory
    implements AlgorithmFactory<PortInitializer>, Supplier<PortInitializer> {

    private IntegerParameter targetYear;
    private InputPath portFile;

    public PortInitializerFromFileFactory() {
    }

    public PortInitializerFromFileFactory(
        final int targetYear,
        final InputPath portFile
    ) {
        this(new IntegerParameter(targetYear), portFile);
    }

    public PortInitializerFromFileFactory(
        final IntegerParameter targetYear,
        final InputPath portFile
    ) {
        this.targetYear = targetYear;
        this.portFile = portFile;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    public InputPath getPortFile() {
        return portFile;
    }

    public void setPortFile(final InputPath portFile) {
        this.portFile = portFile;
    }

    @Override
    public PortInitializer apply(final FishState fishState) {
        return get();
    }

    @Override
    public PortInitializer get() {
        return new PortInitializerFromFile(targetYear.getValue(), portFile.get());
    }
}
