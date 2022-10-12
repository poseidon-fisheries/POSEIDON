package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableSet;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.Collection;

/**
 * A regulation that enforces a single, specific protected area.
 * <p>
 * Stores the protected status of each tile in the {@code inArea} array for speed of access.
 * {@code updateProtectedArea} needs to be called if a tile's MPA is ever modified.
 * <p>
 * This is fast but memory hungry, as each fisher gets its own Regulation object. If/when we migrate
 * protected areas to the ActionSpecificRegulation interface, we'll be able to use a single
 * array for the whole simulation.
 */
public class SpecificProtectedArea implements Regulation {

    private final ImmutableSet<MasonGeometry> masonGeometries;
    private boolean[][] inArea;

    public SpecificProtectedArea(Collection<MasonGeometry> masonGeometries) {
        this.masonGeometries = ImmutableSet.copyOf(masonGeometries);
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        Regulation.super.start(model, fisher);
        model.scheduleOnce(
                (Steppable) simState -> updateProtectedArea(model), StepOrder.DAWN
        );

    }

    public void updateProtectedArea(FishState model) {
        NauticalMap map = model.getMap();
        int w = map.getWidth();
        int h = map.getHeight();
        inArea = new boolean[w][h];
        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                inArea[x][y] = masonGeometries.contains(map.getSeaTile(x, y).grabMPA());
    }

    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) { return !isProtected(tile); }

    public boolean isProtected(SeaTile tile) { return inArea[tile.getGridX()][tile.getGridY()]; }

    @Override
    public double maximumBiomassSellable(
        Fisher agent,
        Species species,
        FishState model,
        int timeStep
    ) { return Double.MAX_VALUE; }

    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) { return true; }

    @Override
    public Regulation makeCopy() { return new SpecificProtectedArea(masonGeometries); }

}
