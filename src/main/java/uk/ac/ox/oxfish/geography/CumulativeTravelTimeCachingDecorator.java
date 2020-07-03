package uk.ac.ox.oxfish.geography;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class CumulativeTravelTimeCachingDecorator implements Distance {

    private static final long CACHE_SIZE = 500;

    private final Distance delegate;

    // A cache from endpoints to a map from speed to cumulative distances along route
    private final LoadingCache<Entry<SeaTile, SeaTile>, Map<Double, List<Entry<SeaTile, Double>>>> cache =
        CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(CacheLoader.from(__ -> new HashMap<>()));

    public CumulativeTravelTimeCachingDecorator(Distance delegate) { this.delegate = delegate; }

    @Override public List<Entry<SeaTile, Double>> cumulativeTravelTimeAlongRouteInHours(
        Deque<SeaTile> route,
        NauticalMap map,
        double speedInKph
    ) {
        return cache.getUnchecked(entry(route.getFirst(), route.getLast())).computeIfAbsent(
            speedInKph,
            __ -> Distance.super.cumulativeTravelTimeAlongRouteInHours(route, map, speedInKph)
        );
    }

    @Override public double distance(SeaTile start, SeaTile end, NauticalMap map) {
        return delegate.distance(start, end, map);
    }
}
