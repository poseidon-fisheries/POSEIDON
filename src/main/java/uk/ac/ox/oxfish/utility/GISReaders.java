package uk.ac.ox.oxfish.utility;

import com.vividsolutions.jts.geom.Envelope;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ArcInfoASCGridImporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.geo.MasonGeometry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * simple utility function to read ASC and return the grid
 */

public class GISReaders {



    public  static GeomGridField readRaster(String resourceName) {
        URL resource = GISReaders.class.getClassLoader().getResource(resourceName);
        if(resource == null)
            throw new NullPointerException("Resource is null");

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(resource.getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        GeomGridField field = new GeomGridField();
        ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.DOUBLE, field);

        return  field;

    }

    public  static GeomVectorField readShapeAndMergeWithRaster(GeomGridField toMerge, String... filenames)
    {
        if(filenames.length < 1)
            throw new IllegalArgumentException("Must have at least one shape file!");

        GeomVectorField vectorField = readShapeFile(filenames[0]);

        //now for all others filenames we are just going to copy paste the arguments in the first one
        for(int i=1;i<filenames.length; i++)
        {
            assert  i>0;
            GeomVectorField tmp =  readShapeFile(filenames[i]);

            Envelope globalMBR = vectorField.getMBR();
            globalMBR.expandToInclude(tmp.getMBR());
            vectorField.setMBR(globalMBR);

            for(Object geometry : tmp.getGeometries())
                vectorField.addGeometry((MasonGeometry) geometry);
        }
        //synchronize MBRs
        Envelope globalMBR = vectorField.getMBR();
        globalMBR.expandToInclude(toMerge.getMBR());
        toMerge.setMBR(globalMBR);
        vectorField.setMBR(globalMBR);
        return  vectorField;
    }

    private static GeomVectorField readShapeFile(String filename) {
        URL resource = GISReaders.class.getClassLoader().getResource(filename);
        if(resource == null)
            throw new NullPointerException("Resource is null");

        GeomVectorField vectorField = new GeomVectorField();

        //read it in!
        try {
            ShapeFileImporter.read(resource, vectorField);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return vectorField;
    }


}
