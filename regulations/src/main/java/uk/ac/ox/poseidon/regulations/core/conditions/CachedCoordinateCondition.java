package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

public abstract class CachedCoordinateCondition implements Condition {

    private final LoadingCache<Coordinate, Boolean> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::test));

    @Override
    public boolean test(final Action action) {
        return action
            .getCoordinate()
            .map(cache::getUnchecked)
            .orElse(false);
    }

    abstract public boolean test(final Coordinate coordinate);

}
