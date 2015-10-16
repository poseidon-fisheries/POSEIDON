package uk.ac.ox.oxfish.demoes;


import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.factory.ITQMultiFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.function.Predicate;

import static org.junit.Assert.assertTrue;


public class GearImitationWithITQ
{


    @Test
    public void ITQDrivePeopleToSwitchToBetterGear() throws Exception {


            System.out.println("Test starting!");
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
            scenario.forcePortPosition(new int[]{40, 25});

            scenario.setUsePredictors(true);

            state.registerStartable(new Startable() {
                @Override
                public void start(FishState model) {

                    for (Fisher fisher : model.getFishers()) {
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

            while (state.getYear() < 10)
                state.schedule.step(state);

            state.schedule.step(state);
            Double blue = state.getYearlyDataSet().getLatestObservation("Blue Catchability");
            Double red = state.getYearlyDataSet().getLatestObservation("Red Catchability");
            System.out.println(red + " --- " + blue);

            assertTrue(red > .01);
            assertTrue(blue < .01);
            assertTrue(red > blue+ .005);

    }
}
