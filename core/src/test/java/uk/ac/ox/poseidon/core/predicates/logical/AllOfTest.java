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

package uk.ac.ox.poseidon.core.predicates.logical;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllOfTest {

    /**
     * Tests the {@link AllOf#test(Object)} method which checks if all predicates in the collection
     * return true for the given object.
     */

    @Test
    void testAllPredicatesReturnTrue() {
        final Object object = Mockito.mock(Object.class);

        final Predicate<Object> predicate1 = Mockito.mock(Predicate.class);
        final Predicate<Object> predicate2 = Mockito.mock(Predicate.class);
        final Predicate<Object> predicate3 = Mockito.mock(Predicate.class);

        Mockito.when(predicate1.test(object)).thenReturn(true);
        Mockito.when(predicate2.test(object)).thenReturn(true);
        Mockito.when(predicate3.test(object)).thenReturn(true);

        final AllOf allOf = new AllOf(ImmutableList.of(predicate1, predicate2, predicate3));
        assertTrue(allOf.test(object), "Expected all predicates to return true.");
    }

    @Test
    void testOnePredicateReturnsFalse() {
        final Object object = Mockito.mock(Object.class);

        final Predicate<Object> predicate1 = Mockito.mock(Predicate.class);
        final Predicate<Object> predicate2 = Mockito.mock(Predicate.class);
        final Predicate<Object> predicate3 = Mockito.mock(Predicate.class);

        Mockito.when(predicate1.test(object)).thenReturn(true);
        Mockito.when(predicate2.test(object)).thenReturn(false);
        Mockito.when(predicate3.test(object)).thenReturn(true);

        final AllOf allOf = new AllOf(ImmutableList.of(predicate1, predicate2, predicate3));
        assertFalse(
            allOf.test(object),
            "Expected test to return false as one predicate returned false."
        );
    }

    @Test
    void testAllPredicatesReturnFalse() {
        final Object object = Mockito.mock(Object.class);

        final Predicate<Object> predicate1 = Mockito.mock(Predicate.class);
        final Predicate<Object> predicate2 = Mockito.mock(Predicate.class);

        Mockito.when(predicate1.test(object)).thenReturn(false);
        Mockito.when(predicate2.test(object)).thenReturn(false);

        final AllOf allOf = new AllOf(ImmutableList.of(predicate1, predicate2));
        assertFalse(
            allOf.test(object),
            "Expected test to return false as all predicates returned false."
        );
    }

    @Test
    void testEmptyPredicates() {
        final Object object = Mockito.mock(Object.class);

        final AllOf allOf = new AllOf(ImmutableList.of());
        assertTrue(
            allOf.test(object),
            "Expected test to return true as no predicates are present."
        );
    }
}
