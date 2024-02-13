package uk.ac.ox.poseidon.geography;

import com.univocity.parsers.common.record.Record;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class DoubleGrid {
    private final MapExtent mapExtent;
    private final DoubleGrid2D gridField;

    private DoubleGrid(
        final MapExtent mapExtent,
        final DoubleGrid2D gridField
    ) {
        checkArgument(gridField.getWidth() == mapExtent.getGridWidth());
        checkArgument(gridField.getHeight() == mapExtent.getGridHeight());
        this.mapExtent = mapExtent;
        this.gridField = gridField;
    }

    public static DoubleGrid from(
        final MapExtent mapExtent
    ) {
        return new DoubleGrid(
            mapExtent,
            new DoubleGrid2D(
                mapExtent.getGridWidth(),
                mapExtent.getGridHeight()
            )
        );
    }

    public static DoubleGrid from(
        final MapExtent mapExtent,
        final double initialValue
    ) {
        return new DoubleGrid(
            mapExtent,
            new DoubleGrid2D(
                mapExtent.getGridWidth(),
                mapExtent.getGridHeight(),
                initialValue
            )
        );
    }

    public static DoubleGrid from(
        final MapExtent mapExtent,
        final double[][] values
    ) {
        return new DoubleGrid(mapExtent, new DoubleGrid2D(values));
    }

    public static DoubleGrid fromRecords(
        final MapExtent mapExtent,
        final Stream<? extends Record> records,
        final String longitudeColumnName,
        final String latitudeColumnName,
        final String valueColumnName
    ) {
        final DoubleGrid2D grid = new DoubleGrid2D(
            mapExtent.getGridWidth(),
            mapExtent.getGridHeight()
        );
        records.forEach(record -> {
            final double lon = record.getDouble(longitudeColumnName);
            final double lat = record.getDouble(latitudeColumnName);
            final int x = mapExtent.toGridX(lon);
            final int y = mapExtent.toGridY(lat);
            if (x < grid.getWidth() && y < grid.getHeight() && x >= 0 && y >= 0) {
                grid.set(x, y, record.getDouble(valueColumnName));
            }
        });
        return DoubleGrid.from(mapExtent, grid);
    }

    public static DoubleGrid from(
        final MapExtent mapExtent,
        final DoubleGrid2D values
    ) {
        return new DoubleGrid(mapExtent, new DoubleGrid2D(values));
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

    public final double get(
        final int x,
        final int y
    ) {
        return gridField.get(x, y);
    }

}
