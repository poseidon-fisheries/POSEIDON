package uk.ac.ox.oxfish.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import org.apache.commons.collections15.Transformer;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.network.FriendshipEdge;
import uk.ac.ox.oxfish.model.network.NetworkBuilder;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.ITQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.FixedProbability;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.DoubleSummaryStatistics;
import java.util.function.Predicate;

/**
 * Created by carrknight on 2/7/17.
 */
public class SimpleDynamicNetworks
{




    public static void main(String[] args) throws IOException {




        EquidegreeBuilder builder = new EquidegreeBuilder();
      /*  for(int degree=0; degree<20; degree++) {
            builder.setDegree(degree);
            run("fixed" + degree,
                8,
                new AnarchyFactory(),
                builder,
                30,
                Paths.get("runs","networks","sweep.csv"));
        }
        */

        builder.setAllowMutualFriendships(false);

        adaptiveRun("anarchy",8,new AnarchyFactory(),builder,30,
                    Paths.get("runs","networks","sweep.csv"));


        ITQMonoFactory regulation = new ITQMonoFactory();
        regulation.setIndividualQuota(new FixedDoubleParameter(4000));
        adaptiveRun("itq", 8, regulation, builder, 30,
                    Paths.get("runs","networks","sweep.csv"));


        TACMonoFactory factory = new TACMonoFactory();
        factory.setQuota(new FixedDoubleParameter(4000*100));
        adaptiveRun("tac", 8, factory, builder, 30,
                    Paths.get("runs","networks","sweep.csv"));

        //mpa?
        ProtectedAreasOnlyFactory mpa = new ProtectedAreasOnlyFactory();
        mpa.setStartingMPAs(Lists.newArrayList(new StartingMPA(15,15,15,15)));
        adaptiveRun("mpa", 8, mpa, builder, 30,
                    Paths.get("runs","networks","sweep.csv"));

    }


    public static void run(String name,
                           int yearsToRun,
                           AlgorithmFactory<? extends  Regulation> regulation,
                           NetworkBuilder builder,
                           int numberOfRuns,
                           Path filePath) throws IOException {
        for(int run = 0; run<numberOfRuns; run++)
        {
            PrototypeScenario scenario = new PrototypeScenario();
            //no regrowth of biomass!
            DiffusingLogisticFactory biologyInitializer = new DiffusingLogisticFactory();
            SimpleLogisticGrowerFactory grower = new SimpleLogisticGrowerFactory();
            grower.setSteepness(new FixedDoubleParameter(0d));
            biologyInitializer.setGrower(grower);
            scenario.setBiologyInitializer(biologyInitializer);

            scenario.setRegulation(regulation);
            scenario.setNetworkBuilder(builder);
            FishState state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();
            while (state.getYear()<yearsToRun)
                state.schedule.step(state);
            DirectedGraph<Fisher, FriendshipEdge> network = state.getSocialNetwork().getBackingnetwork();
            DoubleSummaryStatistics averageDistance = new DoubleSummaryStatistics();
            DoubleSummaryStatistics degree = new DoubleSummaryStatistics();

            Transformer<Fisher, Double> distanceTransformer =
                    DistanceStatistics.averageDistances(
                            network,
                            new UnweightedShortestPath<>(
                                    network));
            double diameter = DistanceStatistics.diameter(network,new UnweightedShortestPath<>(network));

            for(Fisher fisher : state.getFishers())
            {
                degree.accept(network.degree(fisher));
                averageDistance.accept(distanceTransformer.transform(fisher));

            }
            double profits = 0d;
            for (Double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow")) {
                profits+=cash;
            }


            String toWrite = name + "," + diameter + "," +
                    averageDistance.getAverage() + "," + degree.getAverage() + "," + profits + "\n";
            System.out.print(toWrite);
            Files.write(filePath, toWrite.getBytes(), StandardOpenOption.APPEND);

        }
    }




