package uk.ac.ox.oxfish.geography.currents;

import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;
import java.util.Optional;

import static uk.ac.ox.oxfish.utility.MasonUtils.inBounds;

public class VectorGrid2D {

    private final DoubleGrid2D uGrid;
    private final DoubleGrid2D vGrid;

    VectorGrid2D(int width, int height, Map<SeaTile, Double2D> vectors) {
        uGrid = new DoubleGrid2D(width, height, 0.0);
        vGrid = new DoubleGrid2D(width, height, 0.0);
        vectors.forEach((seaTile, vector) -> {
            uGrid.set(seaTile.getGridX(), seaTile.getGridY(), vector.x);
            vGrid.set(seaTile.getGridX(), seaTile.getGridY(), vector.y);
        });
    }

    public Optional<Double2D> move(Double2D xy) {
        final int x = (int) xy.x;
        final int y = (int) xy.y;
        return Optional
            .of(new Double2D(xy.x + uGrid.field[x][y], xy.y + vGrid.field[x][y]))
            .filter(location -> inBounds(location, uGrid));
    }

}
