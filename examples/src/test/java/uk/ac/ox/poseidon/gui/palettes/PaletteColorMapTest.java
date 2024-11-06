/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.gui.palettes;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.ac.ox.poseidon.gui.palettes.PaletteColorMap.OLERON;

class PaletteColorMapTest {

    private final double max = 5;
    private final double min = -max;
    private final PaletteColorMap colorMap = new PaletteColorMap(OLERON, min, max);

    @Test
    void getColor() {
        final Color[] palette = colorMap.getColors();
        Map.of(
            min - 1, 0,
            min, 0,
            -1E-10, 127,
            0.0, 128,
            max, palette.length - 1,
            max + 1, palette.length - 1
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
