/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.processSpeciesNameToDoubleParameterMap;

public class AbundanceLinearIntervalInitializerFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad>> {

    private AbundanceFiltersFactory abundanceFiltersFactory;
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter daysItTakesToFillUp = new FixedDoubleParameter(30);
    private DoubleParameter minAbundanceThreshold = new FixedDoubleParameter(100);
    private AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier;
    private Map<String, DoubleParameter> fishReleaseProbabilities;

    public AbundanceLinearIntervalInitializerFactory() {
    }

    public AbundanceLinearIntervalInitializerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Map<String, DoubleParameter> fishReleaseProbabilities
    ) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    public AlgorithmFactory<CarryingCapacitySupplier> getCarryingCapacitySupplier() {
        return carryingCapacitySupplier;
    }

    public void setCarryingCapacitySupplier(final AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier) {
        this.carryingCapacitySupplier = carryingCapacitySupplier;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> apply(final FishState fishState) {

        final MersenneTwisterFast rng = fishState.getRandom();
        // This is all a bit awkward because the AbundanceLinearIntervalAttractor wants
        // to know the carrying capacities up-front, but the AbundanceAggregatingFadInitializer
        // also wants a CarryingCapacitySupplier to init its carrying capacities, which will
        // be gleefully ignored by the AbundanceLinearIntervalAttractor. Needs more cleanup.
        final CarryingCapacitySupplier carryingCapacitySupplierInstance = carryingCapacitySupplier.apply(fishState);
        final CarryingCapacity carryingCapacity = carryingCapacitySupplierInstance.get();
        checkArgument(
            carryingCapacity instanceof PerSpeciesCarryingCapacity,
            "This type of FADs only works with per-species carrying capacities."
        );
        final AbundanceLinearIntervalAttractor abundanceLinearIntervalAttractor =
            new AbundanceLinearIntervalAttractor(
                (int) daysInWaterBeforeAttraction.applyAsDouble(rng),
                (int) daysItTakesToFillUp.applyAsDouble(rng),
                ((PerSpeciesCarryingCapacity) carryingCapacity).getCarryingCapacities(),
                minAbundanceThreshold.applyAsDouble(rng),
                abundanceFiltersFactory.apply(fishState).get(FadSetAction.class),
                fishState
            );
        return new AbundanceAggregatingFadInitializer(
            fishState.getBiology(),
            abundanceLinearIntervalAttractor,
            fishState::getStep,
            carryingCapacitySupplierInstance,
            processSpeciesNameToDoubleParameterMap(
                getFishReleaseProbabilities(),
                fishState.getBiology(),
                rng
            )
        );
    }

    public Map<String, DoubleParameter> getFishReleaseProbabilities() {
        return fishReleaseProbabilities;
    }

    public void setFishReleaseProbabilities(final Map<String, DoubleParameter> fishReleaseProbabilities) {
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getDaysItTakesToFillUp() {
        return daysItTakesToFillUp;
    }

    public void setDaysItTakesToFillUp(final DoubleParameter daysItTakesToFillUp) {
        this.daysItTakesToFillUp = daysItTakesToFillUp;
    }

    public DoubleParameter getMinAbundanceThreshold() {
        return minAbundanceThreshold;
    }

    public void setMinAbundanceThreshold(final DoubleParameter minAbundanceThreshold) {
        this.minAbundanceThreshold = minAbundanceThreshold;
    }
}
