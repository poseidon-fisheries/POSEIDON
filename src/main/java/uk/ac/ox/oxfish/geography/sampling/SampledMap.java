package uk.ac.ox.oxfish.geography.sampling;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vividsolutions.jts.geom.Envelope;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;

/**
 * A map made of multiple grids, one for each input file given (plus the altitude one).
 * One can build a NauticalMap from this object by integrating the various grids, but this is not done in this class
 *
 * Created by carrknight on 2/25/16.
 */
public class SampledMap implements Serializable
{


    /**
     * collects the biological grids, each objectGrid2D is made of a LinkedList of doubles
     */
    private LinkedHashMap<String,Table<Integer,Integer,LinkedList<Double>>> biologyGrids = new LinkedHashMap<>();

    /**
     * the bathymetry file
     */
    private Table<Integer,Integer,LinkedList<Double>> altitudeGrid;

    /**
     * the envelope containing the map
     */
    private Envelope mbr;


    private int gridWith;

    private int gridHeight;



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
            LinkedHashMap<String,Path> biologyFiles) throws IOException {

        Preconditions.checkArgument(biologyFiles.size() > 0);
        gridWith = gridWidth;
        //read the first biological file
        Iterator<Map.Entry<String, Path>> biologyIterator = biologyFiles.entrySet().iterator();
        Map.Entry<String, Path> firstBiology = biologyIterator.next();
        GeographicalSample biologySample = new GeographicalSample(firstBiology.getValue(), true);
        mbr = new Envelope(biologySample.getMinEasting(), biologySample.getMaxEasting(),
                           biologySample.getMinNorthing(), biologySample.getMaxNorthing());
        System.out.println("Map Info\nEastings from" + biologySample.getMinEasting() + " to " + biologySample.getMaxEasting());
        System.out.println("Northings from" + biologySample.getMinNorthing() + " to " + biologySample.getMaxNorthing());
        //find ratio height to width
        double heightToWidth = mbr.getHeight()/mbr.getWidth();
        gridHeight = (int) Math.round(gridWidth * heightToWidth);


        ObjectGrid2D backingBioGrid = new ObjectGrid2D(gridWidth,gridHeight);
        GeomGridField bioGrid = new GeomGridField(backingBioGrid);
        bioGrid.setMBR(mbr);

        //now collect observations
        Table<Integer,Integer,LinkedList<Double>> backingBioTable = fileToGrid(bioGrid, biologySample,
                                                                               getGridWith(), getGridHeight());
        biologyGrids.put(firstBiology.getKey(), backingBioTable);
        //read the altitude
        //read raster bathymetry
        GeographicalSample altitudeSample = new GeographicalSample(bathymetryFile,false);
        altitudeGrid = fileToGrid( bioGrid, altitudeSample, getGridWith(),
                                  getGridHeight());


        //now do the others
        while (biologyIterator.hasNext())
        {
            Map.Entry<String, Path> biologyFile = biologyIterator.next();
            backingBioTable = fileToGrid(bioGrid,
                                         new GeographicalSample(biologyFile.getValue(), true), getGridWith(),
                                         getGridHeight());
            biologyGrids.put(biologyFile.getKey(),backingBioTable);

        }



    }

    /**
     * Takes a new backing grid, fills it and returns it
     * @param backingGrid the backing grid to fill (it will also be returned)
     * @param coordinateSpace a geo-spatial grid that can be used to transform data coordinates into grid coordinates
     * @param preformattedCSV the data from CSV preformatted
     * @param gridWith
     * @param gridHeight
     * @return the backing grid after it has been filled (it will be made of LinkedList objects, containing double observations
     */
    public static Table<Integer,Integer,LinkedList<Double>> fileToGrid(
            GeomGridField coordinateSpace,
            GeographicalSample preformattedCSV,
            final int gridWith,
            final int gridHeight) {

        Table<Integer, Integer, LinkedList<Double>> backingGrid = HashBasedTable.create(gridWith,gridHeight);
        for(int x = 0; x< gridWith; x++)
            for(int y = 0; y< gridHeight; y++)
                backingGrid.put(x,y,new LinkedList<Double>());
        Iterator<Double> eastings = preformattedCSV.getEastings().iterator();
        Iterator<Double> northings = preformattedCSV.getNorthings().iterator();
        Iterator<Double> observations = preformattedCSV.getObservations().iterator();
        for(int i=0; i<preformattedCSV.getObservations().size(); i++)
        {
            int x = coordinateSpace.toXCoord(eastings.next());
            int y = coordinateSpace.toYCoord(northings.next());
            double obs = observations.next();
            //the very edge might get cut
            if(x>=0 && x < gridWith && y >=0 && y < gridHeight)
                ((List) backingGrid.get(x,y)).add(obs);
            if(i % 10000 == 0 && Log.TRACE)
                Log.trace("Transformed " +i + "  sampled lines into a grid" );
        }
        return backingGrid;
    }

    public LinkedHashMap<String,Table<Integer,Integer,LinkedList<Double>>> getBiologyGrids() {
        return biologyGrids;
    }

    public Table<Integer,Integer,LinkedList<Double>> getAltitudeGrid() {
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

    /**
     * Setter for property 'biologyGrids'.
     *
     * @param biologyGrids Value to set for property 'biologyGrids'.
     */
    public void setBiologyGrids(
            LinkedHashMap<String, Table<Integer, Integer, LinkedList<Double>>> biologyGrids) {
        this.biologyGrids = biologyGrids;
    }

    /**
     * Setter for property 'altitudeGrid'.
     *
     * @param altitudeGrid Value to set for property 'altitudeGrid'.
     */
    public void setAltitudeGrid(
            Table<Integer, Integer, LinkedList<Double>> altitudeGrid) {
        this.altitudeGrid = altitudeGrid;
    }

    /**
     * Setter for property 'mbr'.
     *
     * @param mbr Value to set for property 'mbr'.
     */
    public void setMbr(Envelope mbr) {
        this.mbr = mbr;
    }

    /**
     * Setter for property 'gridWith'.
     *
     * @param gridWith Value to set for property 'gridWith'.
     */
    public void setGridWith(int gridWith) {
        this.gridWith = gridWith;
    }

    /**
     * Setter for property 'gridHeight'.
     *
     * @param gridHeight Value to set for property 'gridHeight'.
     */
    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }
}
