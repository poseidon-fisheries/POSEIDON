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

package uk.ac.ox.poseidon.gui.palettes;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.Getter;
import sim.util.gui.AbstractColorMap;

import java.awt.*;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * A MASON {@link AbstractColorMap} that interpolates over a scientific colour palette loaded
 * from a {@code /palettes/*.txt} resource, clamping the input value to {@code [minimum, maximum]}
 * and mapping {@link Double#NaN} to a transparent colour.
 */
@Getter
public class PaletteColorMap extends AbstractColorMap {

    /** Name of the "imola" palette resource. */
    public static final String IMOLA = "imola";
    /** Name of the "lajolla" palette resource. */
    public static final String LAJOLLA = "lajolla";
    /** Name of the "oleron" palette resource. */
    public static final String OLERON = "oleron";
    /** Name of the "turku" palette resource. */
    public static final String TURKU = "turku";
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    /** The palette's colours, in order from {@link #minimum} to {@link #maximum}. */
    private final Color[] colors;
    /** The value that maps to the first colour in {@link #colors}. */
    private final double minimum;
    /** The value that maps to the last colour in {@link #colors}. */
    private final double maximum;

    /**
     * Loads the named palette (one of {@link #IMOLA}, {@link #LAJOLLA}, {@link #OLERON},
     * {@link #TURKU}) from its {@code /palettes/<mapName>.txt} resource.
     *
     * @param mapName the palette's resource name
     * @param minimum the value that maps to the first colour
     * @param maximum the value that maps to the last colour, must be greater than {@code minimum}
     */
    public PaletteColorMap(
        final String mapName,
        final double minimum,
        final double maximum
    ) {
        this(
            loadColors(mapName),
            minimum,
            maximum
        );
    }

    /**
     * Builds a colour map from an explicit array of colours.
     *
     * @param colors  the palette's colours, in order from {@code minimum} to {@code maximum};
     *                must contain at least two colours, and is defensively copied
     * @param minimum the value that maps to {@code colors[0]}
     * @param maximum the value that maps to the last colour in {@code colors}, must be greater
     *                than {@code minimum}
     */
    public PaletteColorMap(
        final Color[] colors,
        final double minimum,
        final double maximum
    ) {
        checkNotNull(colors);
        checkArgument(colors.length > 1);
        checkArgument(minimum < maximum);
        this.colors = colors.clone();
        this.minimum = minimum;
        this.maximum = maximum;
    }

    private static Color[] loadColors(final String mapName) {
        return loadFromInputStream(
            PaletteColorMap.class.getResourceAsStream("/palettes/" + mapName + ".txt")
        );
    }

    private static Color[] loadFromInputStream(
        final InputStream colourTableText
    ) {
        final CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setDelimiter(' ');
        settings.setHeaderExtractionEnabled(false);
        return new CsvParser(settings)
            .parseAll(colourTableText)
            .stream()
            .map(row -> new Color(
                Float.parseFloat(row[0]),
                Float.parseFloat(row[1]),
                Float.parseFloat(row[2])
            ))
            .toArray(Color[]::new);
    }

    /**
     * @return the transparent colour if {@code v} is {@link Double#NaN}, otherwise the palette
     * colour for {@code v} clamped to {@code [minimum, maximum]} and linearly interpolated over
     * {@link #colors}
     */
    @Override
    public Color getColor(final double v) {
        if (Double.isNaN(v)) {
            return TRANSPARENT;
        } else {
            final double boundedValue = min(max(v, minimum), maximum);
            final double interpolation = (boundedValue - minimum) / (maximum - minimum);
            final long index = Math.round(interpolation * (colors.length - 1));
            return colors[(int) index];
        }
    }

}
