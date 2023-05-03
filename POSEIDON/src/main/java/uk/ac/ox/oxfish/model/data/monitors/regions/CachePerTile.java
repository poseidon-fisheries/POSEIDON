package uk.ac.ox.oxfish.model.data.monitors.regions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.geography.MapExtent;

import java.util.function.Function;

public class CachePerTile<T> {

    private final LoadingCache<Int2D, T> cache;
    private final MapExtent mapExtent;

    public CachePerTile(
        final MapExtent mapExtent,
        final Function<? super Int2D, T> loaderFunction
    ) {
        this.mapExtent = mapExtent;
        this.cache = CacheBuilder.newBuilder().build(CacheLoader.from(loaderFunction::apply));
    }

    public MapExtent getMapExtent() {
        return mapExtent;
    }

    public T get(final Coordinate coordinate) {
        return get(mapExtent.coordinateToXY(coordinate));
    }

    public T get(final Double2D gridXY) {
        return get(new Int2D((int) gridXY.x, (int) gridXY.y));
    }

    public T get(final Int2D gridXY) {
        return cache.getUnchecked(gridXY);
    }

}
