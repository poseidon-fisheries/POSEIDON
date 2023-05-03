/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.Supplier;

abstract class ReallocatorFactory<B extends LocalBiology, T extends Reallocator<B>>
    implements AlgorithmFactory<T> {

    private MapExtent mapExtent;
    private Supplier<SpeciesCodes> speciesCodesSupplier;
    private InputPath biomassDistributionsFile;
    private Integer period;

    /**
     * Empty constructor to make instantiable from YAML.
     */
    public ReallocatorFactory() {
    }

    public ReallocatorFactory(
        final InputPath biomassDistributionsFile,
        final Integer period,
        final Supplier<SpeciesCodes> speciesCodesSupplier
    ) {
        this.biomassDistributionsFile = biomassDistributionsFile;
        this.period = period;
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    public Supplier<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    public void setSpeciesCodesSupplier(final Supplier<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    public Integer getPeriod() {
        return period;
    }

    @SuppressWarnings("unused")
    public void setPeriod(final Integer period) {
        this.period = period;
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

    public void setMapExtent(final MapExtent mapExtent) {
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
