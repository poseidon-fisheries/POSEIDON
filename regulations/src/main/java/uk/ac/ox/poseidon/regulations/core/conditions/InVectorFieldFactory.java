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

package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import static uk.ac.ox.poseidon.common.core.Entry.entry;

public class InVectorFieldFactory implements ComponentFactory<Condition> {

    private static final Cache<Entry<MapExtent, Path>, Condition> cache =
        CacheBuilder.newBuilder().build();

    private InputPath shapeFilePath;

    public InVectorFieldFactory(final InputPath shapeFilePath) {
        this.shapeFilePath = shapeFilePath;
    }

    @SuppressWarnings("unused")
    public InVectorFieldFactory() {
    }

    @SuppressWarnings("unused")
    public InputPath getShapeFilePath() {
        return shapeFilePath;
    }

    @SuppressWarnings("unused")
    public void setShapeFilePath(final InputPath shapeFilePath) {
        this.shapeFilePath = shapeFilePath;
    }

    @Override
    public Condition apply(final ModelState modelState) {
        final MapExtent mapExtent = modelState.getMapExtent();
        final Path path = shapeFilePath.get();
        try {
            return cache.get(entry(mapExtent, path), () -> {
                final GeomVectorField vectorField = readShapeFile(path.toFile());
                vectorField.setMBR(mapExtent.getEnvelope());
                return new uk.ac.ox.poseidon.regulations.core.conditions.InVectorField(vectorField);
            });
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private GeomVectorField readShapeFile(final File path) {
        final GeomVectorField vectorField = new GeomVectorField();
        try {
            ShapeFileImporter.read(path.toURI().toURL(), vectorField);
        } catch (final FileNotFoundException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return vectorField;
    }
}
