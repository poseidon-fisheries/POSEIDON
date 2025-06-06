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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.ScaledDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.ZeroInflatedDoubleParameter;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class WeibullPerSpeciesCarryingCapacitiesFactory
    implements AlgorithmFactory<CarryingCapacitySupplier> {

    private Map<String, DoubleParameter> shapeParameters;
    private Map<String, DoubleParameter> scaleParameters;
    private Map<String, DoubleParameter> proportionOfZeros;
    private DoubleParameter capacityScalingFactor;
    private DoubleParameter shapeScalingFactor;

    @SuppressWarnings("unused")
    public WeibullPerSpeciesCarryingCapacitiesFactory() {
    }

    @SuppressWarnings("WeakerAccess")
    public WeibullPerSpeciesCarryingCapacitiesFactory(
        final Map<String, DoubleParameter> shapeParameters,
        final Map<String, DoubleParameter> scaleParameters,
        final Map<String, DoubleParameter> proportionOfZeros,
        final DoubleParameter capacityScalingFactor,
        final DoubleParameter shapeScalingFactor
    ) {
        this.shapeParameters = shapeParameters;
        this.scaleParameters = scaleParameters;
        this.proportionOfZeros = proportionOfZeros;
        this.capacityScalingFactor = capacityScalingFactor;
        this.shapeScalingFactor = shapeScalingFactor;
    }

    @Override
    public CarryingCapacitySupplier apply(
        final FishState fishState
    ) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return () -> new PerSpeciesCarryingCapacity(
            fishState
                .getBiology()
                .getSpecies()
                .stream()
                .collect(toImmutableMap(
                    identity(),
                    species -> speciesCarryingCapacity(species, rng)
                ))
        );
    }

    private double speciesCarryingCapacity(
        final Species species,
        final MersenneTwisterFast rng
    ) {
        final String speciesName = species.getName();
        final String errorMessageTemplate = "%s parameter not found for species '%s'. Known species are: %s";
        checkState(
            getShapeParameters().containsKey(speciesName),
            errorMessageTemplate,
            "Shape",
            speciesName,
            getShapeParameters().keySet()
        );
        checkState(
            getScaleParameters().containsKey(speciesName),
            errorMessageTemplate,
            "Scale",
            speciesName,
            getScaleParameters().keySet()
        );
        return new ScaledDoubleParameter(
            new ZeroInflatedDoubleParameter(
                new WeibullDoubleParameter(
                    getShapeParameters().get(speciesName).applyAsDouble(rng) *
                        getShapeScalingFactor().applyAsDouble(rng),
                    getScaleParameters().get(speciesName).applyAsDouble(rng)
                ),
                getProportionOfZeros().get(speciesName).applyAsDouble(rng)
            ),
            getCapacityScalingFactor().applyAsDouble(rng)
        ).applyAsDouble(rng);
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, DoubleParameter> getShapeParameters() {
        return shapeParameters;
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, DoubleParameter> getScaleParameters() {
        return scaleParameters;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getShapeScalingFactor() {
        return shapeScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setShapeScalingFactor(final DoubleParameter shapeScalingFactor) {
        this.shapeScalingFactor = shapeScalingFactor;
    }

    @SuppressWarnings("WeakerAccess")
    public Map<String, DoubleParameter> getProportionOfZeros() {
        return proportionOfZeros;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getCapacityScalingFactor() {
        return capacityScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setCapacityScalingFactor(final DoubleParameter capacityScalingFactor) {
        this.capacityScalingFactor = capacityScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setProportionOfZeros(final Map<String, DoubleParameter> proportionOfZeros) {
        this.proportionOfZeros = proportionOfZeros;
    }

    @SuppressWarnings("unused")
    public void setScaleParameters(final Map<String, DoubleParameter> scaleParameters) {
        this.scaleParameters = scaleParameters;
    }

    @SuppressWarnings("unused")
    public void setShapeParameters(final Map<String, DoubleParameter> shapeParameters) {
        this.shapeParameters = shapeParameters;
    }
}
