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

package uk.ac.ox.poseidon.geography;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.Simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EnvelopeFactoryTest {

    /**
     * Tests the `newInstance` method in the `EnvelopeFactory` class. The `newInstance` method
     * should create an `Envelope` instance using the min and max coordinates provided in the
     * `EnvelopeFactory` fields.
     */

    @Test
    void testNewInstance_CreatesEnvelopeWithSpecifiedBounds() {
        // Prepare test data
        final double minX = 1.0;
        final double maxX = 5.0;
        final double minY = 2.0;
        final double maxY = 6.0;
        final EnvelopeFactory factory = new EnvelopeFactory(minX, maxX, minY, maxY);
        final Simulation simulation = new Simulation();

        // Call the method under test
        final Envelope envelope = factory.newInstance(simulation);

        // Verify results
        assertNotNull(envelope, "Expected an instance of Envelope to be created.");
        assertEquals(
            minX,
            envelope.getMinX(),
            "Envelope minX does not match the value set in EnvelopeFactory."
        );
        assertEquals(
            maxX,
            envelope.getMaxX(),
            "Envelope maxX does not match the value set in EnvelopeFactory."
        );
        assertEquals(
            minY,
            envelope.getMinY(),
            "Envelope minY does not match the value set in EnvelopeFactory."
        );
        assertEquals(
            maxY,
            envelope.getMaxY(),
            "Envelope maxY does not match the value set in EnvelopeFactory."
        );
    }

    @Test
    void testNewInstance_CreatesNormalizedEnvelopeWhenValuesAreUnordered() {
        // Prepare test data
        final double minX = 5.0;
        final double maxX = 1.0; // unordered values
        final double minY = 6.0;
        final double maxY = 2.0; // unordered values
        final EnvelopeFactory factory = new EnvelopeFactory(minX, maxX, minY, maxY);
        final Simulation simulation = new Simulation();

        // Call the method under test
        final Envelope envelope = factory.newInstance(simulation);

        // Verify results
        assertNotNull(envelope, "Expected an instance of Envelope to be created.");
        assertEquals(
            1.0,
            envelope.getMinX(),
            "Envelope minX should be normalized to the smaller value."
        );
        assertEquals(
            5.0,
            envelope.getMaxX(),
            "Envelope maxX should be normalized to the larger value."
        );
        assertEquals(
            2.0,
            envelope.getMinY(),
            "Envelope minY should be normalized to the smaller value."
        );
        assertEquals(
            6.0,
            envelope.getMaxY(),
            "Envelope maxY should be normalized to the larger value."
        );
    }

    @Test
    void testNewInstance_CreatesEnvelopeWithZeroedBoundsWhenFactoryNotInitialized() {
        // Prepare an uninitialized factory
        final EnvelopeFactory factory = new EnvelopeFactory();
        final Simulation simulation = new Simulation();

        // Call the method under test
        final Envelope envelope = factory.newInstance(simulation);

        // Verify results
        assertNotNull(envelope, "Expected an instance of Envelope to be created.");
        assertEquals(0.0, envelope.getMinX(), "Envelope minX should default to 0.0.");
        assertEquals(0.0, envelope.getMaxX(), "Envelope maxX should default to 0.0.");
        assertEquals(0.0, envelope.getMinY(), "Envelope minY should default to 0.0.");
        assertEquals(0.0, envelope.getMaxY(), "Envelope maxY should default to 0.0.");
    }
}
