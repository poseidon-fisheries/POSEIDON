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

package uk.ac.ox.poseidon.geography;

/**
 * Factories for the basic spatial primitives of this package: geographic coordinates and
 * bounding-box envelopes.
 */
public class Factories {

    private Factories() {}

    /**
     * @param longitude the longitude, in degrees
     * @param latitude  the latitude, in degrees
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link Coordinate}
     * @see Coordinate
     */
    public static CoordinateFactory coordinate(
        final double longitude,
        final double latitude
    ) {
        return new CoordinateFactory(longitude, latitude);
    }

    /**
     * @param minX one of the envelope's x bounds (ordered with {@code maxX} automatically)
     * @param maxX the other x bound
     * @param minY one of the envelope's y bounds (ordered with {@code maxY} automatically)
     * @param maxY the other y bound
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for an {@link Envelope}
     * @see Envelope
     */
    public static EnvelopeFactory envelope(
        final double minX,
        final double maxX,
        final double minY,
        final double maxY
    ) {
        return new EnvelopeFactory(minX, maxX, minY, maxY);
    }
}
