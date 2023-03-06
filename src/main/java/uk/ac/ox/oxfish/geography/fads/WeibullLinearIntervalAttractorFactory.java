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

import cern.jet.random.engine.MersenneTwister;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.HeterogeneousLinearIntervalAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.MTFApache;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class WeibullLinearIntervalAttractorFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>, PluggableSelectivity {

    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker = new Locker<>();
    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();
    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private LinkedHashMap<String, Double> carryingCapacityShapeParameters = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> carryingCapacityScaleParameters = new LinkedHashMap<>();
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter daysItTakesToFillUp = new FixedDoubleParameter(30);
    private DoubleParameter minAbundanceThreshold = new FixedDoubleParameter(100);

    {
        carryingCapacityShapeParameters.put("Species 0", 0.5d);
    }

    {
        carryingCapacityScaleParameters.put("Species 0", 100000d);
    }

    public WeibullLinearIntervalAttractorFactory() {
    }

    public WeibullLinearIntervalAttractorFactory(
        final DoubleParameter fadDudRate,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final LinkedHashMap<String, Double> carryingCapacityShapeParameters,
        final LinkedHashMap<String, Double> carryingCapacityScaleParameters,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter daysItTakesToFillUp,
        final DoubleParameter minAbundanceThreshold
    ) {
        this.fadDudRate = fadDudRate;
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
        this.carryingCapacityShapeParameters = carryingCapacityShapeParameters;
        this.carryingCapacityScaleParameters = carryingCapacityScaleParameters;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.daysItTakesToFillUp = daysItTakesToFillUp;
        this.minAbundanceThreshold = minAbundanceThreshold;
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
            fishState,
            new Supplier<AbundanceFadInitializer>() {
                @Override
                public AbundanceFadInitializer get() {
                    final double probabilityOfFadBeingDud = fadDudRate.apply(fishState.getRandom());

                    final HeterogeneousLinearIntervalAttractor fishAttractor =
                        generateFishAttractor(
                            fishState);
                    final DoubleSupplier capacityGenerator;
                    if (Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud == 0)
                        capacityGenerator = () -> Double.MAX_VALUE;
                    else
                        capacityGenerator = () -> {
                            if (fishState.getRandom().nextFloat() <= probabilityOfFadBeingDud)
                                return 0;
                            else
                                return Double.MAX_VALUE;
                        };

                    return new AbundanceFadInitializer(
                        fishState.getBiology(),
                        capacityGenerator,
                        fishAttractor,
                        fishReleaseProbabilityInPercent.apply(fishState.getRandom()) / 100d,
                        fishState::getStep
                    );
                }
            }

        );


    }

    @NotNull
    protected HeterogeneousLinearIntervalAttractor generateFishAttractor(final FishState fishState) {
        final DoubleParameter[] carryingCapacities = new DoubleParameter[fishState.getBiology().getSize()];

        //If we want to add correlation, this is the place to do it. I think -BP

        for (final Species species : fishState.getBiology().getSpecies()) {
            carryingCapacities[species.getIndex()] = new WeibullDoubleParameter(
                carryingCapacityShapeParameters.get(species.getName()),
                carryingCapacityScaleParameters.get(species.getName())
            );
        }
        return new HeterogeneousLinearIntervalAttractor(
            daysInWaterBeforeAttraction.apply(
                fishState.getRandom()).intValue(),
            daysItTakesToFillUp.apply(
                fishState.getRandom()).intValue(),
            minAbundanceThreshold.apply(fishState.getRandom()),
            selectivityFilters,
            fishState,
            carryingCapacities

        );
    }

    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        return selectivityFilters;
    }

    public void setSelectivityFilters(
        final Map<Species, NonMutatingArrayFilter> selectivityFilters
    ) {
        oneAttractorPerStateLocker.reset();

        this.selectivityFilters = selectivityFilters;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(final DoubleParameter fadDudRate) {

        oneAttractorPerStateLocker.reset();

        this.fadDudRate = fadDudRate;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(
        final DoubleParameter fishReleaseProbabilityInPercent
    ) {
        oneAttractorPerStateLocker.reset();

        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityShapeParameters() {
        return carryingCapacityShapeParameters;
    }

    public void setCarryingCapacityShapeParameters(
        final LinkedHashMap<String, Double> carryingCapacityShapeParameters
    ) {
        oneAttractorPerStateLocker.reset();

        this.carryingCapacityShapeParameters = carryingCapacityShapeParameters;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityScaleParameters() {
        return carryingCapacityScaleParameters;
    }

    public void setCarryingCapacityScaleParameters(
        final LinkedHashMap<String, Double> carryingCapacityScaleParameters
    ) {
        oneAttractorPerStateLocker.reset();

        this.carryingCapacityScaleParameters = carryingCapacityScaleParameters;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        oneAttractorPerStateLocker.reset();

        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getDaysItTakesToFillUp() {
        return daysItTakesToFillUp;
    }

    public void setDaysItTakesToFillUp(final DoubleParameter daysItTakesToFillUp) {
        oneAttractorPerStateLocker.reset();

        this.daysItTakesToFillUp = daysItTakesToFillUp;
    }

    public DoubleParameter getMinAbundanceThreshold() {
        return minAbundanceThreshold;
    }

    public void setMinAbundanceThreshold(final DoubleParameter minAbundanceThreshold) {
        oneAttractorPerStateLocker.reset();

        this.minAbundanceThreshold = minAbundanceThreshold;
    }
}
