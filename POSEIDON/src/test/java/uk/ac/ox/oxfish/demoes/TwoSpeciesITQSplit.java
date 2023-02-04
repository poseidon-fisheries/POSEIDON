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
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TwoSpeciesITQSplit
{


    /**
     * 2 species ITQ, both are valuable but the quotas of the ones only available south are very few so that
     * it's better to fish north. The results are muffled by the fact that over time north gets consumed and it becomes better
     * to fish south instead anyway.
     */
    @Test
    public void itqAffectsGeography() throws Exception {



        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                Paths.get("inputs", "first_paper", "location_itq.yaml")));
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml,PrototypeScenario.class);
        FishState state = new FishState();
        state.setScenario(scenario);





        long towsNorth = 0;
        long towsSouth = 0;

        state.start();
        //first year, just lspiRun: there is no ITQ running anyway
        while (state.getYear() < 1) {
            state.schedule.step(state);
        }
        state.schedule.step(state);


        while (state.getYear() < 5) {
            state.schedule.step(state);
            for (int x = 0; x < 50; x++) {
                for (int y = 0; y <= 25; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 26; y < 50; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        System.out.println("North vs South : " + towsNorth / ((double) towsNorth + towsSouth));
        Assert.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .6);



    }


    /**
     * we make fish so mobile that the depletion of reds isn't a problem: we are going to see geography choices
     * being as effective as switching gear
     */
    @Test
    public void TwoSpeciesITQSplitUnmuffled()
    {
        //this I think has about a 2% failure rate; I am going to make it run twice. If it fails twice, we have a problem
        try {
            unmuffledTestOnce();
        }
        catch (AssertionError e){
            unmuffledTestOnce();

        }
    }

    private void unmuffledTestOnce() {


        final FishState state = new FishState(System.currentTimeMillis());
        //world split in half

        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));

        SplitInitializerFactory biologyFactory = new SplitInitializerFactory();
        biologyFactory.setCarryingCapacity(new FixedDoubleParameter(5000));
        biologyFactory.setGrower(new SimpleLogisticGrowerFactory(.9));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        //world split in half
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);


        SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
        simpleMap.setCoastalRoughness(new FixedDoubleParameter(0d));
        scenario.setMapInitializer(simpleMap);
        scenario.forcePortPosition(new int[]{40, 25});

        scenario.setUsePredictors(true);


        long towsNorth = 0;
        long towsSouth = 0;

        state.start();
        //first year, just lspiRun: there is no ITQ running anyway
        while (state.getYear() < 1) {
            state.schedule.step(state);
        }
        state.schedule.step(state);

        state.schedule.step(state);
        double earlyRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
                                                                                        AbstractMarket.LANDINGS_COLUMN_NAME);
        double earlyBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
                                                                                         AbstractMarket.LANDINGS_COLUMN_NAME);

        System.out.println("Early Landings: " + earlyRedLandings + " --- " + earlyBlueLandings);
        //blue start as a choke species
        double totalBlueQuotas = 500 * 100;
        Assert.assertTrue(earlyBlueLandings > .8 * totalBlueQuotas);
        //red is underutilized
        double totalRedQuotas = 4500 * 100;
        Assert.assertTrue(earlyRedLandings < .5 * totalRedQuotas);


        while (state.getYear() < 5) {
            state.schedule.step(state);
            for (int x = 0; x < 50; x++) {
                for (int y = 0; y <= 25; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 26; y < 50; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        System.out.println("North vs South : " + towsNorth / ((double) towsNorth + towsSouth));
        Assert.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .6);

        //by year 10 the quotas are very well used!
        double lateRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
                                                                                       AbstractMarket.LANDINGS_COLUMN_NAME);
        double lateBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
                                                                                        AbstractMarket.LANDINGS_COLUMN_NAME);
        System.out.println("Late Landings: " + lateRedLandings + " --- " + lateBlueLandings);
        System.out.println(
                "Late Quota Efficiency: " + lateRedLandings / totalRedQuotas + " --- " + lateBlueLandings / totalBlueQuotas);

        //geographical choice with "fixed" biology works very strongly
        Assert.assertTrue(lateRedLandings > .6 * totalRedQuotas);
    }


}
