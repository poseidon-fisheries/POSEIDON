package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import org.apache.commons.collections15.Transformer;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.selfanalysis.SocialNetworkAdaptation;
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
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.DoubleSummaryStatistics;
import java.util.function.Supplier;

/**
 * Created by carrknight on 2/7/17.
 */
public class SimpleDynamicNetworks
{


    //private final static Path INPUT_FILE = Paths.get("runs", "networks", "twenty_limited.yaml");
    //private static final Path OUTPUT_FILE = Paths.get("runs", "networks", "twenty_limited.csv");
    private final static Path INPUT_FILE = Paths.get("runs", "networks", "chaser_congested.yaml");
    private static final Path OUTPUT_FILE = Paths.get("runs", "networks", "chaser_congested2.csv");
    public static final int NUMBER_OF_RUNS = 100;
    public static final int YEARS_TO_RUN = 20;

    public static void main(String[] args) throws IOException {



        Supplier<NetworkBuilder> supplier = () -> {
            EquidegreeBuilder builder = new EquidegreeBuilder();

            builder.setDegree(3);
            builder.setAllowMutualFriendships(false);
            return builder;
        };



        adaptiveRun("anarchy", YEARS_TO_RUN, INPUT_FILE,
                    new AnarchyFactory(), supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);


        ITQMonoFactory regulation = new ITQMonoFactory();
        regulation.setIndividualQuota(new FixedDoubleParameter(4000));
        adaptiveRun("itq", YEARS_TO_RUN, INPUT_FILE,
                    regulation, supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);


        TACMonoFactory factory = new TACMonoFactory();
        factory.setQuota(new FixedDoubleParameter(4000* 100));
        adaptiveRun("tac", YEARS_TO_RUN, INPUT_FILE,
                    factory, supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);

        //mpa?
        ProtectedAreasOnlyFactory mpa = new ProtectedAreasOnlyFactory();
        mpa.setStartingMPAs(Lists.newArrayList(new StartingMPA(15,15,15,15)));
        adaptiveRun("mpa", YEARS_TO_RUN, INPUT_FILE,
                    mpa, supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);


        for(int degree=0; degree<20; degree++) {
            int finalDegree = degree;
            supplier = () -> {
                EquidegreeBuilder builder = new EquidegreeBuilder();

                builder.setDegree(finalDegree);
                builder.setAllowMutualFriendships(false);
                return builder;
            };
            run("fixed" + degree,
                YEARS_TO_RUN,
                INPUT_FILE,
                new AnarchyFactory(),
                supplier,
                NUMBER_OF_RUNS,
                OUTPUT_FILE);
        }
    }


    public static void run(String name,
                           int yearsToRun,
                           Path inputYamlPath,
                           AlgorithmFactory<? extends  Regulation> regulation,
                           Supplier<NetworkBuilder> builder,
                           int numberOfRuns,
                           Path filePath) throws IOException {
        for(int run = 0; run<numberOfRuns; run++)
        {
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(new FileReader(inputYamlPath.toFile()), PrototypeScenario.class);

            scenario.setRegulation(regulation);
            scenario.setNetworkBuilder(builder.get());
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
            for(Fisher fisher : state.getFishers())
                fisher.turnOff();
            state.finish();

        }
    }




    public static void adaptiveRun(String name,
                                   int yearsToRun,
                                   Path inputYamlPath,
                                   AlgorithmFactory<? extends  Regulation> regulation,
                                   Supplier<NetworkBuilder> builder,
                                   int numberOfRuns,
                                   Path filePath) throws IOException {
        for(int run = 0; run<numberOfRuns; run++)
        {
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(new FileReader(inputYamlPath.toFile()), PrototypeScenario.class);


            scenario.setRegulation(regulation);
            scenario.setNetworkBuilder(builder.get());
            FishState state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();

            for(Fisher fisher : state.getFishers())
            {
                fisher.addBiMonthlyAdaptation(
                        new SocialNetworkAdaptation(5,
                                                    new CashFlowObjective(60),
                                                    .2));
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
            for(Fisher fisher : state.getFishers())
                fisher.turnOff();
            state.finish();

        }
    }


}
