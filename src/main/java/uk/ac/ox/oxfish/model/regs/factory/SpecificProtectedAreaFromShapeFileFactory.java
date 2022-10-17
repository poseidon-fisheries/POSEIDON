package uk.ac.ox.oxfish.model.regs.factory;

import com.vividsolutions.jts.geom.Point;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.GISReaders;

import java.nio.file.Path;
import java.util.function.BiPredicate;

import static com.google.common.io.MoreFiles.getNameWithoutExtension;

public class SpecificProtectedAreaFromShapeFileFactory extends SpecificProtectedAreaFactory {

    private Path shapeFilePath;

    @SuppressWarnings("unused")
    public SpecificProtectedAreaFromShapeFileFactory() {
    }

    public SpecificProtectedAreaFromShapeFileFactory(
        Path shapeFilePath
    ) {
        //noinspection UnstableApiUsage
        this(shapeFilePath, getNameWithoutExtension(shapeFilePath.getFileName()));
    }

    public SpecificProtectedAreaFromShapeFileFactory(
        Path shapeFilePath,
        String name
    ) {
        this.shapeFilePath = shapeFilePath;
        this.setName(name);
    }

    @SuppressWarnings("unused")
    public Path getShapeFilePath() {
        return shapeFilePath;
    }

    @SuppressWarnings("unused")
    public void setShapeFilePath(Path shapeFilePath) {
        this.shapeFilePath = shapeFilePath;
    }

    private GeomVectorField readShapeFile() {
        return GISReaders.readShapeFile(getShapeFilePath().toString());
    }

    @Override
    BiPredicate<Integer, Integer> inAreaPredicate(FishState fishState) {
        final GeomVectorField vectorField = readShapeFile();
        final GeomGridField rasterBathymetry = fishState.getMap().getRasterBathymetry();
        vectorField.setMBR(rasterBathymetry.getMBR());
        return (x, y) -> {
            final Point gridPoint = rasterBathymetry.toPoint(x, y);
            return !vectorField.getCoveringObjects(gridPoint).isEmpty();
        };
    }

}
