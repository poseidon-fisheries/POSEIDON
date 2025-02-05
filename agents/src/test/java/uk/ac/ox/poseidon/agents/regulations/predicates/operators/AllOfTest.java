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

package uk.ac.ox.poseidon.agents.regulations.predicates.operators;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllOfTest {

    /**
     * Tests the {@link AllOf#test(Action)} method which checks if all predicates in the collection
     * return true for the given action.
     */

    @Test
    void testAllPredicatesReturnTrue() {
        final Action action = Mockito.mock(Action.class);

        final Predicate<Action> predicate1 = Mockito.mock(Predicate.class);
        final Predicate<Action> predicate2 = Mockito.mock(Predicate.class);
        final Predicate<Action> predicate3 = Mockito.mock(Predicate.class);

        Mockito.when(predicate1.test(action)).thenReturn(true);
        Mockito.when(predicate2.test(action)).thenReturn(true);
        Mockito.when(predicate3.test(action)).thenReturn(true);

        final AllOf allOf = new AllOf(ImmutableList.of(predicate1, predicate2, predicate3));
        assertTrue(allOf.test(action), "Expected all predicates to return true.");
    }

    @Test
    void testOnePredicateReturnsFalse() {
        final Action action = Mockito.mock(Action.class);

        final Predicate<Action> predicate1 = Mockito.mock(Predicate.class);
        final Predicate<Action> predicate2 = Mockito.mock(Predicate.class);
        final Predicate<Action> predicate3 = Mockito.mock(Predicate.class);

        Mockito.when(predicate1.test(action)).thenReturn(true);
        Mockito.when(predicate2.test(action)).thenReturn(false);
        Mockito.when(predicate3.test(action)).thenReturn(true);

        final AllOf allOf = new AllOf(ImmutableList.of(predicate1, predicate2, predicate3));
        assertFalse(
            allOf.test(action),
            "Expected test to return false as one predicate returned false."
        );
    }

    @Test
    void testAllPredicatesReturnFalse() {
        final Action action = Mockito.mock(Action.class);

        final Predicate<Action> predicate1 = Mockito.mock(Predicate.class);
        final Predicate<Action> predicate2 = Mockito.mock(Predicate.class);

        Mockito.when(predicate1.test(action)).thenReturn(false);
        Mockito.when(predicate2.test(action)).thenReturn(false);

        final AllOf allOf = new AllOf(ImmutableList.of(predicate1, predicate2));
        assertFalse(
            allOf.test(action),
            "Expected test to return false as all predicates returned false."
        );
    }

    @Test
    void testEmptyPredicates() {
        final Action action = Mockito.mock(Action.class);

        final AllOf allOf = new AllOf(ImmutableList.of());
        assertTrue(
            allOf.test(action),
            "Expected test to return true as no predicates are present."
        );
    }
}
