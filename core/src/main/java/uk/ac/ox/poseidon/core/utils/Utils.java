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

package uk.ac.ox.poseidon.core.utils;

public class Utils {

    private Utils() {}

    public static final char STRING_KEY_SEPARATOR = ';';

    public static String toTrimmedString(
        final Object value,
        final boolean nullIfNa
    ) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.toString().trim();
        if (nullIfNa && (trimmed.isEmpty() || trimmed.equalsIgnoreCase("NA"))) {
            return null;
        }
        return trimmed;
    }

    /**
     * Joins trimmed non-null fields with {@link #STRING_KEY_SEPARATOR} to produce a
     * map-friendly compound key. The separator character in any field value is rejected
     * with {@link IllegalArgumentException}, as is ')' which would otherwise interfere
     * with bean property syntax.
     * <p>
     * Null, empty, and "NA" fields (after trimming) are treated as absent and
     * produce no contribution beyond their separator.
     */
    public static String multiStringKey(final String... fields) {
        final var sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(STRING_KEY_SEPARATOR);
            }
            final String s = toTrimmedString(fields[i], true);
            if (s != null) {
                if (s.indexOf(STRING_KEY_SEPARATOR) >= 0) {
                    throw new IllegalArgumentException(
                        "Field contains reserved character '" +
                            STRING_KEY_SEPARATOR + "': " + fields[i]);
                }
                if (s.indexOf(')') >= 0) {
                    throw new IllegalArgumentException(
                        "Field contains reserved character ')': " + fields[i]);
                }
                sb.append(s);
            }
        }
        return sb.toString();
    }

}
