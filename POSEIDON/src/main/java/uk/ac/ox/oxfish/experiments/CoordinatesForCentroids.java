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

package uk.ac.ox.oxfish.experiments;

import com.vividsolutions.jts.geom.Coordinate;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.CentroidMapFileFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;

import java.util.List;

/**
 * Created by carrknight on 8/7/17.
 */
public class CoordinatesForCentroids
{


    private final static int[] groupNames = new int[]{50,51,61,62,63,73,85,86,97,98,109,110,122,134,135,147,148,160,161,173,174,186,187,198,199,200,212,213,225,226,237,238};

    public static void main(String[] args)
    {

        CaliforniaAbundanceScenario scenario  = new CaliforniaAbundanceScenario();
        FishState state = new FishState(0l);
        state.setScenario(scenario);
        state.start();
        CentroidMapFileFactory factory = new CentroidMapFileFactory();
        factory.setFilePath("inputs/california/logit/centroids_utm10N.csv");
        factory.setyColumnName("northings");
        factory.setxColumnName("eastings");
        factory.setAutomaticallyIgnoreWastelands(false);
        CentroidMapDiscretizer discretizer = factory.apply(state);

        List<SeaTile>[] groups = discretizer.discretize(state.getMap());
        System.out.println(groups.length);
        for(int i=0; i<groups.length; i++)
        {
            for(SeaTile tile : groups[i]) {
                Coordinate coordinates = state.getMap().getCoordinates(tile);
                System.out.println(coordinates.x +","+coordinates.y + "," + groupNames[i]);
              //  System.out.println(tile.getGridX() +","+tile.getGridY() + "," + i);
            }
        }


    }



}
