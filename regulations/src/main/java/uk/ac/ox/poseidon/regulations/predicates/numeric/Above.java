/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.regulations.predicates.numeric;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 * The Above class is a Predicate implementation that tests whether the result of evaluating a given
 * Action with a specified ToDoubleFunction exceeds a predetermined threshold.
 * <p>
 * Constructor parameters:
 * <li> threshold: The numeric threshold that the evaluated value must exceed
 * for the predicate to return true.
 * <li> doubleFunction: A ToDoubleFunction used to extract a double
 * value from an Action for comparison against the threshold.
 */
@Getter
@RequiredArgsConstructor
public class Above implements Predicate<Action> {

    private final double threshold;
    @NonNull private final ToDoubleFunction<Action> doubleFunction;

    @Override
    public boolean test(@NonNull final Action action) {
        return doubleFunction.applyAsDouble(action) > threshold;
    }
}
