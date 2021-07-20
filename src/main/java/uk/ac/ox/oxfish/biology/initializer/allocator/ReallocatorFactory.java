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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import java.nio.file.Path;

abstract class ReallocatorFactory {

    private Path speciesCodesFilePath;
    private Path biomassDistributionsFilePath;
    private int period;

    /**
     * Empty constructor to make instantiable from YAML.
     */
    ReallocatorFactory() {
    }

    ReallocatorFactory(
        final Path speciesCodesFilePath,
        final Path biomassDistributionsFilePath,
        final int period
    ) {
        this.speciesCodesFilePath = speciesCodesFilePath;
        this.biomassDistributionsFilePath = biomassDistributionsFilePath;
        this.period = period;
    }

    @SuppressWarnings("WeakerAccess")
    public Path getSpeciesCodesFilePath() {
        return speciesCodesFilePath;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodesFilePath(final Path speciesCodesFilePath) {
        this.speciesCodesFilePath = speciesCodesFilePath;
    }

    @SuppressWarnings("WeakerAccess")
    public Path getBiomassDistributionsFilePath() {
        return biomassDistributionsFilePath;
    }

    @SuppressWarnings("unused")
    public void setBiomassDistributionsFilePath(final Path biomassDistributionsFilePath) {
        this.biomassDistributionsFilePath = biomassDistributionsFilePath;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(final int period) {
        this.period = period;
    }
}
