/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

abstract class ReallocatorFactory<B extends LocalBiology, T extends Reallocator<B>>
    implements AlgorithmFactory<T> {

    private InputPath biomassDistributionsFile;
    private IntegerParameter period;
    private ComponentFactory<MapExtent> mapExtent;

    public ReallocatorFactory(
        final InputPath biomassDistributionsFile,
        final IntegerParameter period,
        final ComponentFactory<MapExtent> mapExtent
    ) {
        this.biomassDistributionsFile = biomassDistributionsFile;
        this.period = period;
        this.mapExtent = mapExtent;
    }

    /**
     * Empty constructor to make instantiable from YAML.
     */
    public ReallocatorFactory() {
    }

    public IntegerParameter getPeriod() {
        return period;
    }

    public void setPeriod(final IntegerParameter period) {
        this.period = period;
    }

    public ComponentFactory<MapExtent> getMapExtent() {
        return mapExtent;
    }

    public void setMapExtent(final ComponentFactory<MapExtent> mapExtent) {
        this.mapExtent = mapExtent;
    }

    @SuppressWarnings("WeakerAccess")
    public InputPath getBiomassDistributionsFile() {
        return biomassDistributionsFile;
    }

    @SuppressWarnings("unused")
    public void setBiomassDistributionsFile(final InputPath biomassDistributionsFile) {
        this.biomassDistributionsFile = biomassDistributionsFile;
    }

}
