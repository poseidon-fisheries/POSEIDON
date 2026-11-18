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
import static uk.ac.ox.poseidon.core.predicates.temporal.Factories.afterDateTime;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

class AfterDateTimeTest {

  private static final LocalDateTime REFERENCE = LocalDateTime.of(
    2025,
    6,
    15,
    12,
    0
  );

  private final ValueFactory<Scope, Object, LocalDateTime> reference = scope ->
    context ->
      REFERENCE;

  private final AfterDateTime<Object> predicate = new AfterDateTime<>(
    context -> REFERENCE
  );

  @Test
  void aLaterDateTimeIsAfter() {
    assertThat(predicate.apply(null, REFERENCE.plusMinutes(1))).isTrue();
  }

  @Test
  void anEarlierDateTimeIsNotAfter() {
    assertThat(predicate.apply(null, REFERENCE.minusMinutes(1))).isFalse();
  }

  @Test
  void theReferenceDateTimeItselfIsNotAfter() {
    assertThat(predicate.apply(null, REFERENCE)).isFalse();
  }

  @Test
  void factoryBuildsTheSamePredicateOverAConstantReference() {
    assertThat(
      afterDateTime(reference)
        .get(GLOBAL_SCOPE)
        .apply(null, REFERENCE.plusMinutes(1))
    ).isTrue();
  }
}
