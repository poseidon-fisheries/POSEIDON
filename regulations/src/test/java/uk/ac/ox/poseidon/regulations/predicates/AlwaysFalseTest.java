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

package uk.ac.ox.poseidon.regulations.predicates;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.behaviours.Action;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class AlwaysFalseTest {

    /**
     * The AlwaysFalse class implements a Predicate<Action> that always returns false regardless of
     * the input Action provided to the test method.
     */

    @Test
    void test_alwaysReturnsFalse() {
        // Arrange
        final AlwaysFalse alwaysFalse = new AlwaysFalse();
        final Action mockAction = mock(Action.class);

        // Act
        final boolean result = alwaysFalse.test(mockAction);

        // Assert
        assertFalse(result);
    }
}
