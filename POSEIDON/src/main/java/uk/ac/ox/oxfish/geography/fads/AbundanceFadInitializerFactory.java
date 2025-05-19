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

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.CarryingCapacitySupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbundanceFadInitializerFactory
    extends FadInitializerFactory<AbundanceLocalBiology, AbundanceAggregatingFad> {

    private AbundanceFiltersFactory abundanceFilters;

    public AbundanceFadInitializerFactory(
        final AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier,
        final Map<String, DoubleParameter> catchabilities,
        final Map<String, DoubleParameter> fishReleaseProbabilities,
        final DoubleParameter daysInWaterBeforeAttraction,
        final AbundanceFiltersFactory abundanceFilters
    ) {
        super(
            carryingCapacitySupplier,
            catchabilities,
            fishReleaseProbabilities,
            daysInWaterBeforeAttraction
        );
        this.abundanceFilters = checkNotNull(abundanceFilters);
    }

    public AbundanceFadInitializerFactory() {
    }

    @SuppressWarnings("WeakerAccess")
    public AbundanceFiltersFactory getAbundanceFilters() {
        return abundanceFilters;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFilters(final AbundanceFiltersFactory abundanceFilters) {
        invalidateCache();
        this.abundanceFilters = checkNotNull(abundanceFilters);
    }

}
