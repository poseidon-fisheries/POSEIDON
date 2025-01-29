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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Map.entry;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class EpsilonGreedyChooser<O> implements Supplier<Optional<O>> {

    private final double epsilon;

    private final MutableOptionValues<O> optionValues;
    private final Picker<O> explorer;
    private final Picker<O> exploiter;
    private final Evaluator<O> evaluator;
    private final MersenneTwisterFast rng;
    private Optional<Entry<O, Evaluation>> valuedOption = Optional.empty();

    @SuppressFBWarnings(value = "EI2")
    public EpsilonGreedyChooser(
        final double epsilon,
        final MutableOptionValues<O> optionValues,
        final Picker<O> explorer,
        final Picker<O> exploiter,
        final Evaluator<O> evaluator,
        final MersenneTwisterFast rng
    ) {
        this.optionValues = checkNotNull(optionValues);
        this.explorer = explorer;
        this.exploiter = exploiter;
        this.evaluator = checkNotNull(evaluator);
        this.epsilon = checkUnitRange(epsilon, "epsilon");
        this.rng = checkNotNull(rng);
    }

    @Override
    public Optional<O> get() {
        valuedOption.ifPresent(entry ->
            optionValues.observe(entry.getKey(), entry.getValue().getResult())
        );
        final boolean explore = rng.nextBoolean(epsilon);
        valuedOption = Optional
            .of(exploiter)
            .filter(__ -> !explore)
            .flatMap(Picker::pick)
            .or(explorer::pick)
            .map(option -> entry(option, evaluator.newEvaluation(option)));
        return valuedOption.map(Entry::getKey);
    }
}
