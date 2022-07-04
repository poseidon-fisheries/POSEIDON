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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;

public class LinearFishAbundanceAttractor
    extends FishAbundanceAttractor {

    protected LinearFishAbundanceAttractor(
        final Collection<Species> species,
        final AttractionProbabilityFunction<AbundanceLocalBiology, AbundanceFad> attractionProbabilityFunction,
        final double[] attractionRates,
        final MersenneTwisterFast rng,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters
    ) {
        super(species, attractionProbabilityFunction, rng, selectivityFilters, attractionRates);
    }

    @Override
    Entry<StructuredAbundance, Double> attractForSpecies(
        final Species species,
        final AbundanceLocalBiology cellBiology,
        final AbundanceFad fad
    ) {
        final NonMutatingArrayFilter selectivity = getSelectivityFilters().get(species);
        final StructuredAbundance fadAbundance = fad.getBiology().getAbundance(species);
        final StructuredAbundance cellAbundance = cellBiology.getAbundance(species);
        final double attractionRate = getAttractionRate(species);

        return fadAbundance.mapAndWeigh(species, (subDivision, bin) ->
            cellAbundance.getAbundance(subDivision, bin) *
                attractionRate *
                selectivity.getFilterValue(subDivision, bin)
        );
    }

}