    public static void adaptiveRun(String name,
                                   int yearsToRun,
                                   AlgorithmFactory<? extends  Regulation> regulation,
                                   NetworkBuilder builder,
                                   int numberOfRuns,
                                   Path filePath) throws IOException {
        for(int run = 0; run<numberOfRuns; run++)
        {
            PrototypeScenario scenario = new PrototypeScenario();
            //no regrowth of biomass!
            DiffusingLogisticFactory biologyInitializer = new DiffusingLogisticFactory();
            SimpleLogisticGrowerFactory grower = new SimpleLogisticGrowerFactory();
            grower.setSteepness(new FixedDoubleParameter(0d));
            biologyInitializer.setGrower(grower);
            scenario.setBiologyInitializer(biologyInitializer);

            scenario.setRegulation(regulation);
            scenario.setNetworkBuilder(builder);
            FishState state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();

            for(Fisher fisher : state.getFishers())
            {
                fisher.addBiMonthlyAdaptation(new ExploreImitateAdaptation<Integer>(
                        (Predicate<Fisher>) fisher1 -> true,
                        new BeamHillClimbing<Integer>(new RandomStep<Integer>() {
                            @Override
                            public Integer randomStep(FishState state, MersenneTwisterFast random, Fisher fisher,
                                                      Integer current) {
                                return Math.min(
                                        Math.max(
                                                current + random.nextInt(6) - 3,0),
                                        state.getFishers().size() - 1);
                            }
                        })
                        ,
                        (Actuator<Fisher, Integer>) (subject, policy, model) -> {
                            int target = Math.min(Math.max(policy, 0), model.getFishers().size() - 1);
                            int difference = target - model.getSocialNetwork().getAllNeighbors(subject).size();
                            if (difference > 0) {
                                for (int i = 0; i < difference; i++)
                                    model.getSocialNetwork().addRandomFriend(subject, model.getFishers(),
                                                                             model.getRandom());
                                ;
                            }
                            if (difference < 0) {
                                for (int i = 0; i < -difference; i++)
                                {
                                    int before = model.getSocialNetwork().getAllNeighbors(subject).size();
                                    model.getSocialNetwork().removeRandomFriend(subject, true, model.getRandom());
                                    int after = model.getSocialNetwork().getAllNeighbors(subject).size();
                                }
                            }
                            Preconditions.checkArgument(
                                    model.getSocialNetwork().getAllNeighbors(subject).size() == target);
                        },
                        (Sensor<Fisher, Integer>) system -> state.getSocialNetwork().getAllNeighbors(system).size(),
                        new CashFlowObjective(60),
                        new FixedProbability(.2, 1),
                        (Predicate<Integer>) integer -> true


                ));
            }

            while (state.getYear()<yearsToRun)
                state.schedule.step(state);
            DirectedGraph<Fisher, FriendshipEdge> network = state.getSocialNetwork().getBackingnetwork();
            DoubleSummaryStatistics averageDistance = new DoubleSummaryStatistics();
            DoubleSummaryStatistics degree = new DoubleSummaryStatistics();

            Transformer<Fisher, Double> distanceTransformer =
                    DistanceStatistics.averageDistances(
                            network,
                            new UnweightedShortestPath<>(
                                    network));
            double diameter = DistanceStatistics.diameter(network,new UnweightedShortestPath<>(network));

            for(Fisher fisher : state.getFishers())
            {
                degree.accept(network.degree(fisher));
                averageDistance.accept(distanceTransformer.transform(fisher));

            }
            double profits = 0d;
            for (Double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow")) {
                profits+=cash;
            }


            String toWrite = name + "," + diameter + "," +
                    averageDistance.getAverage() + "," + degree.getAverage() + "," + profits + "\n";
            System.out.print(toWrite);
            Files.write(filePath, toWrite.getBytes(), StandardOpenOption.APPEND);

        }
    }


}
