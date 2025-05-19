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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import sim.field.geo.GeomVectorField;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.ShapeFileImporterModified;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PolygonAllocatorFactory implements AlgorithmFactory<PolygonBiomassDecorator> {


    private Path shapeFile = Paths.get("./docs/indonesia_hub/runs/712/shape/WPP_boundary.shp");


    private boolean insidePolygon = true;

    private AlgorithmFactory<? extends BiomassAllocator> delegate = new ConstantAllocatorFactory();

    @Override
    public PolygonBiomassDecorator apply(FishState fishState) {
        try {

            GeomVectorField polygon = new GeomVectorField();
            ShapeFileImporterModified.read(shapeFile.toUri().toURL(), polygon,
                null, MasonGeometry.class
            );
            return new PolygonBiomassDecorator(
                polygon,
                insidePolygon,
                delegate.apply(fishState)
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to read shapefile!");
        }

    }


    public Path getShapeFile() {
        return shapeFile;
    }

    public void setShapeFile(Path shapeFile) {
        this.shapeFile = shapeFile;
    }


    public boolean isInsidePolygon() {
        return insidePolygon;
    }

    public void setInsidePolygon(boolean insidePolygon) {
        this.insidePolygon = insidePolygon;
    }

    public AlgorithmFactory<? extends BiomassAllocator> getDelegate() {
        return delegate;
    }

    public void setDelegate(AlgorithmFactory<? extends BiomassAllocator> delegate) {
        this.delegate = delegate;
    }
}
