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

package uk.ac.ox.poseidon.core.predicates.temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.afterDate;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class AfterDateTest {

  private static final LocalDate REFERENCE = LocalDate.of(2025, 6, 15);

  private final ValueFactory<Scope, Object, LocalDate> reference = scope ->
    context ->
      REFERENCE;

  private final AfterDate<Object> predicate = new AfterDate<>(
    context -> REFERENCE
  );

  @Test
  void aLaterDateIsAfter() {
    assertThat(predicate.apply(null, REFERENCE.plusDays(1))).isTrue();
  }

  @Test
  void anEarlierDateIsNotAfter() {
    assertThat(predicate.apply(null, REFERENCE.minusDays(1))).isFalse();
  }

  @Test
  void theReferenceDateItselfIsNotAfter() {
    assertThat(predicate.apply(null, REFERENCE)).isFalse();
  }

  @Test
  void factoryBuildsTheSamePredicateOverAConstantReference() {
    assertThat(
      afterDate(reference).get(GLOBAL_SCOPE).apply(null, REFERENCE.plusDays(1))
    ).isTrue();
  }
}
