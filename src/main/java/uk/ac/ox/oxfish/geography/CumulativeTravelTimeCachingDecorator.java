package uk.ac.ox.oxfish.geography;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class CumulativeTravelTimeCachingDecorator implements Distance {

    private final Distance delegate;
    private Map<Double, Table<SeaTile, SeaTile, ImmutableList<Pair<SeaTile, Double>>>> memory = new HashMap<>();

    public CumulativeTravelTimeCachingDecorator(Distance delegate) { this.delegate = delegate; }

    @Override public ImmutableList<Pair<SeaTile, Double>> cumulativeTravelTimeAlongRouteInHours(
        Deque<SeaTile> route,
        NauticalMap map,
        double speedInKph
    ) {
        final SeaTile startTile = route.getFirst();
        final SeaTile endTile = route.getLast();
        final Table<SeaTile, SeaTile, ImmutableList<Pair<SeaTile, Double>>> cache =
            memory.computeIfAbsent(speedInKph, __ -> HashBasedTable.create());
        final ImmutableList<Pair<SeaTile, Double>> cachedResult =
            cache.get(startTile, endTile);
        if (cachedResult != null) return cachedResult;
        final ImmutableList<Pair<SeaTile, Double>> result =
            Distance.super.cumulativeTravelTimeAlongRouteInHours(route, map, speedInKph);
        cache.put(startTile, endTile, result);
        return result;
    }

    @Override public double distance(SeaTile start, SeaTile end, NauticalMap map) {
        return delegate.distance(start, end, map);
    }
}
