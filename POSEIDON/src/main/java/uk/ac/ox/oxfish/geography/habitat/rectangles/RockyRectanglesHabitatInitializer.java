/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.habitat.rectangles;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.HabitatInitializer;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates random rectangles of purely rocky areas, and assumes everywhere else you have sandy tiles Created by
 * carrknight on 9/28/15.
 */
public class RockyRectanglesHabitatInitializer implements HabitatInitializer {

    public static final String ROCKY_FISHING_INTENSITY = "% Rocky Fishing Intensity";
    public static final String BORDER_FISHING_INTENSITY = "% Rocky Border Fishing Intensity";

    private final RockyRectangleMaker maker;

    private List<SeaTile> rockyTiles = new ArrayList<>();

    private List<SeaTile> borderTiles = new ArrayList<>();

    public RockyRectanglesHabitatInitializer(
        final int minRockyWidth,
        final int maxRockyWidth,
        final int minRockyHeight,
        final int maxRockyHeight,
        final int numberOfRectangles
    ) {
        this(new RandomRockyRectangles(
            new UniformDoubleParameter(minRockyWidth, maxRockyHeight),
            new UniformDoubleParameter(minRockyHeight, maxRockyHeight),
            numberOfRectangles
        ));
        Preconditions.checkArgument(minRockyWidth > 0);
        Preconditions.checkArgument(minRockyHeight > 0);
        Preconditions.checkArgument(maxRockyWidth >= minRockyWidth);
        Preconditions.checkArgument(maxRockyHeight >= minRockyHeight);

    }

    public RockyRectanglesHabitatInitializer(final RockyRectangleMaker maker) {
        this.maker = maker;
    }

    public RockyRectanglesHabitatInitializer(
        final int x,
        final int y,
        final int width,
        final int height
    ) {
        this((random, map) -> new RockyRectangle[]{new RockyRectangle(x, y, width, height)});
    }

    public RockyRectanglesHabitatInitializer(
        final DoubleParameter rockyHeight,
        final DoubleParameter rockyWidth,
        final int numberOfRectangles
    ) {
        this(new RandomRockyRectangles(rockyHeight, rockyWidth, numberOfRectangles));
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param map   the input argument
     * @param model
     */
    @Override
    public void applyHabitats(
        final NauticalMap map,
        final MersenneTwisterFast random,
        final FishState model
    ) {

        // here I assume everything is sandy at first. I do not force it in case at some point I want to chain a series
        // of initializers (unlikely as it is)

        // ask the maker to give you rectangles
        final RockyRectangle[] rectangles = maker.buildRectangles(random, map);

        // turn rectangles into the real thing
        for (final RockyRectangle rectangle : rectangles) {
            // strip the rectangle class
            final int x = rectangle.getTopLeftX();
            final int y = rectangle.getTopLeftY();
            final int rockyWidth = rectangle.getWidth();
            final int rockyHeight = rectangle.getHeight();
            // for each tile in the rectangle
            for (int w = 0; w < rockyWidth; w++) {
                for (int h = 0; h < rockyHeight; h++) {

                    final SeaTile tile = map.getSeaTile(x + w, y + h);
                    // if it's in the sea
                    if (tile != null && tile.isWater()) {
                        // make it rocky
                        tile.setHabitat(new TileHabitat(1d));
                        rockyTiles.add(tile);
                    }

                }
                // get the border if it exists
                SeaTile border = map.getSeaTile(x + w, y + rockyHeight);
                if (border != null && border.isWater())
                    borderTiles.add(border);
                border = map.getSeaTile(x + w, y - 1);
                if (border != null && border.isWater())
                    borderTiles.add(border);

            }

            // find borders
            for (int h = 0; h < rockyHeight; h++) {

                // lower border
                SeaTile border = map.getSeaTile(x - 1, y + h);
                // if it's in the sea
                if (border != null && border.isWater()) {
                    borderTiles.add(border);
                }
                // higher border
                border = map.getSeaTile(x + rockyWidth, y + h);
                if (border != null && border.isWater()) {
                    borderTiles.add(border);
                }

            }

        }

        // rectangles could have covered previously border tiles
        rockyTiles = rockyTiles.stream().distinct().collect(Collectors.toList());
        borderTiles = borderTiles.stream().distinct().collect(Collectors.toList());

        borderTiles.removeAll(rockyTiles);

        model.getDailyDataSet().registerGatherer(ROCKY_FISHING_INTENSITY,
            state -> {
                final double total = Arrays.stream(map.getDailyTrawlsMap().toArray()).sum();
                double rocky = 0;
                for (final SeaTile tile : rockyTiles)
                    rocky += map.getDailyTrawlsMap().field[tile.getGridX()][tile.getGridY()];

                return 100 * rocky / total;

            }, Double.NaN
        );

        model.getDailyDataSet().registerGatherer(BORDER_FISHING_INTENSITY,
            state -> {
                final double total = Arrays.stream(map.getDailyTrawlsMap().toArray()).sum();
                double rocky = 0;
                for (final SeaTile tile : borderTiles)
                    rocky += map.getDailyTrawlsMap().field[tile.getGridX()][tile.getGridY()];

                return 100 * rocky / total;

            }, Double.NaN
        );

    }
}



