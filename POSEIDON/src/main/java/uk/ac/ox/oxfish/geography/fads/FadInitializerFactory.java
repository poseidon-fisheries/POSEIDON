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

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.CarryingCapacitySupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Map;

public abstract class FadInitializerFactory<
    B extends LocalBiology,
    F extends Fad>
    implements AlgorithmFactory<FadInitializer<B, F>> {
    private final CacheByFishState<FadInitializer<B, F>> cache =
        new CacheByFishState<>(this::makeFadInitializer);
    private Map<String, DoubleParameter> catchabilities;
    private AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier;
    private DoubleParameter daysInWaterBeforeAttraction;
    private Map<String, DoubleParameter> fishReleaseProbabilities;

    FadInitializerFactory(
        final AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier,
        final Map<String, DoubleParameter> catchabilities,
        final Map<String, DoubleParameter> fishReleaseProbabilities,
        final DoubleParameter daysInWaterBeforeAttraction
    ) {
        this.carryingCapacitySupplier = carryingCapacitySupplier;
        this.catchabilities = catchabilities;
        this.fishReleaseProbabilities = fishReleaseProbabilities;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    FadInitializerFactory() {
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<CarryingCapacitySupplier> getCarryingCapacitySupplier() {
        return carryingCapacitySupplier;
    }

    @SuppressWarnings("unused")
    public void setCarryingCapacitySupplier(final AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier) {
        this.carryingCapacitySupplier = carryingCapacitySupplier;
    }

    public Map<String, DoubleParameter> getFishReleaseProbabilities() {
        return fishReleaseProbabilities;
    }

    public void setFishReleaseProbabilities(final Map<String, DoubleParameter> fishReleaseProbabilities) {
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, DoubleParameter> getCatchabilities() {
        return catchabilities;
    }

    @SuppressWarnings("unused")
    public void setCatchabilities(final Map<String, DoubleParameter> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        invalidateCache();
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    void invalidateCache() {
        cache.invalidateAll();
    }

    @Override
    public FadInitializer<B, F> apply(final FishState fishState) {
        return cache.get(fishState);
    }

    protected abstract FadInitializer<B, F> makeFadInitializer(FishState fishState);

}
