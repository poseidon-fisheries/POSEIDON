package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.MapExtent;

import java.util.Collection;
import java.util.List;

public class TwoByTwoRegionalDivision extends RegionalDivision {

    private final List<Region> regions;

    public TwoByTwoRegionalDivision(
        final Coordinate middleCoordinate,
        final MapExtent mapExtent
    ) {
        this(mapExtent.coordinateToXY(middleCoordinate), mapExtent);
    }

    public TwoByTwoRegionalDivision(
        final Double2D middleGridXY,
        final MapExtent mapExtent
    ) {
        super(mapExtent);
        final int w = mapExtent.getGridWidth();
        final int h = mapExtent.getGridHeight();
        final int x = (int) middleGridXY.x;
        final int y = (int) middleGridXY.y;
        this.regions = ImmutableList.of(
            new Region("Northwest", 0, x, 0, y),
            new Region("Northeast", x + 1, w, 0, y),
            new Region("Southwest", 0, x, y + 1, h),
            new Region("Southeast", x + 1, w, y + 1, h)
        );
    }

    @Override
    public Collection<Region> getRegions() {
        return regions;
    }

}
