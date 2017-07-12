package uk.ac.ox.oxfish.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.DiscreteRandomAlgorithm;
import uk.ac.ox.oxfish.fisher.strategies.discarding.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.scenario.IndonesiaScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import static uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing.DEFAULT_DYNAMIC_NETWORK;

/**
 * Created by carrknight on 7/12/17.
 */
public class IndonesiaDiscarding {


    private static final Path scenarioFile = Paths.get("docs","20170715 minimum_indonesia","market.yaml");


    public static void main(String[] args) throws IOException {

        File outputFile = Paths.get("docs", "20170715 minimum_indonesia", "discarding.csv").toFile();
        FileWriter writer = new FileWriter(outputFile);
        writer.write("price_low,price_high,landings,earnings,cash-flow,landings_0,landings_1,landings_2,discarding_agents");
        writer.write("\n");
        writer.flush();
        for(double fine=9; fine>-100; fine= fine-.1d)
        {
            FishState state = new FishState(System.currentTimeMillis());
            FishYAML yaml = new FishYAML();
            IndonesiaScenario scenario = yaml.loadAs(
                    new FileReader(
                            scenarioFile.toFile()
                    ), IndonesiaScenario.class
            );
            state.setScenario(scenario);

            ThreePricesMarketFactory market = new ThreePricesMarketFactory();
            scenario.setMarket(market);
            market.setLowAgeThreshold(new FixedDoubleParameter(0));
            market.setPriceBelowThreshold(new FixedDoubleParameter(fine));

            state.start();
            while (state.getYear()<=5)
                state.schedule.step(state);

            for(Fisher fisher : state.getFishers())
            {
                DiscardUnderagedFactory discardUnderagedFactory = new DiscardUnderagedFactory();
                discardUnderagedFactory.setMinAgeRetained(new FixedDoubleParameter(1d));
                PeriodicUpdateDiscarding discarding = new PeriodicUpdateDiscarding(
                        Lists.newArrayList(NoDiscarding.class,
                                           DiscardUnderaged.class),
                        Lists.newArrayList(new NoDiscardingFactory(),
                                           discardUnderagedFactory)
                );
                discarding.start(state,fisher);
                fisher.setRegulation(new Anarchy());
            }

            while (state.getYear()<=15)
                state.schedule.step(state);


            StringBuffer observation = new StringBuffer();
            observation.append(fine).append(",");
            observation.append(10d).append(",");
            observation.append(state.getLatestYearlyObservation("Red Fish Landings")).append(",");
            observation.append(state.getLatestYearlyObservation("Red Fish Earnings")).append(",");
            observation.append(state.getLatestYearlyObservation("Average Cash-Flow")).append(",");
            observation.append(state.getLatestYearlyObservation("Red Fish Landings - age bin 0")).append(",");
            observation.append(state.getLatestYearlyObservation("Red Fish Landings - age bin 1")).append(",");
            observation.append(state.getLatestYearlyObservation("Red Fish Landings - age bin 2")).append(",");


            int discarders = 0;
            for(Fisher fisher: state.getFishers())
                if(!fisher.getDiscardingStrategy().getClass().equals(NoDiscarding.class))
                    discarders++;

            observation.append(discarders).append("\n");
            writer.write(observation.toString());
            writer.flush();
            System.out.println(observation);



        }



    }








}


class PeriodicUpdateDiscarding implements FisherStartable
{


    private final BiMap<Class<? extends DiscardingStrategy>,
            AlgorithmFactory<? extends DiscardingStrategy>> options;




    public PeriodicUpdateDiscarding(List<Class<? extends DiscardingStrategy>> discards,
                                    List<AlgorithmFactory<? extends DiscardingStrategy>> factories)
    {

        Preconditions.checkArgument(discards.size() == factories.size());

        options = HashBiMap.create(discards.size());
        for(int i=0; i<discards.size(); i++)
        {
            options.put(discards.get(i),factories.get(i));

        }

    }


    @Override
    public void start(FishState model, Fisher fisher) {

        fisher.addBiMonthlyAdaptation(
                new ExploreImitateAdaptation<Class<? extends DiscardingStrategy>>(
                        new Predicate<Fisher>() {
                            @Override
                            public boolean test(Fisher fisher1) {
                                return true;
                            }
                        },
                        new DiscreteRandomAlgorithm<Class<? extends DiscardingStrategy>>(
                                Lists.newArrayList(options.keySet())),
                        new Actuator<Fisher, Class<? extends DiscardingStrategy>>() {
                            @Override
                            public void apply(
                                    Fisher subject, Class<? extends DiscardingStrategy> policy, FishState model) {

                                subject.setDiscardingStrategy(
                                        options.get(policy).apply(model)
                                );
                            }
                        },
                        new Sensor<Fisher, Class<? extends DiscardingStrategy>>() {
                            @Override
                            public Class<? extends DiscardingStrategy> scan(Fisher system) {
                                return system.getDiscardingStrategy().getClass();
                            }
                        },
                        new CashFlowObjective(60),
                        new FixedProbability(.2, .6),
                        new Predicate<Class<? extends DiscardingStrategy>>() {
                            @Override
                            public boolean test(Class<? extends DiscardingStrategy> aClass) {
                                return true;
                            }
                        }

                )
        );


    }

    @Override
    public void turnOff(Fisher fisher) {

    }
}
