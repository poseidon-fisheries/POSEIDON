package uk.ac.ox.oxfish.model.regs.factory;

import com.vividsolutions.jts.geom.Point;
import sim.field.geo.GeomVectorField;
import uk.ac.ox.oxfish.utility.GISReaders;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.nio.file.Path;
import java.util.function.BiPredicate;

import static com.google.common.io.MoreFiles.getNameWithoutExtension;

public class SpecificProtectedAreaFromShapeFileFactory extends SpecificProtectedAreaFactory {

    private Path shapeFilePath;

    @SuppressWarnings("unused")
    public SpecificProtectedAreaFromShapeFileFactory() {
    }

    public SpecificProtectedAreaFromShapeFileFactory(
        final Path shapeFilePath
    ) {
        // noinspection UnstableApiUsage
        this(shapeFilePath, getNameWithoutExtension(shapeFilePath.getFileName()));
    }

    public SpecificProtectedAreaFromShapeFileFactory(
        final Path shapeFilePath,
        final String name
    ) {
        this.shapeFilePath = shapeFilePath;
        this.setName(name);
    }

    @Override
    BiPredicate<Integer, Integer> inAreaPredicate(final MapExtent mapExtent) {
        final GeomVectorField vectorField = readShapeFile();
        vectorField.setMBR(mapExtent.getEnvelope());
        return (x, y) -> {
            final Point gridPoint = mapExtent.toPoint(x, y);
            return !vectorField.getCoveringObjects(gridPoint).isEmpty();
        };
    }

    private GeomVectorField readShapeFile() {
        return GISReaders.readShapeFile(getShapeFilePath().toString());
    }

    @SuppressWarnings("unused")
    public Path getShapeFilePath() {
        return shapeFilePath;
    }

    @SuppressWarnings("unused")
    public void setShapeFilePath(final Path shapeFilePath) {
        this.shapeFilePath = shapeFilePath;
    }

}
