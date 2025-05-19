/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.demoes;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQStringFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;


public class GearImitationWithITQ {

    @Test
    public void ITQDrivePeopleToSwitchToBetterGear() throws Exception {

//this fails about once every 30 tests. So I am going to make it best of two


        try {
            final MultiITQFactory multiFactory = new MultiITQFactory();
            //quota ratios: 90-10
            multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
            multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
            final long seed = System.currentTimeMillis();
            Logger.getGlobal().info("seed is : " + seed);
            gearImitationTestRun(multiFactory, true, seed);
        } catch (final AssertionError error) {
            final MultiITQFactory multiFactory = new MultiITQFactory();
            //quota ratios: 90-10
            multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
            multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
            final long seed = System.currentTimeMillis();
            Logger.getGlobal().info("seed is : " + seed);
            gearImitationTestRun(multiFactory, true, seed);
        }


    }

    public FishState gearImitationTestRun(
        final AlgorithmFactory<? extends MultiQuotaRegulation> multiFactory, final boolean checkRed,
        final long seed
    ) throws IOException {
        final FishYAML yaml = new FishYAML();
        final String scenarioYaml = String.join("\n", Files.readAllLines(
            Paths.get("inputs", "first_paper", "gear_itq.yaml")));
        final PrototypeScenario scenario = yaml.loadAs(scenarioYaml, PrototypeScenario.class);
        final FishState state = new FishState();
        state.setScenario(scenario);

        scenario.setRegulation(multiFactory);

        //set up the gear adaptation:
        state.registerStartable(new Startable() {
            @Override
            public void start(final FishState model) {

                //start collecting red catchability and blue catchability
                model.getYearlyDataSet().registerGatherer("Red Catchability", state1 -> {
                    final double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (final Fisher fisher1 : state1.getFishers())
                            total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[0]
                                ;
                        return total / size;
                    }
                }, Double.NaN);


                model.getYearlyDataSet().registerGatherer("Blue Catchability", state1 -> {
                    final double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (final Fisher fisher1 : state1.getFishers())
                            total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[1]
                                ;
                        return total / size;
                    }
                }, Double.NaN);


            }

            @Override
            public void turnOff() {

            }
        });


        state.start();


        while (state.getYear() < 1)
            state.schedule.step(state);
        Double blue = state.getYearlyDataSet().getLatestObservation("Blue Catchability");
        Double red = state.getYearlyDataSet().getLatestObservation("Red Catchability");
        if (state.getDayOfTheYear() == 1)
            Logger.getGlobal().info("Red catchability: " + red + " --- Blue Catchability: " + blue);
        state.schedule.step(state);
        final double earlyRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
            AbstractMarket.LANDINGS_COLUMN_NAME);
        final double earlyBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
            AbstractMarket.LANDINGS_COLUMN_NAME);

        System.out.println("Early Landings: " + earlyRedLandings + " --- " + earlyBlueLandings);
        //blue start as a choke species
        final double totalBlueQuotas = 500 * 100;
        //   Assert.assertTrue(earlyBlueLandings > .8 * totalBlueQuotas);
        //red is underutilized
        if (checkRed) {
            final double totalRedQuotas = 4500 * 100;
            System.out.println("red landings are " + earlyRedLandings / totalRedQuotas + " of the total quota");
            //         Assert.assertTrue(earlyRedLandings < .5 * totalRedQuotas);
        }

        double lateRedLandings;
        double lateBlueLandings;

        while (state.getYear() < 30) {
            state.schedule.step(state);
            blue = state.getYearlyDataSet().getLatestObservation("Blue Catchability");
            red = state.getYearlyDataSet().getLatestObservation("Red Catchability");
            if (state.getDayOfTheYear() == 1) {
                Logger.getGlobal().info("Red catchability: " + red + " --- Blue Catchability: " + blue);

                lateRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
                    AbstractMarket.LANDINGS_COLUMN_NAME);
                lateBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
                    AbstractMarket.LANDINGS_COLUMN_NAME);
                System.out.println("Late Landings: " +
                    (lateRedLandings / ((4500 * 100))) +
                    " --- " +
                    (lateBlueLandings / ((500 * 100))));
            }
        }


        state.schedule.step(state);
        blue = state.getYearlyDataSet().getLatestObservation("Blue Catchability");
        red = state.getYearlyDataSet().getLatestObservation("Red Catchability");
        System.out.println("Red catchability: " + red + " --- Blue Catchability: " + blue);

        Assertions.assertTrue(red > .01);
        Assertions.assertTrue(blue < .01);
        Assertions.assertTrue(red > blue + .005);

        //by year 20 the quotas are very well used!
        lateRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
            AbstractMarket.LANDINGS_COLUMN_NAME);
        lateBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
            AbstractMarket.LANDINGS_COLUMN_NAME);
        System.out.println("Late Landings: " +
            lateRedLandings +
            " --- " +
            lateBlueLandings);
        System.out.println(
            "Late Quota Efficiency: " +
                (!checkRed ? Double.NaN : lateRedLandings / (4500 * 100)) +
                " --- " +
                lateBlueLandings / totalBlueQuotas);

        //much better efficiency by the end of the simulation
        Assertions.assertTrue(lateBlueLandings > .75 * totalBlueQuotas);
        if (checkRed) {
            final double totalRedQuotas = 4500 * 100;

            Assertions.assertTrue(lateRedLandings > .7 * totalRedQuotas); //this is actually almost always above 90% after 20 years
        }

        return state;

    }

    @Test
    public void UnprotectedVersusProtectedGearSwitch() throws Exception {


        final MultiITQStringFactory multiFactory = new MultiITQStringFactory();
        //only blue are protected by quota
        multiFactory.setYearlyQuotaMaps("1:500");

        gearImitationTestRun(multiFactory, false, System.currentTimeMillis());


    }


}
