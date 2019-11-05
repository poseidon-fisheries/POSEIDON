package uk.ac.ox.oxfish.geography;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Deque;

public class CumulativeDistanceCachingDecorator implements Distance {

    private final Distance delegate;
    private Table<SeaTile, SeaTile, ImmutableList<Pair<SeaTile, Double>>> memory = HashBasedTable.create();

    public CumulativeDistanceCachingDecorator(Distance delegate) { this.delegate = delegate; }

    @Override public ImmutableList<Pair<SeaTile, Double>> cumulativeDistanceAlongRoute(Deque<SeaTile> route, NauticalMap map) {
        final SeaTile startTile = route.getFirst();
        final SeaTile endTile = route.getLast();
        final ImmutableList<Pair<SeaTile, Double>> cachedResult = memory.get(startTile, endTile);
        if (cachedResult != null) return cachedResult;
        final ImmutableList<Pair<SeaTile, Double>> result = Distance.super.cumulativeDistanceAlongRoute(route, map);
        memory.put(startTile, endTile, result);
        return result;
    }

    @Override public double distance(SeaTile start, SeaTile end, NauticalMap map) {
        return delegate.distance(start, end, map);
    }
}
