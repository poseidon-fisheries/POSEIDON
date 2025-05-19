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

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.ImmutableDoubleArray;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingProportionFilter;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class AbundanceCatchSampler extends CatchSampler<AbundanceLocalBiology> {

    private final Map<Species, NonMutatingArrayFilter> selectivityFilters;

    public AbundanceCatchSampler(
        final Collection<Collection<Double>> sample,
        final MersenneTwisterFast rng,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters
    ) {
        super(sample, rng);
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    boolean test(
        final AbundanceLocalBiology sourceBiology,
        final ImmutableDoubleArray catchArray
    ) {
        // Build an abundance object representing the maximum abundance that we
        // could catch given the source biology and the selectivity filters...
        final AbundanceLocalBiology catchableAbundance =
            filterAbundance(sourceBiology, selectivityFilters);

        // ...and then check that this maximum abundance contains enough biomass
        // to satisfy the desired catch for all species.
        boolean isThereEnough = species().allMatch(species ->
            catchArray.get(species.getIndex()) <= catchableAbundance.getBiomass(
                species)
        );
        return isThereEnough;
    }

    private static AbundanceLocalBiology filterAbundance(
        final LocalBiology biology,
        final Map<Species, NonMutatingArrayFilter> filters
    ) {
        return new AbundanceLocalBiology(
            filters
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> entry.getValue().filter(
                        entry.getKey(),
                        biology.getAbundance(entry.getKey()).asMatrix()
                    )
                ))
        );
    }

    private Stream<Species> species() {
        return selectivityFilters.keySet().stream();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public AbundanceLocalBiology apply(final AbundanceLocalBiology sourceBiology) {

        final ImmutableDoubleArray desiredCatch = next(sourceBiology);

        final AbundanceLocalBiology catchableAbundance =
            filterAbundance(sourceBiology, selectivityFilters);

        return new AbundanceLocalBiology(
            species().collect(toImmutableMap(
                Function.identity(),
                species -> {
                    double proportion = desiredCatch.get(species.getIndex()) /
                        catchableAbundance.getBiomass(species);
                    if (Double.isNaN(proportion))
                        proportion = 0;
                    return new NonMutatingProportionFilter(
                        proportion
                    ).filter(species, catchableAbundance.getAbundance(species).asMatrix());
                }
            ))
        );
    }
}
