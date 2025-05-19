/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.gui.palettes;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.OLERON;

class PaletteColorMapTest {

    private static final double MAX = 5;
    private static final double MIN = -MAX;
    private final PaletteColorMap colorMap = new PaletteColorMap(OLERON, MIN, MAX);

    @Test
    void getColor() {
        final Color[] palette = colorMap.getColors();
        Map.of(
            MIN - 1, 0,
            MIN, 0,
            -1E-10, 127,
            0.0, 128,
            MAX, palette.length - 1,
            MAX + 1, palette.length - 1
        ).forEach((value, index) -> {
            final Color expected = palette[index];
            final Color actual = colorMap.getColor(value);
            assertEquals(
                expected,
                actual,
                () -> "Expected " + value + " but got " + actual + " for value " + value
            );
        });
    }
}
