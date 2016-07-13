package uk.ac.ox.oxfish.demoes;


import com.esotericsoftware.minlog.Log;
import org.junit.Assert;
import org.junit.Test;
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
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;


public class GearImitationWithITQ
{

    @Test
    public void ITQDrivePeopleToSwitchToBetterGear() throws Exception {


        MultiITQFactory multiFactory = new MultiITQFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        long seed = System.currentTimeMillis();
        Log.info("seed is : " + seed);
        gearImitationTestRun(multiFactory, true, seed);



    }

    @Test
    public void UnprotectedVersusProtectedGearSwitch() throws Exception {


        MultiITQStringFactory multiFactory = new MultiITQStringFactory();
        //only blue are protected by quota
        multiFactory.setYearlyQuotaMaps("1:500");

        gearImitationTestRun(multiFactory, false, System.currentTimeMillis());


    }

    public FishState gearImitationTestRun(
            AlgorithmFactory<? extends  MultiQuotaRegulation> multiFactory, boolean checkRed,
            final long seed) throws IOException {
        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                Paths.get("inputs","first_paper","gear_itq.yaml")));
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml,PrototypeScenario.class);
        FishState state = new FishState();
        state.setScenario(scenario);

        scenario.setRegulation(multiFactory);

        //set up the gear adaptation:
        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {

                //start collecting red catchability and blue catchability
                model.getYearlyDataSet().registerGatherer("Red Catchability", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers())
                            total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[0]
                                    ;
                        return total / size;
                    }
                }, Double.NaN);


                model.getYearlyDataSet().registerGatherer("Blue Catchability", state1 -> {
                    double size = state1.getFishers().size();
                    if (size == 0)
                        return Double.NaN;
                    else {
                        double total = 0;
                        for (Fisher fisher1 : state1.getFishers())
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
        if(state.getDayOfTheYear()==1)
            Log.info("Red catchability: " + red + " --- Blue Catchability: " + blue);
        state.schedule.step(state);
        double earlyRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
                                                                                        AbstractMarket.LANDINGS_COLUMN_NAME);
        double earlyBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
                                                                                         AbstractMarket.LANDINGS_COLUMN_NAME);

        System.out.println("Early Landings: " + earlyRedLandings + " --- " + earlyBlueLandings);
        //blue start as a choke species
        double totalBlueQuotas = 500 * 100;
        //   Assert.assertTrue(earlyBlueLandings > .8 * totalBlueQuotas);
        //red is underutilized
        if(checkRed) {
            double totalRedQuotas = 4500 * 100;
            System.out.println("red landings are " + earlyRedLandings/totalRedQuotas + " of the total quota" );
            //         Assert.assertTrue(earlyRedLandings < .5 * totalRedQuotas);
        }

        double lateRedLandings;
        double lateBlueLandings;

        while (state.getYear() < 30) {
            state.schedule.step(state);
            blue = state.getYearlyDataSet().getLatestObservation("Blue Catchability");
            red = state.getYearlyDataSet().getLatestObservation("Red Catchability");
            if(state.getDayOfTheYear()==1) {
                Log.info("Red catchability: " + red + " --- Blue Catchability: " + blue);

                lateRedLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(0) + " " +
                                                                                        AbstractMarket.LANDINGS_COLUMN_NAME);
                lateBlueLandings = state.getYearlyDataSet().getLatestObservation(state.getSpecies().get(1) + " " +
                                                                                         AbstractMarket.LANDINGS_COLUMN_NAME);
                System.out.println("Late Landings: " +
                                           (lateRedLandings/( (4500 * 100))) +
                                           " --- " +
                                           (lateBlueLandings/( (500 * 100))));
            }
        }


        state.schedule.step(state);
        blue = state.getYearlyDataSet().getLatestObservation("Blue Catchability");
        red = state.getYearlyDataSet().getLatestObservation("Red Catchability");
        System.out.println("Red catchability: " + red + " --- Blue Catchability: " + blue);

        assertTrue(red > .01);
        assertTrue(blue < .01);
        assertTrue(red > blue + .005);

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
        Assert.assertTrue(lateBlueLandings > .75 * totalBlueQuotas);
        if(checkRed)
        {
            double totalRedQuotas = 4500 * 100;

            Assert.assertTrue(
                    lateRedLandings > .7 * totalRedQuotas); //this is actually almost always above 90% after 20 years
        }

        return state;

    }


}
