package uk.ac.ox.oxfish.geography.mapmakers;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import fr.ird.osmose.Cell;
import fr.ird.osmose.OsmoseSimulation;
import fr.ird.osmose.grid.IGrid;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.OsmoseGlobalBiology;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.pathfinding.AStarPathfinder;
import uk.ac.ox.oxfish.geography.pathfinding.Pathfinder;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Creates a map reading from the OSMOSE grid; assumes the Osmose Simulation has been initialized elsewhere.
 * Created by carrknight on 11/5/15.
 */
public class OsmoseMapInitializer implements MapInitializer {

    private final double gridCellSizeInKm;

    public OsmoseMapInitializer(double gridCellSizeInKm) {
        this.gridCellSizeInKm = gridCellSizeInKm;
    }

    @Override
    public NauticalMap makeMap(
            MersenneTwisterFast random, GlobalBiology biology, FishState model) {
        Preconditions.checkArgument(biology instanceof OsmoseGlobalBiology, "OSMOSE map requires OSMOSE biology");
        //cast the global biology
        OsmoseSimulation simulation = ((OsmoseGlobalBiology) biology).getSimulation();



        //build the grid
        final IGrid osmoseMap = simulation.getMap();
        final int width = osmoseMap.get_nx();
        final int height = osmoseMap.get_ny();
        ObjectGrid2D baseGrid =  new ObjectGrid2D(width,
                                                  height);

        //Create sea tiles by reading them from the simulation map
        for(int x=0; x< width; x++)
            for(int y=0; y< height; y++) {
                final Cell cell = osmoseMap.getCell(x, height-y-1);
                final SeaTile seaTile = cell.isLand() ?
                        new SeaTile(x, y, 200, new TileHabitat(0d)) :
                        new SeaTile(x, y, -200, new TileHabitat(0d));
                baseGrid.field[x][y] = seaTile;
            }


        GeomGridField bathymetry = new GeomGridField(baseGrid);
        GeomVectorField mpas = new GeomVectorField(); //empty MPAs
        Distance distance = new CartesianDistance(gridCellSizeInKm);
        Pathfinder astar = new AStarPathfinder(distance);
        return new NauticalMap(bathymetry,mpas,distance,astar);


    }
}
