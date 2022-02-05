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

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.util.Map;
import java.util.function.DoubleSupplier;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LogisticFishAbundanceAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class AbundanceFadInitializerFactory
        extends FadInitializerFactory<AbundanceLocalBiology, AbundanceFad> {

    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();

    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);

    /**
     * Empty constructor for YAML
     */
    public AbundanceFadInitializerFactory() {
    }

    public AbundanceFadInitializerFactory(final String... speciesNames) {
        super(speciesNames);
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        checkNotNull(selectivityFilters);
        checkNotNull(getSpeciesCodes());
        final MersenneTwisterFast rng = fishState.getRandom();
        final double totalCarryingCapacity = getTotalCarryingCapacity().apply(rng);
        final double probabilityOfFadBeingDud = fadDudRate.apply(fishState.getRandom());
        DoubleSupplier capacityGenerator;
        if(Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud==0)
            capacityGenerator = () -> totalCarryingCapacity;
        else
            capacityGenerator = () -> {
                if(rng.nextFloat()<=probabilityOfFadBeingDud)
                    return 0;
                else
                    return totalCarryingCapacity;
            };

        return new AbundanceFadInitializer(
                fishState.getBiology(),
                capacityGenerator,
                makeFishAttractor(fishState, rng),
                getFishReleaseProbabilityInPercent().apply(rng) / 100d,
                fishState::getStep
        );
    }

    private LogisticFishAbundanceAttractor makeFishAttractor(
            final FishState fishState,
            final MersenneTwisterFast rng
    ) {
        return new LogisticFishAbundanceAttractor(
                fishState.getRandom(),
                processParameterMap(getCompressionExponents(), fishState.getBiology(), rng),
                processParameterMap(getAttractableBiomassCoefficients(), fishState.getBiology(), rng),
                processParameterMap(getBiomassInteractionsCoefficients(), fishState.getBiology(), rng),
                processParameterMap(getGrowthRates(), fishState.getBiology(), rng),
                getSelectivityFilters()
        );
    }

    @SuppressWarnings("WeakerAccess")
    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return selectivityFilters;
    }

    public void setSelectivityFilters(final Map<Species, NonMutatingArrayFilter> selectivityFilters) {
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }
}
