/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility;

import com.vividsolutions.jts.geom.Envelope;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.io.geo.ArcInfoASCGridImporter;
import sim.io.geo.ShapeFileImporter;
import sim.util.geo.MasonGeometry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * simple utility function to read ASC and return the grid
 */

public class GISReaders {



    public  static GeomGridField readRaster(String resourceName) {
        URL resource = GISReaders.class.getClassLoader().getResource(resourceName);
        File input = null;
        if(resource!= null)
            input = new File(resource.getFile());
        if(resource == null || !input.exists())
            //it's not a resource, maybe it's an external file
            input = Paths.get(resourceName).toFile();


        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(input);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        GeomGridField field = new GeomGridField();
        ArcInfoASCGridImporter.read(inputStream, GeomGridField.GridDataType.DOUBLE, field);

        return  field;

    }

    /**
     * reads the new files and then expands everyone's MBR so that they are equal
     * @param toMerge the tile raster representing the sea
     * @param filenames all the files with MPAs geometries
     * @return a unique GeomVectorField holding all the shapes.
     */
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
        File input = null;
        if(resource!= null)
            input = new File(resource.getFile());
        if(resource == null || !input.exists())
            //it's not a resource, maybe it's an external file
            input = Paths.get(filename).toFile();

        GeomVectorField vectorField = new GeomVectorField();

        //read it in!
        try {
            ShapeFileImporter.read(input.toURI().toURL(), vectorField);
        } catch (FileNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }
        return vectorField;
    }


}
