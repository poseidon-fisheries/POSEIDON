package uk.ac.ox.oxfish.utility;

import sim.field.geo.GeomGridField;
import sim.field.grid.DoubleGrid2D;
import sim.io.geo.ArcInfoASCGridImporter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * simple utility function to read ASC and return the grid
 */

public class AscRasterReader {



    public  static GeomGridField read(String resourceName) {
        URL resource = AscRasterReader.class.getClassLoader().getResource(resourceName);
        if(resource == null)
            throw new NullPointerException("Resource is null");

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(resource.getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        GeomGridField field = new GeomGridField();
        ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.DOUBLE, field);

        return  field;

    }


}
