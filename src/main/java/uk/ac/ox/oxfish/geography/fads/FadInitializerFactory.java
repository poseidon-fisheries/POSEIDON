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

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

import ec.util.MersenneTwisterFast;
import java.util.HashMap;
import java.util.Map;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LogisticFishBiomassAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public abstract class FadInitializerFactory<B extends LocalBiology, F extends Fad<B, F>>
    implements AlgorithmFactory<FadInitializer<B, F>> {

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private DoubleParameter totalCarryingCapacity = new FixedDoubleParameter(445_000); // TODO
    private Map<String, DoubleParameter> compressionExponents = new HashMap<>();
    private Map<String, DoubleParameter> attractableBiomassCoefficients = new HashMap<>();
    private Map<String, DoubleParameter> biomassInteractionsCoefficients = new HashMap<>();
    private Map<String, DoubleParameter> growthRates = new HashMap<>();
    private SpeciesCodes speciesCodes;

    public SpeciesCodes getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    @SuppressWarnings("unused")
    public void setFishReleaseProbabilityInPercent(
        final DoubleParameter fishReleaseProbabilityInPercent
    ) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public DoubleParameter getTotalCarryingCapacity() {
        return totalCarryingCapacity;
    }

    public void setTotalCarryingCapacity(final DoubleParameter totalCarryingCapacity) {
        this.totalCarryingCapacity = totalCarryingCapacity;
    }

    FishAttractor<B, F> makeFishAttractor(
        final FishState fishState,
        final MersenneTwisterFast rng,
        final double totalCarryingCapacity
    ) {
        return fishState.getSpecies()
            .stream()
            .collect(toImmutableMap(
                identity(),
                species -> {
                    final String speciesName = species.getName();
                    return new LogisticFishBiomassAttractor(
                        fishState.getRandom(),
                        getCompressionExponents().get(speciesName).apply(rng),
                        getAttractableBiomassCoefficients().get(speciesName).apply(rng),
                        getBiomassInteractionsCoefficients().get(speciesName).apply(rng),
                        getGrowthRates().get(speciesName).apply(rng),
                        totalCarryingCapacity
                    );
                }
            ));
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getCompressionExponents() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return compressionExponents;
    }

    @SuppressWarnings("unused")
    public void setCompressionExponents(final Map<String, DoubleParameter> compressionExponents) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.compressionExponents = compressionExponents;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getAttractableBiomassCoefficients() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return attractableBiomassCoefficients;
    }

    @SuppressWarnings("unused")
    public void setAttractableBiomassCoefficients(
        final Map<String, DoubleParameter> attractableBiomassCoefficients
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.attractableBiomassCoefficients = attractableBiomassCoefficients;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getBiomassInteractionsCoefficients() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return biomassInteractionsCoefficients;
    }

    @SuppressWarnings("unused")
    public void setBiomassInteractionsCoefficients(
        final Map<String, DoubleParameter> biomassInteractionsCoefficients
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.biomassInteractionsCoefficients = biomassInteractionsCoefficients;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getGrowthRates() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return growthRates;
    }

    @SuppressWarnings("unused")
    public void setGrowthRates(final Map<String, DoubleParameter> growthRates) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.growthRates = growthRates;
    }
}
