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
import static java.lang.Math.min;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.ImmutableDoubleArray;
import ec.util.MersenneTwisterFast;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;

public class LogisticFishAbundanceAttractor
    extends LogisticFishAttractor<StructuredAbundance, AbundanceLocalBiology, AbundanceFad> {

    private final Map<Species, NonMutatingArrayFilter> selectivityFilters;

    public LogisticFishAbundanceAttractor(
        final MersenneTwisterFast rng,
        final Map<Species, Double> compressionExponents,
        final Map<Species, Double> attractableBiomassCoefficients,
        final Map<Species, Double> biomassInteractionCoefficients,
        final Map<Species, Double> attractionRates,
        final Map<Species, NonMutatingArrayFilter> selectivityFilters
    ) {
        super(
            rng,
            compressionExponents,
            attractableBiomassCoefficients,
            biomassInteractionCoefficients,
            attractionRates
        );
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    StructuredAbundance attractForSpecies(
        final Species s, final AbundanceLocalBiology cellBiology, final AbundanceFad fad
    ) {
        final List<ImmutableDoubleArray> selectivity = selectivityFilters.get(s).getFilters();
        final StructuredAbundance fadAbundance = fad.getBiology().getAbundance(s);
        final StructuredAbundance cellAbundance = cellBiology.getAbundance(s);

        final double space = 1 - fad.getBiology().getBiomass(s) / fad.getTotalCarryingCapacity();

        return fadAbundance.mapIndices((subDivision, bin) -> min(
            cellAbundance.getAbundance(subDivision, bin),
            getAttractionRates().get(s) *
                (selectivity.get(subDivision).get(bin) +
                    fadAbundance.getAbundance(subDivision, bin)) * space
        ));
    }

    @Override
    StructuredAbundance attractNothing(final Species s, final AbundanceFad fad) {
        return fad.getBiology().getAbundance(s).mapIndices((sub, bin) -> 0.0);
    }

    @Override
    AbundanceLocalBiology scale(
        final Map<Species, StructuredAbundance> attractedFish,
        final AbundanceFad fad
    ) {

        final double attractedBiomass = attractedFish.entrySet()
            .stream()
            .mapToDouble(entry -> entry.getValue().computeWeight(entry.getKey()))
            .sum();

        final double scalingFactor = biomassScalingFactor(
            attractedBiomass,
            fad.getBiology().getTotalBiomass(),
            fad.getTotalCarryingCapacity()
        );

        return new AbundanceLocalBiology(
            attractedFish.entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> entry.getValue().mapValues(a -> a * scalingFactor).asMatrix()
                ))
        );
    }

}
