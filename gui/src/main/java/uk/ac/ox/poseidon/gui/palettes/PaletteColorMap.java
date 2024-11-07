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

@Getter
public class PaletteColorMap extends AbstractColorMap {

    public static final String IMOLA = "imola";
    public static final String OLERON = "oleron";
    public static final String TURKU = "turku";
    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private final Color[] colors;
    private final double minimum;
    private final double maximum;

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

    public PaletteColorMap(
        final Color[] colors,
        final double minimum,
        final double maximum
    ) {
        checkNotNull(colors);
        checkArgument(colors.length > 1);
        checkArgument(minimum < maximum);
        this.colors = colors;
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
