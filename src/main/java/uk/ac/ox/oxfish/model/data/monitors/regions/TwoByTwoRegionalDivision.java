package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.MapExtent;

import java.util.Collection;

public class TwoByTwoRegionalDivision implements RegionalDivision {

    private static final ImmutableList<Region> REGIONS = ImmutableList.of(
        new Region(0, "Northwest"),
        new Region(1, "Northeast"),
        new Region(2, "Southwest"),
        new Region(3, "Southeast")
    );
    private final MapExtent mapExtent;
    private final int middleX;
    private final int middleY;

    public TwoByTwoRegionalDivision(MapExtent mapExtent, final int middleX, final int middleY) {
        this.mapExtent = mapExtent;
        this.middleX = middleX;
        this.middleY = middleY;
    }

    public static TwoByTwoRegionalDivision from(final Coordinate middleCoordinate, final MapExtent mapExtent) {
        final Double2D xy = mapExtent.coordinateToXY(middleCoordinate);
        return new TwoByTwoRegionalDivision(mapExtent, (int) xy.x, (int) xy.y);
    }

    @Override
    public MapExtent getMapExtent() {
        return mapExtent;
    }

    @Override
    public Collection<Region> getRegions() {
        return REGIONS;
    }

    @Override
    public Region getRegion(int gridX, int gridY) {
        return REGIONS.get(
            gridY <= middleY
                ? (gridX <= middleX ? 0 : 1)
                : (gridX <= middleX ? 2 : 3)
        );
    }

}
