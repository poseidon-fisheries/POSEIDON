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

package uk.ac.ox.poseidon.regulations.predicates.logical;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AllOfFactory extends GlobalScopeFactory<AllOf> {

    List<Factory<? extends Predicate<Action>>> predicates;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public AllOfFactory(final Factory<? extends Predicate<Action>>... predicates) {
        this(List.of(predicates));
    }

    @Override
    protected AllOf newInstance(final Simulation simulation) {
        return new AllOf(
            predicates
                .stream()
                .map(p -> p.get(simulation))
                .collect(toImmutableList())
        );
    }
}
