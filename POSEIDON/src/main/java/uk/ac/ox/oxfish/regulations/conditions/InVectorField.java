package uk.ac.ox.oxfish.regulations.conditions;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import sim.field.geo.GeomVectorField;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.GISReaders;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class InVectorField implements AlgorithmFactory<Condition> {

    private static final Cache<Entry<MapExtent, Path>, Condition> cache =
        CacheBuilder.newBuilder().build();

    private InputPath shapeFilePath;

    public InVectorField(final InputPath shapeFilePath) {
        this.shapeFilePath = shapeFilePath;
    }

    public InVectorField() {
    }

    private GeomVectorField readShapeFile() {
        return GISReaders.readShapeFile(getShapeFilePath().toString());
    }

    public InputPath getShapeFilePath() {
        return shapeFilePath;
    }

    public void setShapeFilePath(final InputPath shapeFilePath) {
        this.shapeFilePath = shapeFilePath;
    }

    @Override
    public Condition apply(final FishState fishState) {
        final MapExtent mapExtent = fishState.getMap().getMapExtent();
        final Path path = shapeFilePath.get();
        try {
            return cache.get(entry(mapExtent, path), () -> {
                final GeomVectorField vectorField = GISReaders.readShapeFile(path.toString());
                vectorField.setMBR(mapExtent.getEnvelope());
                return new uk.ac.ox.poseidon.regulations.core.conditions.InVectorField(vectorField);
            });
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
