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

package uk.ac.ox.oxfish.geography.osmose;

import fr.ird.osmose.OsmoseSimulation;
import fr.ird.osmose.School;
import fr.ird.osmose.process.mortality.MortalityCause;
import org.junit.Test;
import uk.ac.ox.ouce.oxfish.ExogenousMortality;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A test to make sure that whatever we say to the exogenous mortality to kill, it actually kills
 * Created by carrknight on 7/6/15.
 */
public class ExogenousMortalityWorks
{

    @Test
    public void murderResponsibly() throws Exception {


        final URL restartFile = getClass().getClassLoader().getResource(
                "test.nc");
        assert restartFile != null;


        final URL configuration = getClass().getClassLoader().getResource(
                "osmose_config/osm_all-parameters.csv");
        assert configuration != null;
        OsmoseSimulation simulation = OsmoseSimulation.startupOSMOSEWithRestartFile(0,
                                                                                    configuration.getPath(),
                                                                                    restartFile.getPath());

        ExogenousMortality mortality = simulation.getMortality();
        for(int i=0; i<10; i++)
            mortality.markThisSpeciesAsExogenous(i);

        HashMap<School, Double> fishCaught = new HashMap<>();
        for(School school : simulation.getSchoolSet())
        {
            Double pretendCatches = 0.05*school.getInstantaneousBiomass(); //kill off 5% of biomass
            mortality.incrementCatches(school,pretendCatches);
            fishCaught.put(school, school.biom2abd(pretendCatches)); // i have to store it as number dead rather than
            //biomass because that's how the information is stored in the school
        }

        //step the simulation
        simulation.oneStep();
        //should be stored as "NDead"
        for(Map.Entry<School,Double> catches : fishCaught.entrySet())
        {
            System.out.println(catches.getValue());
            assertEquals(catches.getValue(),
                         catches.getKey().getNdead(MortalityCause.FISHING),.1);
        }

        // release memory from the simulation instance because
        // fr.ird.osmose.Osmose statically holds on to it
        simulation.destroy();
    }

    @Test
    public void osmoseMurderSome() throws Exception {


        final URL restartFile = getClass().getClassLoader().getResource(
                "test.nc");
        assert restartFile != null;


        final URL configuration = getClass().getClassLoader().getResource(
                "osmose_config/osm_all-parameters.csv");
        assert configuration != null;
        OsmoseSimulation simulation = OsmoseSimulation.startupOSMOSEWithRestartFile(0,
                                                                                    configuration.getPath(),
                                                                                    restartFile.getPath());

        ExogenousMortality mortality = simulation.getMortality();
        for(int i=0; i<5; i++)
            mortality.markThisSpeciesAsExogenous(i);

        HashMap<School, Double> fishCaught = new HashMap<>();


        //step the simulation
        simulation.oneStep();
        //should be stored as "NDead"
        int osmoseKilled=0;
        for(School school : simulation.getSchoolSet())
        {
            double deads = school.getNdead(MortalityCause.FISHING);

            System.out.println(deads);

            if(school.getSpeciesIndex()<5)
            assertEquals(0,
                         deads, .1);
            if(school.getSpeciesIndex()>=5 && deads > 0)
                osmoseKilled ++;
        }
        assertTrue(osmoseKilled>0);

        // release memory from the simulation instance because
        // fr.ird.osmose.Osmose statically holds on to it
        simulation.destroy();
    }
}
