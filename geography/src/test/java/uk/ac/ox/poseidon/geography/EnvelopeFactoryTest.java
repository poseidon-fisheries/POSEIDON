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

package uk.ac.ox.poseidon.geography;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Scope;

class EnvelopeFactoryTest {

  /**
   * Tests the `newInstance` method in the `EnvelopeFactory` class. The
   * `newInstance` method should create an `Envelope` instance using the min and
   * max coordinates provided in the `EnvelopeFactory` fields.
   */

  @Test
  void testNewInstance_CreatesEnvelopeWithSpecifiedBounds() {
    // Prepare test data
    final double minX = 1.0;
    final double maxX = 5.0;
    final double minY = 2.0;
    final double maxY = 6.0;
    final EnvelopeFactory factory = new EnvelopeFactory(minX, maxX, minY, maxY);

    // Call the method under test
    final Envelope envelope = factory.get(Scope.GLOBAL_SCOPE);

    // Verify results
    assertThat(envelope).isNotNull();
    assertThat(envelope.getMinX()).isEqualTo(minX);
    assertThat(envelope.getMaxX()).isEqualTo(maxX);
    assertThat(envelope.getMinY()).isEqualTo(minY);
    assertThat(envelope.getMaxY()).isEqualTo(maxY);
  }

  @Test
  void testNewInstance_CreatesNormalizedEnvelopeWhenValuesAreUnordered() {
    // Prepare test data
    final double minX = 5.0;
    final double maxX = 1.0; // unordered values
    final double minY = 6.0;
    final double maxY = 2.0; // unordered values
    final EnvelopeFactory factory = new EnvelopeFactory(minX, maxX, minY, maxY);

    // Call the method under test
    final Envelope envelope = factory.get(Scope.GLOBAL_SCOPE);

    // Verify results
    assertThat(envelope).isNotNull();
    assertThat(envelope.getMinX()).isEqualTo(1.0);
    assertThat(envelope.getMaxX()).isEqualTo(5.0);
    assertThat(envelope.getMinY()).isEqualTo(2.0);
    assertThat(envelope.getMaxY()).isEqualTo(6.0);
  }

  @Test
  void testNewInstance_CreatesEnvelopeWithZeroedBoundsWhenFactoryNotInitialized() {
    // Prepare an uninitialized factory
    final EnvelopeFactory factory = EnvelopeFactory.builder().build();

    // Call the method under test
    final Envelope envelope = factory.get(Scope.GLOBAL_SCOPE);

    // Verify results
    assertThat(envelope).isNotNull();
    assertThat(envelope.getMinX()).isEqualTo(0.0);
    assertThat(envelope.getMaxX()).isEqualTo(0.0);
    assertThat(envelope.getMinY()).isEqualTo(0.0);
    assertThat(envelope.getMaxY()).isEqualTo(0.0);
  }
}
