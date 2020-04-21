package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.ImmutableSet;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.SpecificProtectedArea;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

abstract public class SpecificProtectedAreaFactory implements AlgorithmFactory<SpecificProtectedArea> {

    private final Locker<FishState, SpecificProtectedArea> specificProtectedAreaLocker = new Locker<>();

    abstract ImmutableSet<MasonGeometry> buildGeometries(FishState fishState);

    private SpecificProtectedArea buildSpecificProtectedArea(FishState fishState) {
        ImmutableSet<MasonGeometry> geometries = buildGeometries(fishState);
        fishState.registerStartable(model -> {
            final NauticalMap map = model.getMap();
            geometries.forEach(map.getMpaVectorField()::addGeometry);
            map.recomputeTilesMPA(); // this destroys MPAs created through StartingMPA::buildMPA
        });
        return new SpecificProtectedArea(geometries);
    }

    @Override
    public SpecificProtectedArea apply(FishState fishState) {
        return specificProtectedAreaLocker.presentKey(fishState, () -> buildSpecificProtectedArea(fishState));
    }
}
