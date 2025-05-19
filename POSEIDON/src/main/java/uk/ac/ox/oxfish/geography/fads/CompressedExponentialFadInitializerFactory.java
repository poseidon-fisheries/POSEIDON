/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AggregatingFad;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Arrays;
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

    private DoubleParameter totalCarryingCapacity;

    private Map<String, DoubleParameter> compressionExponents;
    private Map<String, DoubleParameter> attractableBiomassCoefficients;
    private Map<String, DoubleParameter> biomassInteractionsCoefficients;
    private Map<String, DoubleParameter> growthRates;
    private Map<String, DoubleParameter> fishReleaseProbabilities;

    CompressedExponentialFadInitializerFactory(
        final DoubleParameter totalCarryingCapacity,
        final String... speciesNames
    ) {
        // By setting all coefficients to zero, we'll get a 0.5 probability of attraction
        this(
            totalCarryingCapacity,
            makeZeros(speciesNames),
            makeZeros(speciesNames),
            makeZeros(speciesNames),
            makeZeros(speciesNames),
            makeZeros(speciesNames)
        );
    }

    public CompressedExponentialFadInitializerFactory(
        final DoubleParameter totalCarryingCapacity,
        final Map<String, DoubleParameter> compressionExponents,
        final Map<String, DoubleParameter> attractableBiomassCoefficients,
        final Map<String, DoubleParameter> biomassInteractionsCoefficients,
        final Map<String, DoubleParameter> growthRates,
        final Map<String, DoubleParameter> fishReleaseProbabilities
    ) {
        this.totalCarryingCapacity = totalCarryingCapacity;
        this.compressionExponents = compressionExponents;
        this.attractableBiomassCoefficients = attractableBiomassCoefficients;
        this.biomassInteractionsCoefficients = biomassInteractionsCoefficients;
        this.growthRates = growthRates;
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    private static Map<String, DoubleParameter> makeZeros(final String[] speciesNames) {
        return Arrays.stream(speciesNames).collect(toMap(
            identity(),
            __ -> new FixedDoubleParameter(0.0)
        ));
    }

    CompressedExponentialFadInitializerFactory() {
    }

    private static ImmutableMap<String, DoubleParameter> wrapParameters(
        final Map<String, Double> params
    ) {
        return params.entrySet().stream().collect(toImmutableMap(
            Entry::getKey, entry -> new FixedDoubleParameter(entry.getValue())
        ));
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

    public Map<String, DoubleParameter> getFishReleaseProbabilities() {
        return fishReleaseProbabilities;
    }

    public void setFishReleaseProbabilities(final Map<String, DoubleParameter> fishReleaseProbabilities) {
        this.fishReleaseProbabilities = fishReleaseProbabilities;
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
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return compressionExponents;
    }

    @SuppressWarnings("WeakerAccess")
    public void setCompressionExponents(final Map<String, DoubleParameter> compressionExponents) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.compressionExponents = compressionExponents;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getAttractableBiomassCoefficients() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return attractableBiomassCoefficients;
    }

    @SuppressWarnings("WeakerAccess")
    public void setAttractableBiomassCoefficients(
        final Map<String, DoubleParameter> attractableBiomassCoefficients
    ) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.attractableBiomassCoefficients = attractableBiomassCoefficients;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getBiomassInteractionsCoefficients() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return biomassInteractionsCoefficients;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBiomassInteractionsCoefficients(
        final Map<String, DoubleParameter> biomassInteractionsCoefficients
    ) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.biomassInteractionsCoefficients = biomassInteractionsCoefficients;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Map<String, DoubleParameter> getGrowthRates() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return growthRates;
    }

    @SuppressWarnings("WeakerAccess")
    public void setGrowthRates(final Map<String, DoubleParameter> growthRates) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.growthRates = growthRates;
    }

}
