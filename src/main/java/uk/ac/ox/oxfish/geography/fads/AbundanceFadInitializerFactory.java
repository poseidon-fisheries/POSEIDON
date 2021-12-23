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

package uk.ac.ox.oxfish.geography.fads;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.util.Map;
import java.util.function.Function;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LogisticFishAbundanceAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class AbundanceFadInitializerFactory
    extends FadInitializerFactory<AbundanceLocalBiology, AbundanceFad> {

    private Map<String, NonMutatingArrayFilter> selectivityFilters;

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        checkNotNull(selectivityFilters);
        checkNotNull(getSpeciesCodes());
        final MersenneTwisterFast rng = fishState.getRandom();
        final double totalCarryingCapacity = getTotalCarryingCapacity().apply(rng);
        return new AbundanceFadInitializer(
            fishState.getBiology(),
            totalCarryingCapacity,
            makeFishAttractor(fishState, rng, totalCarryingCapacity),
            getFishReleaseProbabilityInPercent().apply(rng) / 100d,
            fishState::getStep
        );
    }

    LogisticFishAbundanceAttractor makeFishAttractor(
        final FishState fishState,
        final MersenneTwisterFast rng,
        final double totalCarryingCapacity
    ) {

        return new LogisticFishAbundanceAttractor(
            fishState.getRandom(),
            processParameterMap(getCompressionExponents(), fishState.getBiology(), rng),
            processParameterMap(getAttractableBiomassCoefficients(), fishState.getBiology(), rng),
            processParameterMap(getBiomassInteractionsCoefficients(), fishState.getBiology(), rng),
            processParameterMap(getGrowthRates(), fishState.getBiology(), rng),
            processParameterMap(getSelectivityFilters(), fishState.getBiology())
        );
    }

    private <T> Map<Species, T> processParameterMap(
        final Map<String, T> map,
        final GlobalBiology globalBiology
    ) {
        return processParameterMap(map, globalBiology, identity());
    }

    private <T, U> Map<Species, U> processParameterMap(
        final Map<String, T> map,
        final GlobalBiology globalBiology,
        final Function<T, U> valueMapper
    ) {
        return map.entrySet().stream().collect(toImmutableMap(
            entry -> getSpeciesCodes().getSpeciesFromCode(globalBiology, entry.getKey()),
            entry -> valueMapper.apply(entry.getValue())
        ));
    }

    private Map<Species, Double> processParameterMap(
        final Map<String, DoubleParameter> map,
        final GlobalBiology globalBiology,
        final MersenneTwisterFast rng
    ) {
        return processParameterMap(map, globalBiology, value -> value.apply(rng));
    }

    public Map<String, NonMutatingArrayFilter> getSelectivityFilters() {
        return selectivityFilters;
    }

    public void setSelectivityFilters(final Map<String, NonMutatingArrayFilter> selectivityFilters) {
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }
}
