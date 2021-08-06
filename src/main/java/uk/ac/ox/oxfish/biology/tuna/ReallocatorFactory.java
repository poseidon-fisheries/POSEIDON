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

import java.nio.file.Path;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

abstract class ReallocatorFactory<T extends Reallocator<?, ?>>
    implements AlgorithmFactory<T> {

    private MapExtent mapExtent;
    private SpeciesCodes speciesCodes;
    private Path biomassDistributionsFilePath;
    private Integer period;

    /**
     * Empty constructor to make instantiable from YAML.
     */
    ReallocatorFactory() {
    }

    ReallocatorFactory(
        final Path biomassDistributionsFilePath,
        final Integer period
    ) {
        this.biomassDistributionsFilePath = biomassDistributionsFilePath;
        this.period = period;
    }

    SpeciesCodes getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    Integer getPeriod() {
        return period;
    }

    public void setPeriod(final Integer period) {
        this.period = period;
    }

    MapExtent getMapExtent() {
        return mapExtent;
    }

    public void setMapExtent(final MapExtent mapExtent) {
        this.mapExtent = mapExtent;
    }

    @SuppressWarnings("WeakerAccess")
    public Path getBiomassDistributionsFilePath() {
        return biomassDistributionsFilePath;
    }

    @SuppressWarnings("unused")
    public void setBiomassDistributionsFilePath(final Path biomassDistributionsFilePath) {
        this.biomassDistributionsFilePath = biomassDistributionsFilePath;
    }

}
