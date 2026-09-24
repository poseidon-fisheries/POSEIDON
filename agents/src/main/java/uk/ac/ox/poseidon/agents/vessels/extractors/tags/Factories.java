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

package uk.ac.ox.poseidon.agents.vessels.extractors.tags;

/** Factories for {@code Function<Vessel, ?>}s that extract a vessel's tag by name. */
public class Factories {
    private Factories() {}

    /**
     * @param tagName the name of the tag to extract
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
     * {@link DoubleTagExtractor} extracting the {@code tagName} tag
     * @see DoubleTagExtractor
     */
    public static DoubleTagExtractorFactory doubleTagExtractor(final String tagName) {
        return new DoubleTagExtractorFactory(tagName);
    }

    /**
     * @param tagName the name of the tag to extract
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
     * {@link StringTagExtractor} extracting the {@code tagName} tag
     * @see StringTagExtractor
     */
    public static StringTagExtractorFactory stringTagExtractor(final String tagName) {
        return new StringTagExtractorFactory(tagName);
    }

}
