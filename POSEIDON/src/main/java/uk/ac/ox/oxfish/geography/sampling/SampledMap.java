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

package uk.ac.ox.oxfish.geography.sampling;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.vividsolutions.jts.geom.Envelope;
import sim.field.geo.GeomGridField;
import sim.field.grid.ObjectGrid2D;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A map made of multiple grids, one for each input file given (plus the altitude one).
 * One can build a NauticalMap from this object by integrating the various grids, but this is not done in this class
 * <p>
 * Created by carrknight on 2/25/16.
 */
public class SampledMap implements Serializable {


    private static final long serialVersionUID = 336168355423244254L;
    /**
     * collects the biological grids, each objectGrid2D is made of a LinkedList of doubles
     */
    private LinkedHashMap<String, Table<Integer, Integer, LinkedList<Double>>> biologyGrids = new LinkedHashMap<>();

    /**
     * the bathymetry file
     */
    private Table<Integer, Integer, LinkedList<Double>> altitudeGrid;

    /**
     * the envelope containing the map
     */
    private Envelope mbr;


    private int gridWith;

    private int gridHeight;


    /**
     * reads and combines the various map files. The envelope of the map is given by the size of the first biologyFile
     * and all the other maps are cropped to fit it
     *
     * @param bathymetryFile bathymetry file
     * @param gridWidth      width of the gridded maps
     * @param biologyFiles   a list of biology files
     */
    public SampledMap(
        final Path bathymetryFile,
        final int gridWidth,
        final LinkedHashMap<String, Path> biologyFiles
    ) throws IOException {

        Preconditions.checkArgument(biologyFiles.size() > 0);
        gridWith = gridWidth;
        //read the first biological file
        final Iterator<Map.Entry<String, Path>> biologyIterator = biologyFiles.entrySet().iterator();
        final Map.Entry<String, Path> firstBiology = biologyIterator.next();
        final GeographicalSample biologySample = new GeographicalSample(firstBiology.getValue(), true);
        mbr = new Envelope(biologySample.getMinFirstCoordinate(), biologySample.getMaxFirstCoordinate(),
            biologySample.getMinSecondCoordinate(), biologySample.getMaxSecondCoordinate()
        );
        System.out.println("Map Info\nEastings from" + biologySample.getMinFirstCoordinate() + " to " + biologySample.getMaxFirstCoordinate());
        System.out.println("Northings from" + biologySample.getMinSecondCoordinate() + " to " + biologySample.getMaxSecondCoordinate());
        //find ratio height to width
        final double heightToWidth = mbr.getHeight() / mbr.getWidth();
        gridHeight = (int) Math.round(gridWidth * heightToWidth);


        final ObjectGrid2D backingBioGrid = new ObjectGrid2D(gridWidth, gridHeight);
        final GeomGridField bioGrid = new GeomGridField(backingBioGrid);
        bioGrid.setMBR(mbr);

        //now collect observations
        Table<Integer, Integer, LinkedList<Double>> backingBioTable = fileToGrid(bioGrid, biologySample,
            getGridWith(), getGridHeight()
        );
        biologyGrids.put(firstBiology.getKey(), backingBioTable);
        //read the altitude
        //read raster bathymetry
        final GeographicalSample altitudeSample = new GeographicalSample(bathymetryFile, false);
        altitudeGrid = fileToGrid(bioGrid, altitudeSample, getGridWith(),
            getGridHeight()
        );


        //now do the others
        while (biologyIterator.hasNext()) {
            final Map.Entry<String, Path> biologyFile = biologyIterator.next();
            backingBioTable = fileToGrid(bioGrid,
                new GeographicalSample(biologyFile.getValue(), true), getGridWith(),
                getGridHeight()
            );
            biologyGrids.put(biologyFile.getKey(), backingBioTable);

        }


    }

    /**
     * Takes a new backing grid, fills it and returns it
     *
     * @param coordinateSpace a geo-spatial grid that can be used to transform data coordinates into grid coordinates
     * @param preformattedCSV the data from CSV preformatted
     * @param gridWith
     * @param gridHeight
     * @return the backing grid after it has been filled (it will be made of LinkedList objects, containing double observations
     */
    public static Table<Integer, Integer, LinkedList<Double>> fileToGrid(
        final GeomGridField coordinateSpace,
        final GeographicalSample preformattedCSV,
        final int gridWith,
        final int gridHeight
    ) {

        final Table<Integer, Integer, LinkedList<Double>> backingGrid = HashBasedTable.create(gridWith, gridHeight);
        for (int x = 0; x < gridWith; x++)
            for (int y = 0; y < gridHeight; y++)
                backingGrid.put(x, y, new LinkedList<>());
        final Iterator<Double> eastings = preformattedCSV.getFirstCoordinate().iterator();
        final Iterator<Double> northings = preformattedCSV.getSecondCoordinate().iterator();
        final Iterator<Double> observations = preformattedCSV.getObservations().iterator();
        for (int i = 0; i < preformattedCSV.getObservations().size(); i++) {
            final int x = coordinateSpace.toXCoord(eastings.next());
            final int y = coordinateSpace.toYCoord(northings.next());
            final double obs = observations.next();
            //the very edge might get cut
            if (x >= 0 && x < gridWith && y >= 0 && y < gridHeight)
                backingGrid.get(x, y).add(obs);
            if (i % 10000 == 0) {
                final int finalI = i;
                Logger.getGlobal().fine(() -> "Transformed " + finalI + "  sampled lines into a grid");
            }
        }
        return backingGrid;
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
     * Setter for property 'gridHeight'.
     *
     * @param gridHeight Value to set for property 'gridHeight'.
     */
    public void setGridHeight(final int gridHeight) {
        this.gridHeight = gridHeight;
    }

    /**
     * Setter for property 'gridWith'.
     *
     * @param gridWith Value to set for property 'gridWith'.
     */
    public void setGridWith(final int gridWith) {
        this.gridWith = gridWith;
    }

    public LinkedHashMap<String, Table<Integer, Integer, LinkedList<Double>>> getBiologyGrids() {
        return biologyGrids;
    }

    /**
     * Setter for property 'biologyGrids'.
     *
     * @param biologyGrids Value to set for property 'biologyGrids'.
     */
    public void setBiologyGrids(
        final LinkedHashMap<String, Table<Integer, Integer, LinkedList<Double>>> biologyGrids
    ) {
        this.biologyGrids = biologyGrids;
    }

    public Table<Integer, Integer, LinkedList<Double>> getAltitudeGrid() {
        return altitudeGrid;
    }

    /**
     * Setter for property 'altitudeGrid'.
     *
     * @param altitudeGrid Value to set for property 'altitudeGrid'.
     */
    public void setAltitudeGrid(
        final Table<Integer, Integer, LinkedList<Double>> altitudeGrid
    ) {
        this.altitudeGrid = altitudeGrid;
    }

    public Envelope getMbr() {
        return mbr;
    }

    /**
     * Setter for property 'mbr'.
     *
     * @param mbr Value to set for property 'mbr'.
     */
    public void setMbr(final Envelope mbr) {
        this.mbr = mbr;
    }
}
