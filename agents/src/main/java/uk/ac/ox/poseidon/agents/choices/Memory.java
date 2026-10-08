/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.choices;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;

import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PACKAGE;

/**
 * What has been observed about each option (e.g. each destination cell), as one remembered
 * {@code M} per option. The first observation of an option is remembered as is; each later one is
 * combined with what is remembered by an update rule supplied with the observation, so that
 * observations from different sources can be remembered differently.
 * <p>
 * This only stores observations: how much an option is worth is up to whoever reads the memory.
 *
 * @param <O> the type of option observed
 * @param <M> the type of what is remembered about an option
 */
@NoArgsConstructor(access = PACKAGE)
public class Memory<O, M> {

    private final Map<O, M> contents = new HashMap<>();

    /**
     * Remembers {@code observation} for {@code option}: as is if nothing is remembered for it
     * yet, otherwise combined with what is remembered by {@code rule}.
     *
     * @param option      the option observed
     * @param observation what was observed; must not be {@code null}
     * @param rule        called with the recollection of {@code option} first and
     *                    {@code observation} second, it returns what to remember from now on; must
     *                    not return {@code null}
     */
    public void observe(
        final O option,
        final M observation,
        final BinaryOperator<M> rule
    ) {
        contents.merge(
            option,
            requireNonNull(observation, "observation"),
            (recollection, _) ->
                requireNonNull(rule.apply(recollection, observation), "rule result")
        );
    }

    /**
     * @param option the option to look up
     * @return what is remembered for {@code option}, if it has been observed
     */
    public Optional<M> get(final O option) {
        return Optional.ofNullable(contents.get(option));
    }

    /**
     * Calls {@code action} once per observed option, with what is remembered for it, in no
     * particular order.
     *
     * @param action the action to call
     */
    public void forEach(final BiConsumer<? super O, ? super M> action) {
        contents.forEach(action);
    }
}
