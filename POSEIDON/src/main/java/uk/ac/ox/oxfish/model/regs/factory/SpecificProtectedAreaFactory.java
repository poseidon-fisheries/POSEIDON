package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.util.Map.Entry;
import java.util.function.BiPredicate;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

abstract public class SpecificProtectedAreaFactory implements AlgorithmFactory<SpecificProtectedArea> {

    private final LoadingCache<Entry<String, MapExtent>, SpecificProtectedArea> cache =
        CacheBuilder.newBuilder().build(
            CacheLoader.from(entry -> new SpecificProtectedArea(makeInAreaArray(entry.getValue()), entry.getKey()))
        );
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public SpecificProtectedArea apply(final FishState fishState) {
        return cache.getUnchecked(entry(name, fishState.getMap().getMapExtent()));
    }

    public boolean[][] makeInAreaArray(
        final MapExtent mapExtent
    ) {
        final int w = mapExtent.getGridWidth();
        final int h = mapExtent.getGridHeight();
        final BiPredicate<Integer, Integer> inAreaPredicate = inAreaPredicate(mapExtent);
        final boolean[][] inArea = new boolean[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                inArea[x][y] = inAreaPredicate.test(x, y);
            }
        }
        return inArea;
    }

    abstract BiPredicate<Integer, Integer> inAreaPredicate(final MapExtent mapExtent);

}
