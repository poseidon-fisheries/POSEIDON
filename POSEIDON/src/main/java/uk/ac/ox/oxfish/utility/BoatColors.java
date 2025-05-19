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

package uk.ac.ox.oxfish.utility;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.awt.*;
import java.util.Map;

public class BoatColors {
    public final static Map<String, Color> BOAT_COLORS =
        ImmutableMap.<String, Color>builder()
            .put("black", Color.black)
            .put("red", Color.red)
            .put("blue", Color.blue)
            .put("yellow", Color.yellow)
            .put("green", Color.green)
            .put("grey", Color.gray)
            .put("gray", Color.gray)
            .put("pink", Color.pink)
            .put("salmon", new Color(250, 128, 114))
            .put("palevioletred", new Color(219, 112, 147))
            .put("teal", new Color(0, 128, 128))
            .put("wheat", new Color(245, 222, 179))
            .build();

    public static boolean hasColorTag(final Fisher fisher) {
        return fisher.getTagsList().stream().anyMatch(BOAT_COLORS::containsKey);
    }
}
