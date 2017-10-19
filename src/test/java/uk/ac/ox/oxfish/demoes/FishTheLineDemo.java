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

package uk.ac.ox.oxfish.demoes;

import org.junit.Assert;
import org.junit.Test;
import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.experiments.FirstPaper;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;

/**
 * I write here in unit-test format the results of the demoes I show on the website. This will help make sure
 * these results are stable whenever I change the code.
 * Created by carrknight on 8/3/15.
 */
public class FishTheLineDemo {







    //create an MPA, after people fish everything else there is to fish, they'll mostly fish just at the border of
    //the MPA
    @Test
    public void fishTheLine() throws IOException {


        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                FirstPaper.INPUT_FOLDER.resolve("mpa.yaml")));
        Scenario scenario =  yaml.loadAs(scenarioYaml, Scenario.class);
        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();
        double[][] theGrid = new double[state.getMap().getWidth()][state.getMap().getHeight()];

        while(state.getYear()<20)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
            for(int x =0; x<state.getMap().getWidth(); x++)
            {
                for (int y = 0; y < state.getMap().getHeight(); y++)
                {
                    theGrid[x][state.getMap().getHeight()-y-1] += trawls.get(x, y);
                }
            }
        }

        int mpaWidth= 15;
        int topLeftX = 15;
        int topLeftY = 10;
        int height = 30;

        //now check the hotspots
        double allHotspots = 0;
        double onTheLine = 0;
        IntGrid2D hotspots = state.getMap().getDailyTrawlsMap();
        for(int x =0; x<state.getMap().getWidth(); x++)
        {
            for (int y = 0; y < state.getMap().getHeight(); y++)
            {
                double hotspot = hotspots.get(x, y);
                allHotspots += hotspot;
                if(x >=  topLeftX - 1 && x <= topLeftX + 1 + mpaWidth && y>= topLeftY-1  && y<=topLeftY + 1 + height)
                    onTheLine+= hotspot;
                //also hotspot should be 0 in the MPA itself
                if(x >=  topLeftX && x <= topLeftX + mpaWidth && y>= topLeftY  && y<=topLeftY  + height)
                    Assert.assertEquals(0,hotspot,.0001);
            }

        }

        //on the line fishing make up at least 40% of all recent fishing (since exploration is pretty aggressive anyway)
        System.out.println(allHotspots + " --- " + onTheLine);
        System.out.println("percentage fished on the line : " + onTheLine/allHotspots);
        Assert.assertTrue(allHotspots * .40 <= onTheLine);
        Assert.assertTrue(onTheLine > 0);


    }




}