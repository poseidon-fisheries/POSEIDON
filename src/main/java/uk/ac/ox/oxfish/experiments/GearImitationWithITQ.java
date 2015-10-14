package uk.ac.ox.oxfish.experiments;


import ec.util.MersenneTwisterFast;
import sim.display.Console;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.factory.ITQMultiFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

public class GearImitationWithITQ
{


    public static void main(String[] args)
    {

        final FishState state = new FishState(System.currentTimeMillis());

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        final RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
        gearFactory.setMeanCatchabilityFirstSpecies(new UniformDoubleParameter(.001, .02));
        gearFactory.setMeanCatchabilityOtherSpecies(new UniformDoubleParameter(.001, .02));
        scenario.setGear(gearFactory);


        scenario.setCoastalRoughness(0);
        scenario.forcePortPosition(new int[]{40,25});

        scenario.setUsePredictors(true);

        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model)
            {

                for(Fisher fisher : model.getFishers())
                {
                    Adaptation<RandomCatchabilityTrawl> trawlAdaptation =
                            new Adaptation<RandomCatchabilityTrawl>(
                                    (Predicate<Fisher>) fisher1 -> true,
                                    new BeamHillClimbing<RandomCatchabilityTrawl>() {
                                        @Override
                                        public RandomCatchabilityTrawl randomStep(
                                                FishState state, MersenneTwisterFast random, Fisher fisher,
                                                RandomCatchabilityTrawl current) {
                                            return gearFactory.apply(state);
                                        }
                                    },
                                    (fisher1, change, state1) -> fisher1.setGear(
                                            change),
                                    fisher1 -> ((RandomCatchabilityTrawl) fisher1.getGear()),
                                    new CashFlowObjective(365),
                                    .1, .8);

                    //tell the fisher to use this once a year
                    fisher.addYearlyAdaptation(trawlAdaptation);




                }

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
        state.schedule.step(state);

        Path directory = Paths.get("docs", "20151014 corollaries");

        //initial distributions
        FishStateUtilities.pollHistogramToFile(
                fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0],
                state.getFishers(),
                directory.resolve("initial_red.csv").toFile());
        FishStateUtilities.pollHistogramToFile(
                fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1],
                state.getFishers(),
                directory.resolve("initial_blue.csv").toFile());

        while(state.getYear()<10)
            state.schedule.step(state);

        state.schedule.step(state);

        //final distributions
        FishStateUtilities.pollHistogramToFile(
                fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0],
                state.getFishers(),
                directory.resolve("final_red.csv").toFile());

        //initial distributions
        FishStateUtilities.pollHistogramToFile(
                fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1],
                state.getFishers(),
                directory.resolve("final_blue.csv").toFile());

        //show the effect on catches
        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                                                directory.resolve("red_landings.csv").toFile());

        FishStateUtilities.printCSVColumnToFile(state.getYearlyDataSet().getColumn(state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME),
                                                directory.resolve("blue_landings.csv").toFile());


    }


    public static void gui(String[] args)
    {


        final FishState state = new FishState(System.currentTimeMillis());

        ITQMultiFactory multiFactory = new ITQMultiFactory();
        //quota ratios: 90-10
        multiFactory.setQuotaFirstSpecie(new FixedDoubleParameter(4500));
        multiFactory.setQuotaOtherSpecies(new FixedDoubleParameter(500));
        //biomass ratio: 70-30
        WellMixedBiologyFactory biologyFactory = new WellMixedBiologyFactory();
        biologyFactory.setCapacityRatioSecondToFirst(new FixedDoubleParameter(.3));


        PrototypeScenario scenario = new PrototypeScenario();
        state.setScenario(scenario);
        scenario.setBiologyInitializer(biologyFactory);
        scenario.setRegulation(multiFactory);

        final RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
        gearFactory.setMeanCatchabilityFirstSpecies(new UniformDoubleParameter(.001, .02));
        gearFactory.setMeanCatchabilityOtherSpecies(new UniformDoubleParameter(.001, .02));
        scenario.setGear(gearFactory);


        scenario.setCoastalRoughness(0);
        scenario.forcePortPosition(new int[]{40,25});

        scenario.setUsePredictors(true);

        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model)
            {

                for(Fisher fisher : model.getFishers())
                {
                    Adaptation<RandomCatchabilityTrawl> trawlAdaptation =
                            new Adaptation<RandomCatchabilityTrawl>(
                                    (Predicate<Fisher>) fisher1 -> true,
                                    new BeamHillClimbing<RandomCatchabilityTrawl>() {
                                        @Override
                                        public RandomCatchabilityTrawl randomStep(
                                                FishState state, MersenneTwisterFast random, Fisher fisher,
                                                RandomCatchabilityTrawl current) {
                                            return gearFactory.apply(state);
                                        }
                                    },
                                    (fisher1, change, state1) -> fisher1.setGear(
                                            change),
                                    fisher1 -> ((RandomCatchabilityTrawl) fisher1.getGear()),
                                    new CashFlowObjective(365),
                                    .1, .8);

                    //tell the fisher to use this once a year
                    fisher.addYearlyAdaptation(trawlAdaptation);




                }

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

        FishGUI vid = new FishGUI(state);
        Console c = new Console(vid);
        c.setVisible(true);

    }

}
