package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.function.BiPredicate;

abstract public class SpecificProtectedAreaFactory implements AlgorithmFactory<SpecificProtectedArea> {

    private String name;
    private final CacheByFishState<SpecificProtectedArea> cache =
        new CacheByFishState<>(fishState ->
            new SpecificProtectedArea(makeInAreaArray(fishState), name)
        );

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public SpecificProtectedArea apply(FishState fishState) {
        return cache.get(fishState);
    }

    abstract BiPredicate<Integer, Integer> inAreaPredicate(final FishState fishState);

    public boolean[][] makeInAreaArray(
        final FishState fishState
    ) {
        int w = fishState.getMap().getWidth();
        int h = fishState.getMap().getHeight();
        final BiPredicate<Integer, Integer> inAreaPredicate = inAreaPredicate(fishState);
        final boolean[][] inArea = new boolean[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                inArea[x][y] = inAreaPredicate.test(x, y);
            }
        }
        return inArea;
    }

}