package uk.ac.ox.oxfish.geography.sampling;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Envelope;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A map made of multiple grids, one for each input file given (plus the altitude one).
 * One can build a NauticalMap from this object by integrating the various grids, but this is not done in this class
 *
 * Created by carrknight on 2/25/16.
 */
public class SampledMap
{


    /**
     * collects the biological grids, each objectGrid2D is made of a LinkedList of doubles
     */
    private final LinkedHashMap<String,ObjectGrid2D> biologyGrids = new LinkedHashMap<>();

    /**
     * the bathymetry file
     */
    private final ObjectGrid2D altitudeGrid;

    /**
     * the envelope containing the map
     */
    private final Envelope mbr;


    private final int gridWith;

    private final int gridHeight;

    /**
     * reads and combines the various map files. The envelope of the map is given by the size of the first biologyFile
     * and all the other maps are cropped to fit it
     * @param bathymetryFile bathymetry file
     * @param gridWidth width of the gridded maps
     * @param biologyFiles a list of biology files
     */
    public SampledMap(
            Path bathymetryFile,
            int gridWidth,
            Path... biologyFiles) throws IOException {

        Preconditions.checkArgument(biologyFiles.length > 0);
        gridWith = gridWidth;
        //read the first biological file
        GeographicalSample biologySample = new GeographicalSample(biologyFiles[0],true);
        mbr = new Envelope(biologySample.getMinEasting(), biologySample.getMaxEasting(),
                           biologySample.getMinNorthing(), biologySample.getMaxNorthing());
        //find ratio height to width
        double heightToWidth = mbr.getHeight()/mbr.getWidth();
        gridHeight = (int) Math.round(gridWidth * heightToWidth);


        ObjectGrid2D backingBioGrid = new ObjectGrid2D(gridWidth,gridHeight);
        GeomGridField bioGrid = new GeomGridField(backingBioGrid);
        bioGrid.setMBR(mbr);

        //now collect observations
        backingBioGrid = fileToGrid(backingBioGrid,bioGrid,biologySample);
        biologyGrids.put(biologyFiles[0].getFileName().toString(),backingBioGrid);
        //read the altitude
        //read raster bathymetry
        GeographicalSample altitudeSample = new GeographicalSample(bathymetryFile,false);
        altitudeGrid = fileToGrid(new ObjectGrid2D(gridWidth, gridHeight),bioGrid,altitudeSample);


        //now do the others
        for(int csv=1; csv<biologyFiles.length; csv++)
        {
            backingBioGrid = fileToGrid(new ObjectGrid2D(gridWidth, gridHeight), bioGrid,
                                        new GeographicalSample(biologyFiles[csv], true));
            biologyGrids.put(biologyFiles[csv].getFileName().toString(),backingBioGrid);

        }



    }

    /**
     * Takes a new backing grid, fills it and returns it
     * @param backingGrid the backing grid to fill (it will also be returned)
     * @param coordinateSpace a geo-spatial grid that can be used to transform data coordinates into grid coordinates
     * @param preformattedCSV the data from CSV preformatted
     * @return the backing grid after it has been filled (it will be made of LinkedList objects, containing double observations
     */
    private ObjectGrid2D fileToGrid(
            ObjectGrid2D backingGrid,
            GeomGridField coordinateSpace,
            GeographicalSample preformattedCSV ) {

        for(int x = 0; x<backingGrid.getWidth(); x++)
            for(int y = 0; y<backingGrid.getHeight(); y++)
                backingGrid.field[x][y] = new LinkedList<Double>();
        Iterator<Double> eastings = preformattedCSV.getEastings().iterator();
        Iterator<Double> northings = preformattedCSV.getNorthings().iterator();
        Iterator<Double> observations = preformattedCSV.getObservations().iterator();
        for(int i=0; i<preformattedCSV.getObservations().size(); i++)
        {
            int x = coordinateSpace.toXCoord(eastings.next());
            int y = coordinateSpace.toYCoord(northings.next());
            double obs = observations.next();
            //the very edge might get cut
            if(x>=0 && x < backingGrid.getWidth() && y >=0 && y < backingGrid.getHeight())
                ((List) backingGrid.field[x][y]).add(obs);
            if(i % 10000 == 0 && Log.TRACE)
                Log.trace("Transformed " +i + "  sampled lines into a grid" );
        }
        return backingGrid;
    }

    public LinkedHashMap<String, ObjectGrid2D> getBiologyGrids() {
        return biologyGrids;
    }

    public ObjectGrid2D getAltitudeGrid() {
        return altitudeGrid;
    }

    public Envelope getMbr() {
        return mbr;
    }

    /**
     * Getter for property 'gridWith'.
     *
     * @return Value for property 'gridWith'.
     */
    public int getGridWith() {
        return gridWith;
    }

    /**
     * Getter for property 'gridHeight'.
     *
     * @return Value for property 'gridHeight'.
     */
    public int getGridHeight() {
        return gridHeight;
    }
}
