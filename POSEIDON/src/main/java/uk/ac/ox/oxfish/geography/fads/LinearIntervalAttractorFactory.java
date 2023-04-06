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

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.GlobalCarryingCapacitiesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.HeterogeneousLinearIntervalAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPredicateFunctionFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.Optional;

public class LinearIntervalAttractorFactory extends AbundanceFadInitializerFactory {

    private GlobalCarryingCapacitiesFactory globalCarryingCapacitiesFactory;
    private EnvironmentalPredicateFunctionFactory environmentalPredicateFunctionFactory;
    private DoubleParameter daysItTakesToFillUp = new CalibratedParameter(30);
    private DoubleParameter minAbundanceThreshold = new CalibratedParameter(100);
    public LinearIntervalAttractorFactory() {
    }

    public GlobalCarryingCapacitiesFactory getGlobalCarryingCapacitiesFactory() {
        return globalCarryingCapacitiesFactory;
    }

    public void setGlobalCarryingCapacitiesFactory(final GlobalCarryingCapacitiesFactory globalCarryingCapacitiesFactory) {
        this.globalCarryingCapacitiesFactory = globalCarryingCapacitiesFactory;
    }

    @Override
    protected FadInitializer<AbundanceLocalBiology, AbundanceFad> makeFadInitializer(
        final FishState fishState
    ) {
        return new AbundanceFadInitializer(
            fishState.getBiology(),
            globalCarryingCapacitiesFactory.apply(fishState),
            generateFishAttractor(fishState),
            getFishReleaseProbabilityInPercent().applyAsDouble(fishState.getRandom()) / 100d,
            fishState::getStep
        );
    }

    @NotNull
    private HeterogeneousLinearIntervalAttractor generateFishAttractor(final FishState fishState) {
        final DoubleParameter[] carryingCapacities = getCarryingCapacitiesFactory().apply(fishState);
        final HeterogeneousLinearIntervalAttractor attractor = new HeterogeneousLinearIntervalAttractor(
            (int) getDaysInWaterBeforeAttraction().applyAsDouble(fishState.getRandom()),
            (int) daysItTakesToFillUp.applyAsDouble(fishState.getRandom()),
            minAbundanceThreshold.applyAsDouble(fishState.getRandom()),
            getAbundanceFiltersFactory().apply(fishState).get(FadSetAction.class),
            fishState,
            carryingCapacities
        );
        Optional
            .ofNullable(environmentalPredicateFunctionFactory)
            .map(factory -> factory.apply(fishState))
            .ifPresent(f -> attractor.setAdditionalAttractionHurdle(f::apply));
        return attractor;
    }

    public EnvironmentalPredicateFunctionFactory getEnvironmentalPredicateFunctionFactory() {
        return environmentalPredicateFunctionFactory;
    }

    public void setEnvironmentalPredicateFunctionFactory(final EnvironmentalPredicateFunctionFactory environmentalPredicateFunctionFactory) {
        this.environmentalPredicateFunctionFactory = environmentalPredicateFunctionFactory;
    }

    public DoubleParameter getDaysItTakesToFillUp() {
        return daysItTakesToFillUp;
    }

    public void setDaysItTakesToFillUp(final DoubleParameter daysItTakesToFillUp) {
        invalidateCache();
        this.daysItTakesToFillUp = daysItTakesToFillUp;
    }

    public DoubleParameter getMinAbundanceThreshold() {
        return minAbundanceThreshold;
    }

    public void setMinAbundanceThreshold(final DoubleParameter minAbundanceThreshold) {
        invalidateCache();
        this.minAbundanceThreshold = minAbundanceThreshold;
    }

}
