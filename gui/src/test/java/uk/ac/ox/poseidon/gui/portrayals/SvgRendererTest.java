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

package uk.ac.ox.poseidon.gui.portrayals;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class SvgRendererTest {

  @Test
  void drawsTheSameImageAgainAndAtEachSize() throws Exception {
    final SvgRenderer renderer;
    try (InputStream svg = getClass().getResourceAsStream("/images/port.svg")) {
      renderer = SvgRenderer.from(svg);
    }
    for (final int size : new int[] { 20, 40, 20 }) {
      final BufferedImage canvas = new BufferedImage(
        size,
        size,
        BufferedImage.TYPE_INT_ARGB
      );
      final Graphics2D g = canvas.createGraphics();
      renderer.draw(g, 0, 0, size, size);
      g.dispose();
      assertThat(pixels(canvas)).isEqualTo(pixels(renderer.render(size, size)));
    }
  }

  private static int[] pixels(final BufferedImage image) {
    return image.getRGB(
      0,
      0,
      image.getWidth(),
      image.getHeight(),
      null,
      0,
      image.getWidth()
    );
  }
}
