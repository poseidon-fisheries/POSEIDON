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

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;

public abstract class FishAbundanceAttractor extends
    AbstractFishAttractor<
        StructuredAbundance,
        AbundanceLocalBiology,
        AbundanceFad> {

    private final Map<Species, NonMutatingArrayFilter> selectivityFilters;

    FishAbundanceAttractor(
        final Collection<Species> species,
        final AttractionProbabilityFunction<AbundanceLocalBiology, AbundanceFad> attractionProbabilityFunction,
        final MersenneTwisterFast rng,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters,
        final double[] attractionRates
    ) {
        super(species, attractionProbabilityFunction, attractionRates, rng);
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }

    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        return selectivityFilters;
    }

    @Override
    StructuredAbundance attractNothing(final Species species) {
        return StructuredAbundance.empty(species);
    }

    @Override
    Entry<AbundanceLocalBiology, Double> scale(
        final Map<Species, Entry<StructuredAbundance, Double>> attractedFish,
        final AbundanceFad fad
    ) {

        final double attractedBiomass = attractedFish.values()
            .stream()
            .mapToDouble(Entry::getValue)
            .sum();

        final double originalBiomass = fad.getBiology().getTotalBiomass();
        final double scalingFactor = biomassScalingFactor(
            attractedBiomass,
            originalBiomass,
            fad.getTotalCarryingCapacity()
        );

        return entry(
            new AbundanceLocalBiology(
                attractedFish.entrySet()
                    .stream()
                    .collect(toImmutableMap(
                        Entry::getKey,
                        entry -> entry.getValue()
                            .getKey()
                            .mapValues(a -> a * scalingFactor)
                            .asMatrix()
                    ))
            ),
            attractedBiomass * scalingFactor
        );
    }

}
