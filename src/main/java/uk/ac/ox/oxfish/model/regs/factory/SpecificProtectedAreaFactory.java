package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.ImmutableSet;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

abstract public class SpecificProtectedAreaFactory implements AlgorithmFactory<SpecificProtectedArea> {

    private final CacheByFishState<SpecificProtectedArea> cache =
        new CacheByFishState<>(fishState -> {
            ImmutableSet<MasonGeometry> geometries = buildGeometries(fishState);
            fishState.registerStartable(model -> {
                final NauticalMap map = model.getMap();
                geometries.forEach(map.getMpaVectorField()::addGeometry);
                map.recomputeTilesMPA(); // this destroys MPAs created through StartingMPA::buildMPA
            });
            return new SpecificProtectedArea(geometries);
        });

    @Override
    public SpecificProtectedArea apply(FishState fishState) {
        return cache.get(fishState);
    }

    abstract ImmutableSet<MasonGeometry> buildGeometries(FishState fishState);
}