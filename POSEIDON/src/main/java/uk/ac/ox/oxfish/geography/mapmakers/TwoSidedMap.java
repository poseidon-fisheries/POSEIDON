package uk.ac.ox.oxfish.geography.mapmakers;

import com.vividsolutions.jts.geom.Point;
import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;
import uk.ac.ox.oxfish.geography.pathfinding.StraightLinePathfinder;
import uk.ac.ox.oxfish.model.FishState;

/**
 * land on both sides
 */
public class TwoSidedMap implements MapInitializer {


    private final int width;

    private final int height;

    private final  double cellSizeInKilometers;


    public TwoSidedMap(int width, int height, double cellSizeInKilometers) {
        this.width = width;
        this.height = height;
        this.cellSizeInKilometers = cellSizeInKilometers;
    }






    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getCellSizeInKilometers() {
        return cellSizeInKilometers;
    }


    @Override
    public NauticalMap makeMap(MersenneTwisterFast random, GlobalBiology biology, FishState model)
    {

        //build the grid
        ObjectGrid2D baseGrid =  new ObjectGrid2D(width, height);

        for(int x=1; x< width-1; x++)
            for(int y=0; y< height; y++) {
                baseGrid.field[x][y] =
                        new SeaTile(x,y,-2000, new TileHabitat(0d));
            }

        for(int y=0; y< height; y++) {
            baseGrid.field[0][y] =
                    new SeaTile(0,y,2000, new TileHabitat(0d));
            baseGrid.field[width-1][y] =
                    new SeaTile(width-1,y,2000, new TileHabitat(0d));
        }


        GeomGridField bathymetry = new GeomGridField(baseGrid);
        GeomVectorField mpas = new GeomVectorField(baseGrid.getWidth(), baseGrid.getHeight()); //empty MPAs

        //expand MBR to cointan all the cells
        Point coordinates = bathymetry.toPoint(width-1, height-1);
        bathymetry.getMBR().expandToInclude(coordinates.getX(),coordinates.getY());
        coordinates = bathymetry.toPoint(width-1, 0);
        bathymetry.getMBR().expandToInclude(coordinates.getX(),coordinates.getY());
        coordinates = bathymetry.toPoint(0, 0);
        bathymetry.getMBR().expandToInclude(coordinates.getX(),coordinates.getY());
        coordinates = bathymetry.toPoint(0, height-1);
        bathymetry.getMBR().expandToInclude(coordinates.getX(), coordinates.getY());

        //expand it a little further
        bathymetry.getMBR().expandToInclude(bathymetry.getMBR().getMaxX()-bathymetry.getPixelWidth()/2,
                bathymetry.getMBR().getMaxY()-bathymetry.getPixelHeight()/2);
        bathymetry.getMBR().expandToInclude(bathymetry.getMBR().getMinX()-bathymetry.getPixelWidth()/2,
                bathymetry.getMBR().getMinY()-bathymetry.getPixelHeight()/2);

        mpas.setMBR(bathymetry.getMBR());

        Distance distance = new CartesianDistance(cellSizeInKilometers);
        Pathfinder pathfinder = new StraightLinePathfinder();
        return new NauticalMap(bathymetry,mpas,distance,pathfinder);
    }
}
