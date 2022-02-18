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
import static java.util.stream.Collectors.toMap;

import ec.util.MersenneTwisterFast;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
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

    public FadInitializerFactory(final String... speciesNames) {

        final Supplier<Map<String, DoubleParameter>> zeros = () ->
            Arrays.stream(speciesNames).collect(toMap(
                identity(),
                __ -> new FixedDoubleParameter(0.0)
            ));

        // By setting all coefficients to zero, we'll get a 0.5 probability of attraction
        setCompressionExponents(zeros.get());
        setAttractableBiomassCoefficients(zeros.get());
        setBiomassInteractionsCoefficients(zeros.get());
        setGrowthRates(zeros.get());
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

    @SuppressWarnings("unused")
    public void setTotalCarryingCapacity(final DoubleParameter totalCarryingCapacity) {
        this.totalCarryingCapacity = totalCarryingCapacity;
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

    static Map<Species, Double> processParameterMap(
        final Map<String, DoubleParameter> map,
        final GlobalBiology globalBiology,
        final MersenneTwisterFast rng
    ) {
        return processParameterMap(map, globalBiology, value -> value.apply(rng));
    }

    /**
     * Turns a map from species names to some that to a map from Species objects to some mapping of
     * the original type.
     */
    private static <T, U> Map<Species, U> processParameterMap(
        final Map<String, T> map,
        final GlobalBiology globalBiology,
        final Function<T, U> valueMapper
    ) {
        return map.entrySet().stream().collect(toImmutableMap(
            entry -> globalBiology.getSpecie(entry.getKey()),
            entry -> valueMapper.apply(entry.getValue())
        ));
    }

    public SpeciesCodes getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

}
