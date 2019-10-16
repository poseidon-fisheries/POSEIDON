package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableSet;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A regulation that enforces a single, specific protected area.
 */
public class SpecificProtectedArea implements Regulation {

    private final ImmutableSet<MasonGeometry> masonGeometries;

    public SpecificProtectedArea(ImmutableSet<MasonGeometry> masonGeometries) { this.masonGeometries = masonGeometries; }
    public SpecificProtectedArea(MasonGeometry masonGeometry) { this.masonGeometries = ImmutableSet.of(masonGeometry); }

    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) { return !masonGeometries.contains(tile.grabMPA()); }

    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) { return Double.MAX_VALUE; }

    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) { return true; }

    @Override
    public Regulation makeCopy() { return new SpecificProtectedArea(masonGeometries); }

}
