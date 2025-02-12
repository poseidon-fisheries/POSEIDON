/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.geography.vectors;

import lombok.*;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.GridExtent;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VectorFieldFromShapeFileFactory extends GlobalScopeFactory<GeomVectorField> {

    @NonNull private Factory<? extends Path> path;
    @NonNull private Factory<? extends GridExtent> gridExtent;

    @Override
    protected GeomVectorField newInstance(final Simulation simulation) {
        final Path path = this.path.get(simulation);
        final GridExtent gridExtent = this.gridExtent.get(simulation);
        final GeomVectorField vectorField = readShapeFile(path.toFile());
        vectorField.setMBR(gridExtent.getEnvelope().toJTS());
        return vectorField;
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
