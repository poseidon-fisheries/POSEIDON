/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.HeterogeneousLinearIntervalAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPredicateFunctionFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Map;
import java.util.Optional;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.processSpeciesNameToDoubleParameterMap;

public class LinearIntervalAttractorFactory
    extends AbundanceFadInitializerFactory {

    private EnvironmentalPredicateFunctionFactory environmentalPredicateFunctionFactory;
    private DoubleParameter daysItTakesToFillUp = new CalibratedParameter(30);
    private DoubleParameter minAbundanceThreshold = new CalibratedParameter(100);
    private Map<String, DoubleParameter> fishReleaseProbabilities;

    public LinearIntervalAttractorFactory() {
    }

    public Map<String, DoubleParameter> getFishReleaseProbabilities() {
        return fishReleaseProbabilities;
    }

    public void setFishReleaseProbabilities(final Map<String, DoubleParameter> fishReleaseProbabilities) {
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    @Override
    protected FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> makeFadInitializer(
        final FishState fishState
    ) {
        return new AbundanceAggregatingFadInitializer(
            fishState.getBiology(),
            generateFishAttractor(fishState),
            fishState::getStep,
            getCarryingCapacitySupplier().apply(fishState),
            processSpeciesNameToDoubleParameterMap(
                fishReleaseProbabilities,
                fishState.getBiology(),
                fishState.getRandom()
            )
        );
    }

    private HeterogeneousLinearIntervalAttractor generateFishAttractor(final FishState fishState) {
        final HeterogeneousLinearIntervalAttractor attractor = new HeterogeneousLinearIntervalAttractor(
            (int) getDaysInWaterBeforeAttraction().applyAsDouble(fishState.getRandom()),
            (int) daysItTakesToFillUp.applyAsDouble(fishState.getRandom()),
            minAbundanceThreshold.applyAsDouble(fishState.getRandom()),
            getAbundanceFilters().apply(fishState).get(FadSetAction.class),
            fishState
        );
        Optional
            .ofNullable(environmentalPredicateFunctionFactory)
            .map(factory -> factory.apply(fishState))
            .ifPresent(f -> attractor.setAdditionalAttractionHurdle(f::apply));
        return attractor;
    }

    @SuppressWarnings("unused")
    public EnvironmentalPredicateFunctionFactory getEnvironmentalPredicateFunctionFactory() {
        return environmentalPredicateFunctionFactory;
    }

    @SuppressWarnings("unused")
    public void setEnvironmentalPredicateFunctionFactory(final EnvironmentalPredicateFunctionFactory environmentalPredicateFunctionFactory) {
        this.environmentalPredicateFunctionFactory = environmentalPredicateFunctionFactory;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getDaysItTakesToFillUp() {
        return daysItTakesToFillUp;
    }

    @SuppressWarnings("unused")
    public void setDaysItTakesToFillUp(final DoubleParameter daysItTakesToFillUp) {
        invalidateCache();
        this.daysItTakesToFillUp = daysItTakesToFillUp;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getMinAbundanceThreshold() {
        return minAbundanceThreshold;
    }

    @SuppressWarnings("unused")
    public void setMinAbundanceThreshold(final DoubleParameter minAbundanceThreshold) {
        invalidateCache();
        this.minAbundanceThreshold = minAbundanceThreshold;
    }

}
