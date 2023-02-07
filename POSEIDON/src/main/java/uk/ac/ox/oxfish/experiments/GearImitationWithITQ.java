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


import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GearImitationWithITQ
{






    public static void toFile(String[] args)
    {

        final Path directory = Paths.get("docs", "20151014 corollaries");

        FishState state = gearImitationWithITQ(System.currentTimeMillis(), 10, new Consumer<FishState>() {
                                                   @Override
                                                   public void accept(FishState state) {

                                                       //initial distributions
                                                       FishStateUtilities.pollHistogramToFile(
                                                               state.getFishers(),
                                                               directory.resolve("initial_red.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0]
                                                       );
                                                       FishStateUtilities.pollHistogramToFile(
                                                               state.getFishers(),
                                                               directory.resolve("initial_blue.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1]
                                                       );
                                                   }
                                               }

        );


        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), directory.resolve("final_red.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[0]
        );

        //initial distributions
        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), directory.resolve("final_blue.csv").toFile(), fisher -> ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[1]
        );

        //show the effect on catches
        FishStateUtilities.printCSVColumnToFile(directory.resolve("red_landings.csv").toFile(),
                                                state.getYearlyDataSet().getColumn(state.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

        FishStateUtilities.printCSVColumnToFile(directory.resolve("blue_landings.csv").toFile(),
                                                state.getYearlyDataSet().getColumn(state.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME)
        );

    }



    public static FishState gearImitationWithITQ(
            final long randomSeed, final int simulationRunTimeInYears,
            final Consumer<FishState> dayOneConsumer){

        final FishState state = new FishState(randomSeed);

        MultiITQFactory multiFactory = new MultiITQFactory();
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


        scenario.setMapInitializer(new SimpleMapInitializerFactory(50, 50, 0, 1000000, 5));

        scenario.forcePortPosition(new int[]{40,25});

        scenario.setUsePredictors(true);

        state.registerStartable(new Startable() {
            @Override
            public void start(FishState model)
            {

                for(Fisher fisher : model.getFishers())
                {
                    ExploreImitateAdaptation<RandomCatchabilityTrawl> trawlAdaptation =
                            new ExploreImitateAdaptation<RandomCatchabilityTrawl>(
                                    (Predicate<Fisher>) fisher1 -> true,
                                    new BeamHillClimbing<RandomCatchabilityTrawl>(
                                            new RandomStep<RandomCatchabilityTrawl>() {
                                                @Override
                                                public RandomCatchabilityTrawl randomStep(
                                                        FishState state, MersenneTwisterFast random, Fisher fisher,
                                                        RandomCatchabilityTrawl current) {
                                                    return gearFactory.apply(state);
                                                }
                                            }
                                    ),
                                    (fisher1, change, state1) -> fisher1.setGear(
                                            change),
                                    fisher1 -> ((RandomCatchabilityTrawl) fisher1.getGear()),
                                    new CashFlowObjective(365),
                                    .1, .8, new Predicate<RandomCatchabilityTrawl>() {
                                        @Override
                                        public boolean test(RandomCatchabilityTrawl a) {
                                            return true;
                                        }
                                    });

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

        dayOneConsumer.accept(state);

        while(state.getYear() < simulationRunTimeInYears)
            state.schedule.step(state);

        return state;




    }



}
