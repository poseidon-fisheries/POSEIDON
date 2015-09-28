package uk.ac.ox.oxfish.geography.osmose;

import ec.util.MersenneTwisterFast;
import fr.ird.osmose.Cell;
import fr.ird.osmose.OsmoseSimulation;
import fr.ird.osmose.grid.IGrid;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.geography.CartesianDistance;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;

/**
 * Takes a OsmoseSimulation object and extract a map from it
 * Created by carrknight on 6/25/15.
 */
public class OsmoseMapMaker{



    public static NauticalMap buildMap(
            OsmoseSimulation simulation,
            final double gridCellSizeInKm,
            OsmoseStepper stepper, MersenneTwisterFast random)
    {



        //build the grid
        final IGrid osmoseMap = simulation.getMap();
        final int width = osmoseMap.get_nx();
        final int height = osmoseMap.get_ny();
        ObjectGrid2D baseGrid =  new ObjectGrid2D(width,
                                                  height);

        //the 10 rightmost patches are land, the rest is sea
        for(int x=0; x< width; x++)
            for(int y=0; y< height; y++) {
                final Cell cell = osmoseMap.getCell(x, height-y-1);
                final SeaTile seaTile = cell.isLand() ?
                        new SeaTile(x, y, 20, new TileHabitat(0d)) :
                        new SeaTile(x, y, -20, new TileHabitat(0d));
                final LocalOsmoseBiology biology = new LocalOsmoseBiology(simulation.getMortality(),
                                                                          simulation.getCounter().getBiomass(x, height-y-1),
                                                                          simulation.getNumberOfSpecies(),
                                                                          random
                                                                          );
                stepper.getToReset().add(biology);
                seaTile.setBiology(biology);
                baseGrid.field[x][y] = seaTile;
            }


        GeomGridField bathymetry = new GeomGridField(baseGrid);
        GeomVectorField mpas = new GeomVectorField(); //empty MPAs
        Distance distance = new CartesianDistance(gridCellSizeInKm);
        return new NauticalMap(bathymetry,mpas,distance);
    }




}
