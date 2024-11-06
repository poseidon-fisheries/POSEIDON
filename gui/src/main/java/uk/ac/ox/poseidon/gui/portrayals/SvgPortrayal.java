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

package uk.ac.ox.poseidon.gui.portrayals;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.RectanglePortrayal2D;

import java.awt.*;
import java.io.InputStream;

public class SvgPortrayal extends RectanglePortrayal2D {

    private final SvgRenderer renderer;

    public SvgPortrayal(final InputStream svgInputStream) {
        this.renderer = new SvgRenderer(svgInputStream);
    }

    @Override
    public void draw(
        final Object object,
        final Graphics2D graphics,
        final DrawInfo2D info
    ) {
        final int width = (int) info.draw.width;
        final int height = (int) info.draw.height;
        renderer.draw(
            graphics,
            (int) info.draw.x - width / 2,
            (int) info.draw.y - height / 2,
            width,
            height
        );
    }
}
