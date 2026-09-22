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

package uk.ac.ox.poseidon.geography.grids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.geotools.api.geometry.Bounds;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.geography.Envelope;

import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Wraps a GeoTools {@link GridCoverage2D} (a raster grid, e.g. read from a GeoTIFF or ASCII grid
 * file) with convenience accessors used across this module to build a {@link ModelGrid} or read
 * per-cell values from raster files.
 */
@Getter
@RequiredArgsConstructor
public final class CoverageWrapper {

    private final GridCoverage2D coverage;

    /** @param gridFile the raster grid file to read, in any format GeoTools can auto-detect */
    public CoverageWrapper(final File gridFile) {
        this(readCoverage(gridFile));
    }

    /**
     * @param gridFile the raster grid file to read, in any format GeoTools can auto-detect
     * @return the file's content as a GeoTools coverage
     * @throws RuntimeException if an I/O error occurs
     */
    public static GridCoverage2D readCoverage(final File gridFile) {
        final AbstractGridFormat format = GridFormatFinder.findFormat(gridFile);
        final GridCoverage2DReader reader = format.getReader(gridFile);
        try {
            final GridCoverage2D coverage = reader.read();
            reader.dispose();
            return coverage;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param action called once per raster cell, with its (x, y) position and its value (only the
     *               first band is read)
     */
    public void processGrid(
        final BiConsumer<Int2D, Double> action
    ) {
        final RenderedImage image = coverage.getRenderedImage();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final Raster raster = image.getData();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                action.accept(new Int2D(x, y), raster.getSampleDouble(x, y, 0));
            }
        }
    }

    /** @return the raster's width, in cells */
    public int getGridWidth() {
        return coverage.getRenderedImage().getWidth();
    }

    /** @return the raster's height, in cells */
    public int getGridHeight() {
        return coverage.getRenderedImage().getHeight();
    }

    /** @return the raster's geographic bounding box, as an {@link Envelope} */
    public Envelope makeEnvelope() {
        final Bounds bounds = coverage.getEnvelope();
        return new Envelope(
            bounds.getMinimum(0),
            bounds.getMaximum(0),
            bounds.getMinimum(1),
            bounds.getMaximum(1)
        );
    }
}
