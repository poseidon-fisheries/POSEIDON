package uk.ac.ox.oxfish.biology.initializer.allocator;

import sim.field.geo.GeomVectorField;
import sim.io.geo.ShapeFileImporter;
import sim.util.geo.MasonGeometry;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.ShapeFileImporterModified;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PolygonAllocatorFactory implements AlgorithmFactory<PolygonBiomassDecorator> {



    private Path shapeFile = Paths.get("./docs/indonesia_hub/runs/712/shape/WPP_boundary.shp");


    private boolean insidePoligon = true;

    private AlgorithmFactory<? extends BiomassAllocator> delegate = new ConstantAllocatorFactory();

    @Override
    public PolygonBiomassDecorator apply(FishState fishState) {
        try {

            GeomVectorField polygon = new GeomVectorField();
            ShapeFileImporterModified.read(shapeFile.toUri().toURL(), polygon,
                    null, MasonGeometry.class);
            return new PolygonBiomassDecorator(polygon,
                    insidePoligon,
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


    public boolean isInsidePoligon() {
        return insidePoligon;
    }

    public void setInsidePoligon(boolean insidePoligon) {
        this.insidePoligon = insidePoligon;
    }

    public AlgorithmFactory<? extends BiomassAllocator> getDelegate() {
        return delegate;
    }

    public void setDelegate(AlgorithmFactory<? extends BiomassAllocator> delegate) {
        this.delegate = delegate;
    }
}
