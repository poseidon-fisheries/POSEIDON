/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.time.Duration;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermittedIfTest {

    /**
     * Test class for the PermittedIf class. Provides test cases for the isPermitted method, which
     * determines if an action is permitted based on a given predicate.
     */

    @Test
    void testIsPermittedWhenPredicateReturnsTrue() {
        // Mock the Action interface
        final Action mockAction = Mockito.mock(Action.class);

        // Define a predicate that always returns true
        final Predicate<Action> predicate = action -> true;

        // Create an instance of PermittedIf with the predicate
        final PermittedIf permittedIf = new PermittedIf(predicate);

        // Assert the method isPermitted returns true
        assertTrue(permittedIf.isPermitted(mockAction));
    }

    @Test
    void testIsPermittedWhenPredicateReturnsFalse() {
        // Mock the Action interface
        final Action mockAction = Mockito.mock(Action.class);

        // Define a predicate that always returns false
        final Predicate<Action> predicate = action -> false;

        // Create an instance of PermittedIf with the predicate
        final PermittedIf permittedIf = new PermittedIf(predicate);

        // Assert the method isPermitted returns false
        assertFalse(permittedIf.isPermitted(mockAction));
    }

    @Test
    void testIsPermittedWithPredicateThatEvaluatesSpecificAction() {
        // Mock the Action interface
        final Action mockAction = Mockito.mock(Action.class);
        Mockito.when(mockAction.getDuration()).thenReturn(Duration.ofHours(2));

        // Define a predicate that permits actions with a duration of exactly 2 hours
        final Predicate<Action> predicate =
            action -> action.getDuration().equals(Duration.ofHours(2));

        // Create an instance of PermittedIf with the predicate
        final PermittedIf permittedIf = new PermittedIf(predicate);

        // Assert the method isPermitted returns true for the mockAction
        assertTrue(permittedIf.isPermitted(mockAction));
    }

    @Test
    void testIsPermittedWithPredicateThatRejectsSpecificAction() {
        // Mock the Action interface
        final Action mockAction = Mockito.mock(Action.class);
        Mockito.when(mockAction.getDuration()).thenReturn(Duration.ofHours(3));

        // Define a predicate that only permits actions with a duration of exactly 2 hours
        final Predicate<Action> predicate =
            action -> action.getDuration().equals(Duration.ofHours(2));

        // Create an instance of PermittedIf with the predicate
        final PermittedIf permittedIf = new PermittedIf(predicate);

        // Assert the method isPermitted returns false for the mockAction
        assertFalse(permittedIf.isPermitted(mockAction));
    }
}
