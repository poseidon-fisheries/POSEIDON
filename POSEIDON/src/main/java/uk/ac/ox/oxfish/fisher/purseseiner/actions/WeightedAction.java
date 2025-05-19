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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import java.util.Map;
import java.util.function.DoubleUnaryOperator;

public class WeightedAction<A extends PurseSeinerAction> {

    private final A action;
    private final double initialValue;
    private final double modulatedValue;
    private final double weightedValue;

    private WeightedAction(
        final A action,
        final double initialValue,
        final double modulatedValue,
        final double weightedValue
    ) {
        this.action = action;
        this.initialValue = initialValue;
        this.modulatedValue = modulatedValue;
        this.weightedValue = weightedValue;
    }

    public static <A extends PurseSeinerAction> WeightedAction<A> from(
        final A action,
        final double initialValue,
        final DoubleUnaryOperator actionValueFunction,
        final Map<Class<? extends PurseSeinerAction>, Double> actionWeights
    ) {
        final double modulatedValue = actionValueFunction.applyAsDouble(initialValue);
        final Double w = actionWeights.getOrDefault(action.getClassForWeighting(), 0.0);
        return new WeightedAction<>(
            action,
            initialValue,
            modulatedValue,
            modulatedValue * w
        );
    }

    public A getAction() {
        return action;
    }

    public double getInitialValue() {
        return initialValue;
    }

    public double getModulatedValue() {
        return modulatedValue;
    }

    public double getWeightedValue() {
        return weightedValue;
    }
}
