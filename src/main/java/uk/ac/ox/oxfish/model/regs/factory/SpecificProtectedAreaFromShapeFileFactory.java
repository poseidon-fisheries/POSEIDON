package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import sim.field.geo.GeomVectorField;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.GISReaders;

import java.nio.file.Path;
import java.nio.file.Paths;

import static uk.ac.ox.oxfish.utility.MasonUtils.bagToSet;

public class SpecificProtectedAreaFromShapeFileFactory extends SpecificProtectedAreaFactory {

    private Path shapeFilePath;

    @SuppressWarnings("unused") public SpecificProtectedAreaFromShapeFileFactory() { this.shapeFilePath = Paths.get("inputs"); }
    public SpecificProtectedAreaFromShapeFileFactory(Path shapeFilePath) { this.shapeFilePath = shapeFilePath; }

    @SuppressWarnings("unused") public Path getShapeFilePath() { return shapeFilePath; }
    @SuppressWarnings("unused") public void setShapeFilePath(Path shapeFilePath) { this.shapeFilePath = shapeFilePath; }

    @Override ImmutableSet<MasonGeometry> buildGeometries(@NotNull FishState fishState) {
        final GeomVectorField geomVectorField = GISReaders.readShapeAndMergeWithRaster(
            fishState.getMap().getRasterBathymetry(),
            shapeFilePath.toString()
        );
        return bagToSet(geomVectorField.getGeometries());
    }
}
