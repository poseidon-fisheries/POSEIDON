/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.regulations;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@SuppressWarnings("unchecked")
class ForbiddenIfTest {

  /**
   * Class under test: ForbiddenIf
   * <p>
   * The ForbiddenIf class is used to determine whether an Action is permitted
   * or forbidden. It uses a Predicate<Action> to evaluate the conditions that
   * make an action forbidden. The isPermitted(Action action) method returns
   * true if the action is permitted (i.e., not forbidden) and false otherwise.
   */

  @SuppressWarnings("unchecked")
  @Test
  void testActionIsPermittedWhenPredicateReturnsFalse() {
    final ForbiddenIf<ExtendedAction<Object>> forbiddenIf = new ForbiddenIf<>(
      action -> false
    );
    final ExtendedAction<Object> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );

    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isTrue();
  }

  @Test
  void testActionIsNotPermittedWhenPredicateIsAlwaysTrue() {
    final ForbiddenIf<ExtendedAction<Object>> forbiddenIf = new ForbiddenIf<>(
      action -> true
    );
    final ExtendedAction<Object> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );

    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isFalse();
  }

  @Test
  void testActionWithNullVessel() {
    final ForbiddenIf<ExtendedAction<Object>> forbiddenIf = new ForbiddenIf<>(
      action -> action.getAgent() == null
    );
    final ExtendedAction<Object> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );
    Mockito.when(mockExtendedAction.getAgent()).thenReturn(null);

    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isFalse();
  }

  @Test
  void testActionWithExtremelyLongDuration() {
    final Predicate<ExtendedAction<Object>> predicate = action ->
      action.getDuration().compareTo(Duration.ofDays(365)) > 0;
    final ForbiddenIf<ExtendedAction<Object>> forbiddenIf = new ForbiddenIf<>(
      predicate
    );

    final ExtendedAction<Object> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );
    Mockito.when(mockExtendedAction.getDuration()).thenReturn(
      Duration.ofDays(1000)
    );

    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isFalse();
  }

  @Test
  void testActionIsNotPermittedWhenPredicateReturnsTrue() {
    final Predicate<ExtendedAction<Object>> predicate = action -> true;
    final ForbiddenIf<ExtendedAction<Object>> forbiddenIf = new ForbiddenIf<>(
      predicate
    );
    final ExtendedAction<Object> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );

    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isFalse();
  }

  @Test
  void testPredicateChecksSpecificFieldOfAction() {
    final String vessel = "Vessel123";
    final ForbiddenIf<ExtendedAction<String>> forbiddenIf = new ForbiddenIf<>(
      action -> action.getAgent().equals(vessel)
    );

    final ExtendedAction<String> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );
    Mockito.when(mockExtendedAction.getAgent()).thenReturn(vessel);
    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isFalse();
  }

  @Test
  void testPredicateChecksDurationGreaterThanThreshold() {
    final Predicate<ExtendedAction<Object>> predicate = action ->
      action.getDuration().compareTo(Duration.ofHours(2)) > 0;
    final ForbiddenIf<ExtendedAction<Object>> forbiddenIf = new ForbiddenIf<>(
      predicate
    );

    final ExtendedAction<Object> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );
    Mockito.when(mockExtendedAction.getDuration()).thenReturn(
      Duration.ofHours(3)
    );

    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isFalse();
  }

  @Test
  void testPredicateChecksDurationLessThanThreshold() {
    final Predicate<ExtendedAction<Object>> predicate = action ->
      action.getDuration().compareTo(Duration.ofHours(2)) > 0;
    final ForbiddenIf<ExtendedAction<Object>> forbiddenIf = new ForbiddenIf<>(
      predicate
    );

    final ExtendedAction<Object> mockExtendedAction = Mockito.mock(
      ExtendedAction.class
    );
    Mockito.when(mockExtendedAction.getDuration()).thenReturn(
      Duration.ofMinutes(90)
    );

    final boolean result = forbiddenIf.isPermitted(mockExtendedAction);

    assertThat(result).isTrue();
  }
}
