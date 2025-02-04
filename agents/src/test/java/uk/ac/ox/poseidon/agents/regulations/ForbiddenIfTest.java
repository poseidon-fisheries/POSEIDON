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

package uk.ac.ox.poseidon.agents.regulations;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.time.Duration;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForbiddenIfTest {

    /**
     * Class under test: ForbiddenIf
     * <p>
     * The ForbiddenIf class is used to determine whether an Action is permitted or forbidden. It
     * uses a Predicate<Action> to evaluate the conditions that make an action forbidden. The
     * isPermitted(Action action) method returns true if the action is permitted (i.e., not
     * forbidden) and false otherwise.
     */

    @Test
    void testActionIsPermittedWhenPredicateReturnsFalse() {
        // Arrange
        final Predicate<Action> predicate = action -> false;
        final ForbiddenIf forbiddenIf = new ForbiddenIf(predicate);
        final Action mockAction = Mockito.mock(Action.class);

        // Act
        final boolean result = forbiddenIf.isPermitted(mockAction);

        // Assert
        assertTrue(result, "Expected action to be permitted when predicate returns false.");
    }

    @Test
    void testActionIsNotPermittedWhenPredicateReturnsTrue() {
        // Arrange
        final Predicate<Action> predicate = action -> true;
        final ForbiddenIf forbiddenIf = new ForbiddenIf(predicate);
        final Action mockAction = Mockito.mock(Action.class);

        // Act
        final boolean result = forbiddenIf.isPermitted(mockAction);

        // Assert
        assertFalse(result, "Expected action to be not permitted when predicate returns true.");
    }

    @Test
    void testPredicateChecksSpecificFieldOfAction() {
        // Arrange
        final Predicate<Action> predicate = action -> "Vessel123".equals(action
            .getVessel()
            .getId());
        final ForbiddenIf forbiddenIf = new ForbiddenIf(predicate);

        final Action mockAction = Mockito.mock(Action.class);
        final Vessel mockVessel = Mockito.mock(Vessel.class);
        Mockito.when(mockAction.getVessel()).thenReturn(mockVessel);
        Mockito.when(mockVessel.getId()).thenReturn("Vessel123");

        // Act
        final boolean result = forbiddenIf.isPermitted(mockAction);

        // Assert
        assertFalse(
            result,
            "Expected action to be not permitted when predicate matches a specific vessel name."
        );
    }

    @Test
    void testPredicateChecksDurationGreaterThanThreshold() {
        // Arrange
        final Predicate<Action> predicate =
            action -> action.getDuration().compareTo(Duration.ofHours(2)) > 0;
        final ForbiddenIf forbiddenIf = new ForbiddenIf(predicate);

        final Action mockAction = Mockito.mock(Action.class);
        Mockito.when(mockAction.getDuration()).thenReturn(Duration.ofHours(3));

        // Act
        final boolean result = forbiddenIf.isPermitted(mockAction);

        // Assert
        assertFalse(result, "Expected action to be not permitted when duration exceeds 2 hours.");
    }

    @Test
    void testPredicateChecksDurationLessThanThreshold() {
        // Arrange
        final Predicate<Action> predicate =
            action -> action.getDuration().compareTo(Duration.ofHours(2)) > 0;
        final ForbiddenIf forbiddenIf = new ForbiddenIf(predicate);

        final Action mockAction = Mockito.mock(Action.class);
        Mockito.when(mockAction.getDuration()).thenReturn(Duration.ofMinutes(90));

        // Act
        final boolean result = forbiddenIf.isPermitted(mockAction);

        // Assert
        assertTrue(result, "Expected action to be permitted when duration is less than 2 hours.");
    }
}
