package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class CustomRegionalDivision extends RegionalDivision {

    private final List<Region> regions;

    public CustomRegionalDivision(
        final MapExtent mapExtent,
        final List<Region> regions
    ) {
        super(mapExtent);
        this.regions = ImmutableList.copyOf(regions);
    }

    public CustomRegionalDivision(
        final MapExtent mapExtent,
        final Map<String, Entry<Coordinate, Coordinate>> regions
    ) {
        super(mapExtent);
        this.regions = regions.entrySet().stream()
            .map(entry -> {
                final String name = entry.getKey();
                final Entry<Coordinate, Coordinate> corners = entry.getValue();
                final Double2D topLeftCorner = mapExtent.coordinateToXY(corners.getKey());
                final Double2D bottomRightCorner = mapExtent.coordinateToXY(corners.getValue());
                return new Region(
                    name,
                    (int) topLeftCorner.x,
                    (int) bottomRightCorner.x,
                    (int) topLeftCorner.y,
                    (int) bottomRightCorner.y
                );
            })
            .collect(toImmutableList());
    }

    @Override
    public Collection<Region> getRegions() {
        return regions;
    }
}
