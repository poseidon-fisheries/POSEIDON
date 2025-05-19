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

package uk.ac.ox.poseidon.gui.portrayals;

import lombok.RequiredArgsConstructor;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class SvgRenderer {

    private final SVGDocument svgDocument;

    public static SvgRenderer from(final InputStream svgInputStream) {
        final String parser = XMLResourceDescriptor.getXMLParserClassName();
        final SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        try {
            return new SvgRenderer(factory.createSVGDocument(null, svgInputStream));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage render(
        final int width,
        final int height
    ) throws Exception {
        final CustomImageTranscoder transcoder = new CustomImageTranscoder(width, height);
        final TranscoderInput input = new TranscoderInput(svgDocument);
        transcoder.transcode(input, null);
        return transcoder.getBufferedImage();
    }

    public void draw(
        final Graphics2D g,
        final int x,
        final int y,
        final int width,
        final int height
    ) {
        try {
            final BufferedImage image = render(width, height);
            g.drawImage(image, x, y, null);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to draw SVG document", e);
        }
    }

    // Inner class for handling the image transcoding
    private static class CustomImageTranscoder extends ImageTranscoder {
        private BufferedImage image;

        public CustomImageTranscoder(
            final int width,
            final int height
        ) {
            this.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) width);
            this.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) height);
        }

        @Override
        public BufferedImage createImage(
            final int width,
            final int height
        ) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            return image;
        }

        @Override
        public void writeImage(
            final BufferedImage img,
            final TranscoderOutput output
        ) {
            // Image is already captured in createImage(), so no need to do anything here
        }

        public BufferedImage getBufferedImage() {
            return image;
        }
    }
}
