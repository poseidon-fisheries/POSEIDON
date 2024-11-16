/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.agents.behaviours.choices;

import ec.util.MersenneTwisterFast;

import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

public class EpsilonGreedyChooser<O> implements Supplier<O> {

    private final double epsilon;
    private final OptionValues<O> optionValues;
    private final Explorer<O> explorer;
    private final Evaluator<O> evaluator;
    private final MersenneTwisterFast rng;
    private O currentOption;
    private Evaluation currentEvaluation;

    public EpsilonGreedyChooser(
        final double epsilon,
        final OptionValues<O> optionValues,
        final Explorer<O> explorer,
        final Evaluator<O> evaluator,
        final MersenneTwisterFast rng
    ) {
        this.explorer = explorer;
        this.optionValues = checkNotNull(optionValues);
        this.evaluator = checkNotNull(evaluator);
        this.epsilon = checkUnitRange(epsilon, "epsilon");
        this.rng = checkNotNull(rng);
    }

    @Override
    public O get() {
        if (currentOption != null) {
            optionValues.observe(currentOption, currentEvaluation.getResult());
        }
        final List<O> bestOptions = optionValues.getBestOptions();
        final boolean explore = bestOptions.isEmpty() || rng.nextDouble() < epsilon;
        currentOption = explore
            ? explorer.explore(currentOption)
            : oneOf(bestOptions, rng);
        currentEvaluation = evaluator.newEvaluation(currentOption);
        return currentOption;
    }
}
