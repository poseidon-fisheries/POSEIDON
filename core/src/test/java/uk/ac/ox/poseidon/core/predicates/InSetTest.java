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

package uk.ac.ox.poseidon.core.predicates;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.predicates.Factories.in;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class InSetTest {

  private final InSet<Object, String> predicate = new InSet<>(context ->
    List.of("a", "b", "c")
  );

  @Test
  void aMemberIsInTheSet() {
    assertThat(predicate.apply(null, "b")).isTrue();
  }

  @Test
  void aNonMemberIsNotInTheSet() {
    assertThat(predicate.apply(null, "z")).isFalse();
  }

  @Test
  void anEmptySetContainsNothing() {
    final InSet<Object, String> empty = new InSet<>(context -> List.of());

    assertThat(empty.apply(null, "a")).isFalse();
  }

  @Test
  void factoryBuildsTheSamePredicateOverAConstantCollection() {
    final ValueFactory<Scope, Object, Collection<String>> values = scope ->
      context ->
        List.of("a", "b", "c");

    assertThat(in(values).get(GLOBAL_SCOPE).apply(null, "a")).isTrue();
  }
}
