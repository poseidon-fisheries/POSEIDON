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

import org.jfree.util.Log;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * These tests are here to make sure that the yaml files of the narrative example
 * are still valid and they generate more or less the same stuff
 * Created by carrknight on 4/11/17.
 */
public class NarrativeBestTest {


    Path inputs = Paths.get("inputs", "tests", "narrative");


    @Test
    public void anarchyKillsOffAllFish() throws Exception {


        final FishYAML yaml = new FishYAML();
        final Scenario scenario = yaml.loadAs(new FileReader(inputs.resolve("anarchy.yaml").toFile()), Scenario.class);
        final FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();

        final double initialBiomass = state.getTotalBiomass(state.getSpecies().get(0));
        //we expect no regulation to achieve biomass levels of 5% or less

        while (state.getYear() < 20)
            state.schedule.step(state);

        final double finalBiomass = state.getTotalBiomass(state.getSpecies().get(0));

        Log.info("final biomass : " + finalBiomass + " which is  " + (finalBiomass / initialBiomass) + "% of the initial value; we are targeting 5% or lower");
        System.out.println("final biomass : " + finalBiomass + " which is  " + (finalBiomass / initialBiomass) + "% of the initial value; we are targeting 5% or lower");
        assertTrue(finalBiomass < initialBiomass * .05);

    }


    @Test
    public void improveSmallFishermen() throws Exception {

        final long start = System.currentTimeMillis();

        final HashMap<String, Double> profits = new HashMap<>();
        final String[] files = new String[]{"itq_best.yaml", "itqplus_best.yaml", "tac_best.yaml"};


        //gather for each run the profits made by small boats
        for (final String file : files) {
            final FishYAML yaml = new FishYAML();
            final Scenario scenario = yaml.loadAs(new FileReader(inputs.resolve(file).toFile()), Scenario.class);
            final FishState state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();


            while (state.getYear() < 20)
                state.schedule.step(state);

            final DoubleSummaryStatistics statistics = new DoubleSummaryStatistics();
            for (final Double profit : state.getYearlyDataSet().getColumn("Small Fishers Total Income"))
                statistics.accept(profit);

            profits.put(
                file,
                statistics.getSum()
            );

            Log.info("small fishermen profits of " + file + " are " + statistics.getSum());
            System.out.println("small fishermen profits of " + file + " are " + statistics.getSum());

        }
        System.out.println(profits);
        //itq beats tac
        assertTrue(profits.get("itq_best.yaml") > profits.get("tac_best.yaml"));
        //itq+ beats itq
        assertTrue(profits.get("itqplus_best.yaml") > profits.get("itq_best.yaml") * 1.5);

        final long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000);


    }
}