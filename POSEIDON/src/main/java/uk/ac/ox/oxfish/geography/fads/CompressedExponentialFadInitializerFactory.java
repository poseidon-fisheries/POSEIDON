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

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AggregatingFad;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public abstract class CompressedExponentialFadInitializerFactory<
    B extends LocalBiology,
    F extends AggregatingFad<B, F>
    >
    implements AlgorithmFactory<FadInitializer<B, F>> {

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(2.0);
    private DoubleParameter totalCarryingCapacity = new FixedDoubleParameter(445_000); // TODO


    private Map<String, DoubleParameter> compressionExponents = new HashMap<>();
    private Map<String, DoubleParameter> attractableBiomassCoefficients = new HashMap<>();
    private Map<String, DoubleParameter> biomassInteractionsCoefficients = new HashMap<>();
    private Map<String, DoubleParameter> growthRates = new HashMap<>();

    CompressedExponentialFadInitializerFactory(
        final String... speciesNames
    ) {
        // By setting all coefficients to zero, we'll get a 0.5 probability of attraction
        this(
            makeZeros(speciesNames),
            makeZeros(speciesNames),
            makeZeros(speciesNames),
            makeZeros(speciesNames)
        );
    }

    CompressedExponentialFadInitializerFactory(
        final Map<String, Double> compressionExponents,
        final Map<String, Double> attractableBiomassCoefficients,
        final Map<String, Double> biomassInteractionsCoefficients,
        final Map<String, Double> growthRates
    ) {
        setCompressionExponents(wrapParameters(compressionExponents));
        setAttractableBiomassCoefficients(wrapParameters(attractableBiomassCoefficients));
        setBiomassInteractionsCoefficients(wrapParameters(biomassInteractionsCoefficients));
        setGrowthRates(wrapParameters(growthRates));
    }

    private static Map<String, Double> makeZeros(final String[] speciesNames) {
        return Arrays.stream(speciesNames).collect(toMap(
            identity(),
            __ -> 0.0
        ));
    }

    private static ImmutableMap<String, DoubleParameter> wrapParameters(
        final Map<String, Double> params
    ) {
        return params.entrySet().stream().collect(toImmutableMap(
            Entry::getKey, entry -> new FixedDoubleParameter(entry.getValue())
        ));
    }

    CompressedExponentialFadInitializerFactory() {
    }

    static double[] processParameterMap(
        final Map<String, DoubleParameter> map,
        final GlobalBiology globalBiology,
        final MersenneTwisterFast rng
    ) {
        final double[] a = new double[globalBiology.getSize()];
        map.forEach((speciesName, parameter) -> {
            final int index = globalBiology.getSpeciesByCaseInsensitiveName(speciesName).getIndex();
            a[index] = parameter.applyAsDouble(rng);
        });
        return a;
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

    @SuppressWarnings("WeakerAccess")
    public void setCompressionExponents(final Map<String, DoubleParameter> compressionExponents) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.compressionExponents = compressionExponents;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getAttractableBiomassCoefficients() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return attractableBiomassCoefficients;
    }

    @SuppressWarnings("WeakerAccess")
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

    @SuppressWarnings("WeakerAccess")
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

    @SuppressWarnings("WeakerAccess")
    public void setGrowthRates(final Map<String, DoubleParameter> growthRates) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.growthRates = growthRates;
    }

}
