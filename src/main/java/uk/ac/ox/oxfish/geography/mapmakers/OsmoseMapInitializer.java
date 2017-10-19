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

package uk.ac.ox.oxfish.geography.mapmakers;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Envelope;
import ec.util.MersenneTwisterFast;
import fr.ird.osmose.Cell;
import fr.ird.osmose.OsmoseSimulation;
import fr.ird.osmose.grid.IGrid;
import sim.field.geo.GeomGridField;
import sim.field.geo.GeomVectorField;
import sim.field.grid.ObjectGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.OsmoseGlobalBiology;
import uk.ac.ox.oxfish.geography.*;
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

    private final double  lowRightEasting;

    private final double lowRightNorthing;

    private final double upLeftEasting;

    private final double upLeftNorthing;

    public OsmoseMapInitializer(double gridCellSizeInKm) {
        this.gridCellSizeInKm = gridCellSizeInKm;
        this.lowRightEasting = Double.NaN;
        this.lowRightNorthing = Double.NaN;
        this.upLeftEasting = Double.NaN;
        this.upLeftNorthing = Double.NaN;

    }

    public OsmoseMapInitializer(
            double lowRightEasting, double lowRightNorthing,
            double upLeftEasting, double upLeftNorthing) {
        this.gridCellSizeInKm = Double.NaN;
        this.lowRightEasting = lowRightEasting;
        this.lowRightNorthing = lowRightNorthing;
        this.upLeftEasting = upLeftEasting;
        this.upLeftNorthing = upLeftNorthing;
    }

    @Override
    public NauticalMap makeMap(
            MersenneTwisterFast random, GlobalBiology biology, FishState model) {
        Preconditions.checkArgument(biology instanceof OsmoseGlobalBiology,
                                    "OSMOSE map requires OSMOSE biology");
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



        GeomGridField bathymetry;
        Distance distance;
        if(Double.isFinite(gridCellSizeInKm)) {
            bathymetry = new GeomGridField(baseGrid);
            distance = new CartesianDistance(gridCellSizeInKm);
        }
        else
        {
            bathymetry = new GeomGridField(baseGrid);
            bathymetry.setMBR(
                    new Envelope(
                    upLeftEasting,lowRightEasting,
                    lowRightNorthing,upLeftNorthing
            ));
            distance = new CartesianUTMDistance();

        }
        Pathfinder astar = new AStarPathfinder(distance);

        return new NauticalMap(bathymetry,new GeomVectorField(),distance,astar);


    }
}
