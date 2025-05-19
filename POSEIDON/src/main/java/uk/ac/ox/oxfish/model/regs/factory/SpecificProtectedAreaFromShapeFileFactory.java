/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
