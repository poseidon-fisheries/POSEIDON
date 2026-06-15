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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

import java.util.function.Function;

import static uk.ac.ox.poseidon.core.utils.Utils.toTrimmedString;

@RequiredArgsConstructor
public class DoubleTagExtractor implements Function<Vessel, Double> {

    @NonNull private final String tagName;

    private static Double toDouble(final Object value) {
        if (value instanceof final Number number) {
            final double doubleValue = number.doubleValue();
            return Double.isFinite(doubleValue) ? doubleValue : null;
        }
        final String stringValue = toTrimmedString(value, true);
        if (stringValue == null) {
            return null;
        }
        try {
            return Double.valueOf(stringValue);
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public Double apply(final Vessel vessel) {
        return vessel.getTag(tagName)
            .map(DoubleTagExtractor::toDouble)
            .orElse(null);
    }

}
