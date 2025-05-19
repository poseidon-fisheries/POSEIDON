/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.epo.fleet;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.ParameterTable;
import uk.ac.ox.poseidon.agents.core.AtomicLongMapYearlyActionCounter;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.temporal.TemporalMap;
import uk.ac.ox.poseidon.geography.DoubleGrid;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
public abstract class PurseSeineGearFactory implements AlgorithmFactory<PurseSeineGear> {

    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();
    private IntegerParameter targetYear;
    private AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializer;
    private AlgorithmFactory<? extends FishValueCalculator> fishValueCalculator;
    private AlgorithmFactory<? extends ParameterTable> otherParameters;
    private ComponentFactory<? extends TemporalMap<DoubleGrid>> shearGridFactory;

    @SuppressWarnings("WeakerAccess")
    public PurseSeineGearFactory() {
    }

    @SuppressWarnings("WeakerAccess")
    public PurseSeineGearFactory(
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializer,
        final AlgorithmFactory<? extends FishValueCalculator> fishValueCalculator,
        final AlgorithmFactory<? extends ParameterTable> otherParameters,
        final ComponentFactory<? extends TemporalMap<DoubleGrid>> shearGridFactory
    ) {
        this.targetYear = targetYear;
        this.fadInitializer = fadInitializer;
        this.fishValueCalculator = fishValueCalculator;
        this.shearGridFactory = shearGridFactory;
        this.otherParameters = otherParameters;
    }

    public ComponentFactory<? extends TemporalMap<DoubleGrid>> getShearGridFactory() {
        return shearGridFactory;
    }

    public void setShearGridFactory(final ComponentFactory<? extends TemporalMap<DoubleGrid>> shearGridFactory) {
        this.shearGridFactory = shearGridFactory;
    }

    public AlgorithmFactory<? extends FishValueCalculator> getFishValueCalculator() {
        return fishValueCalculator;
    }

    public void setFishValueCalculator(final AlgorithmFactory<? extends FishValueCalculator> fishValueCalculator) {
        this.fishValueCalculator = fishValueCalculator;
    }

    public AlgorithmFactory<? extends FadInitializer<?, ?>> getFadInitializer() {
        return fadInitializer;
    }

    public void setFadInitializer(final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializer) {
        this.fadInitializer = fadInitializer;
    }

    @Override
    public PurseSeineGear apply(final FishState fishState) {
        final Map<String, ? extends DoubleParameter> parameters =
            getOtherParameters()
                .apply(fishState)
                .getParameters(getTargetYear().getIntValue());
        final MersenneTwisterFast rng = fishState.getRandom();
        return makeGear(
            makeFadManager(fishState),
            parameters.get("successful_set_probability").applyAsDouble(rng),
            parameters.get("max_allowable_shear").applyAsDouble(rng),
            shearGridFactory.apply(fishState)
        );
    }

    public AlgorithmFactory<? extends ParameterTable> getOtherParameters() {
        return otherParameters;
    }

    public void setOtherParameters(final AlgorithmFactory<? extends ParameterTable> otherParameters) {
        this.otherParameters = otherParameters;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    protected abstract PurseSeineGear makeGear(
        FadManager fadManager,
        double successfulSetProbability,
        double maxAllowableShear,
        TemporalMap<DoubleGrid> shearGrid
    );

    FadManager makeFadManager(final FishState fishState) {
        checkNotNull(fadInitializer);
        final MersenneTwisterFast rng = fishState.getRandom();
        final GlobalBiology globalBiology = fishState.getBiology();
        return new FadManager(
            fishState.getFadMap(),
            fadInitializer.apply(fishState),
            AtomicLongMapYearlyActionCounter.create(),
            fishValueCalculator.apply(fishState)
        );
    }
}
