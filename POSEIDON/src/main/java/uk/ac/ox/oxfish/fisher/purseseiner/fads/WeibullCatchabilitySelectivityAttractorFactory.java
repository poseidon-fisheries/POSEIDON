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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

import java.util.LinkedHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class WeibullCatchabilitySelectivityAttractorFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>> {

    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker = new Locker<>();
    private AbundanceFiltersFactory abundanceFiltersFactory;
    private LinkedHashMap<String, Double> carryingCapacityShapeParameters = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> carryingCapacityScaleParameters = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> catchabilities = new LinkedHashMap<>();
    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);

    {
        carryingCapacityShapeParameters.put("Species 0", 0.5d);
    }

    {
        carryingCapacityScaleParameters.put("Species 0", 100000d);
    }

    {
        catchabilities.put("Species 0", 0.001d);
    }
    public WeibullCatchabilitySelectivityAttractorFactory() {
    }

    public WeibullCatchabilitySelectivityAttractorFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
            fishState,
            new Supplier<AbundanceFadInitializer>() {
                @Override
                public AbundanceFadInitializer get() {
                    final MersenneTwisterFast rng = fishState.getRandom();
                    final double probabilityOfFadBeingDud = fadDudRate.applyAsDouble(rng);
                    final DoubleSupplier capacityGenerator;
                    if (Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud == 0)
                        capacityGenerator = () -> Double.MAX_VALUE;
                    else
                        capacityGenerator = () -> {
                            if (rng.nextFloat() <= probabilityOfFadBeingDud)
                                return 0;
                            else
                                return Double.MAX_VALUE;
                        };

                    final GlobalBiology globalBiology = fishState.getBiology();
                    final DoubleParameter[] carryingCapacities = new DoubleParameter[globalBiology.getSize()];
                    final double[] catchabilitiesHere = new double[globalBiology.getSize()];

                    for (final Species species : globalBiology.getSpecies()) {
                        carryingCapacities[species.getIndex()] =
                            carryingCapacityScaleParameters.containsKey(species.getName()) ?
                                new WeibullDoubleParameter(
                                    carryingCapacityShapeParameters.get(species.getName()),
                                    carryingCapacityScaleParameters.get(species.getName())
                                ) : new FixedDoubleParameter(-1);

                        catchabilitiesHere[species.getIndex()] =
                            catchabilities.getOrDefault(species.getName(), 0d);

                    }


                    return new AbundanceFadInitializer(
                        globalBiology,
                        capacityGenerator,
                        new CatchabilitySelectivityFishAttractor(
                            carryingCapacities,
                            catchabilitiesHere,
                            (int) daysInWaterBeforeAttraction.applyAsDouble(rng),
                            (int) maximumDaysAttractions.applyAsDouble(rng),
                            fishState,
                            abundanceFiltersFactory.apply(fishState).get(FadSetAction.class)
                        ),
                        fishReleaseProbabilityInPercent.applyAsDouble(rng) / 100d,
                        fishState::getStep
                    );
                }
            }

        );


    }

    public LinkedHashMap<String, Double> getCarryingCapacityShapeParameters() {
        return carryingCapacityShapeParameters;
    }

    public void setCarryingCapacityShapeParameters(
        final LinkedHashMap<String, Double> carryingCapacityShapeParameters
    ) {
        this.carryingCapacityShapeParameters = carryingCapacityShapeParameters;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityScaleParameters() {
        return carryingCapacityScaleParameters;
    }

    public void setCarryingCapacityScaleParameters(
        final LinkedHashMap<String, Double> carryingCapacityScaleParameters
    ) {
        this.carryingCapacityScaleParameters = carryingCapacityScaleParameters;
    }

    public LinkedHashMap<String, Double> getCatchabilities() {
        return catchabilities;
    }

    public void setCatchabilities(final LinkedHashMap<String, Double> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getMaximumDaysAttractions() {
        return maximumDaysAttractions;
    }

    public void setMaximumDaysAttractions(final DoubleParameter maximumDaysAttractions) {
        this.maximumDaysAttractions = maximumDaysAttractions;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(
        final DoubleParameter fishReleaseProbabilityInPercent
    ) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(final DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }
}
