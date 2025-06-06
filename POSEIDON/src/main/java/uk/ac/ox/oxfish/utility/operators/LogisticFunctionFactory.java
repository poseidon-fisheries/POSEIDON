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

package uk.ac.ox.oxfish.utility.operators;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import static java.lang.Math.min;

public class LogisticFunctionFactory implements Supplier<DoubleUnaryOperator>, AlgorithmFactory<DoubleUnaryOperator> {

    private double midpoint = 0.5;
    private double steepness = 1.0;
    private double maximum = 1.0;
    private double maximumSteepness = 1E6; // to avoid infinite steepness when (relativeSteepness && midpoint == 0)
    private boolean relativeSteepness = true;

    public LogisticFunctionFactory(final double midpoint, final double steepness) {
        this(midpoint, steepness, 1.0, true);
    }

    public LogisticFunctionFactory(
        final double midpoint,
        final double steepness,
        final double maximum,
        final boolean relativeSteepness
    ) {
        this.midpoint = midpoint;
        this.steepness = steepness;
        this.maximum = maximum;
        this.relativeSteepness = relativeSteepness;
    }

    @SuppressWarnings("unused")
    public LogisticFunctionFactory() {
    }

    @SuppressWarnings("unused")
    public double getMaximumSteepness() {
        return maximumSteepness;
    }

    @SuppressWarnings("unused")
    public void setMaximumSteepness(final double maximumSteepness) {
        this.maximumSteepness = maximumSteepness;
    }

    @SuppressWarnings("unused")
    public boolean isRelativeSteepness() {
        return relativeSteepness;
    }

    @SuppressWarnings("unused")
    public void setRelativeSteepness(final boolean relativeSteepness) {
        this.relativeSteepness = relativeSteepness;
    }

    @SuppressWarnings("unused")
    public double getMidpoint() {
        return midpoint;
    }

    @SuppressWarnings("unused")
    public void setMidpoint(final double midpoint) {
        this.midpoint = midpoint;
    }

    public double getSteepness() {
        return steepness;
    }

    public void setSteepness(final double steepness) {
        this.steepness = steepness;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(final double maximum) {
        this.maximum = maximum;
    }

    @Override
    public DoubleUnaryOperator apply(final FishState fishState) {
        return get();
    }

    @Override
    public LogisticFunction get() {
        return new LogisticFunction(
            midpoint,
            relativeSteepness ? min(steepness / midpoint, maximumSteepness) : steepness,
            maximum
        );
    }
}
