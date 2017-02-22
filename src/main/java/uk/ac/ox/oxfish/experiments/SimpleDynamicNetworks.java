package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import org.apache.commons.collections15.Transformer;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
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
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
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
    private final static Path INPUT_FILE = Paths.get("runs", "networks", "twenty_best_big.yaml");
    private static final Path OUTPUT_FILE = Paths.get("runs", "networks", "all_results.csv");
    private static final Path COV_FILE = Paths.get("runs", "networks", "covariances.csv");

    private static final Path COV_FILE2 = Paths.get("runs", "networks", "covariances_each.csv");
    public static final int NUMBER_OF_RUNS = 100;
    public static final int YEARS_TO_RUN = 4;
    public static final int NUMBER_OF_FISHERS = 100;

    public static void main(String[] args) throws IOException {


        File outputFile = OUTPUT_FILE.toFile();
        if(!outputFile.exists())
        {
            Files.write(OUTPUT_FILE,"scenario,fishers,adaptive,name,diameter,distance,degree,profits\n".getBytes());
        }

        File covarianceFile = COV_FILE.toFile();
        if(!covarianceFile.exists())
        {
            Files.write(COV_FILE,"scenario,fishers,name,diameter,distance,degree,correlation_pre,correlation_suc,correlation_degree\n".getBytes());
        }


        File covarianceFileIndividual = COV_FILE2.toFile();
        if(!covarianceFileIndividual.exists())
        {
            Files.write(COV_FILE2,"scenario,fishers,name,diameter,distance,avg_degree,predecessors,successors,degree,profits\n".getBytes());
        }


        Supplier<NetworkBuilder> supplier =  () -> {
            EquidegreeBuilder builder = new EquidegreeBuilder();

            builder.setDegree(new UniformDoubleParameter(0,Math.min(40,NUMBER_OF_FISHERS-1)));
            builder.setAllowMutualFriendships(true);
            return builder;
        };


        run("uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            new AnarchyFactory(),
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);


        ITQMonoFactory itq = new ITQMonoFactory();
        itq.setIndividualQuota(new FixedDoubleParameter(4000));

        run("itq_uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            itq,
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);


        itq.setIndividualQuota(new FixedDoubleParameter(2000));

        run("lowitq_uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            itq,
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);

        itq.setIndividualQuota(new FixedDoubleParameter(1000));

        run("verylowitq_uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            itq,
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);


        itq.setIndividualQuota(new FixedDoubleParameter(8000));

        run("highitq_uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            itq,
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);


        TACMonoFactory factory = new TACMonoFactory();
        factory.setQuota(new FixedDoubleParameter(4000* NUMBER_OF_FISHERS));

        run("tac_uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            factory,
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);


        factory.setQuota(new FixedDoubleParameter(2000* NUMBER_OF_FISHERS));

        run("lowtac_uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            factory,
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);



        factory.setQuota(new FixedDoubleParameter(1000* NUMBER_OF_FISHERS));

        run("verylowtac_uniform",
            YEARS_TO_RUN,
            INPUT_FILE,
            factory,
            supplier,
            NUMBER_OF_RUNS,
            OUTPUT_FILE, COV_FILE,COV_FILE2);




        for(int degree=0; degree<20 && degree <NUMBER_OF_FISHERS/2 + 1; degree++) {
            int finalDegree = degree;
            supplier = () -> {
                EquidegreeBuilder builder = new EquidegreeBuilder();

                builder.setDegree(new FixedDoubleParameter(finalDegree));
                builder.setAllowMutualFriendships(false);
                return builder;
            };

            run("fixed" + degree,
                YEARS_TO_RUN,
                INPUT_FILE,
                new AnarchyFactory(),
                supplier,
                NUMBER_OF_RUNS,
                OUTPUT_FILE, COV_FILE,null);


            itq = new ITQMonoFactory();
            itq.setIndividualQuota(new FixedDoubleParameter(4000));

            run("itq_fixed" + degree,
                YEARS_TO_RUN,
                INPUT_FILE,
                itq,
                supplier,
                NUMBER_OF_RUNS,
                OUTPUT_FILE, COV_FILE,null);


            itq.setIndividualQuota(new FixedDoubleParameter(2000));

            run("lowitq_fixed" + degree,
                YEARS_TO_RUN,
                INPUT_FILE,
                itq,
                supplier,
                NUMBER_OF_RUNS,
                OUTPUT_FILE, COV_FILE,null);


            itq.setIndividualQuota(new FixedDoubleParameter(8000));

            run("highitq_fixed" + degree,
                YEARS_TO_RUN,
                INPUT_FILE,
                itq,
                supplier,
                NUMBER_OF_RUNS,
                OUTPUT_FILE, COV_FILE,null);


            factory = new TACMonoFactory();
            factory.setQuota(new FixedDoubleParameter(4000* NUMBER_OF_FISHERS));

            run("tac_fixed" + degree,
                YEARS_TO_RUN,
                INPUT_FILE,
                factory,
                supplier,
                NUMBER_OF_RUNS,
                OUTPUT_FILE, COV_FILE,null);



        }

        /*
        adaptiveRun("anarchy", YEARS_TO_RUN, INPUT_FILE,
                    new AnarchyFactory(), supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);


        ITQMonoFactory itq = new ITQMonoFactory();
        itq.setIndividualQuota(new FixedDoubleParameter(4000));
        adaptiveRun("itq", YEARS_TO_RUN, INPUT_FILE,
                    itq, supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);


        TACMonoFactory factory = new TACMonoFactory();
        factory.setQuota(new FixedDoubleParameter(4000* NUMBER_OF_FISHERS));
        adaptiveRun("tac", YEARS_TO_RUN, INPUT_FILE,
                    factory, supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);

        //mpa?
        ProtectedAreasOnlyFactory mpa = new ProtectedAreasOnlyFactory();
        mpa.setStartingMPAs(Lists.newArrayList(new StartingMPA(15,15,15,15)));
        adaptiveRun("mpa", YEARS_TO_RUN, INPUT_FILE,
                    mpa, supplier, NUMBER_OF_RUNS,
                    OUTPUT_FILE);
*/

    }


    public static void run(
            String name,
            int yearsToRun,
            Path inputYamlPath,
            AlgorithmFactory<? extends Regulation> regulation,
            Supplier<NetworkBuilder> builder,
            int numberOfRuns,
            Path filePath, Path covariancePath, Path covariance2Path) throws IOException {
        for(int run = 0; run<numberOfRuns; run++)
        {
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(new FileReader(inputYamlPath.toFile()), PrototypeScenario.class);

            scenario.setRegulation(regulation);
            scenario.setNetworkBuilder(builder.get());
            scenario.setFishers(NUMBER_OF_FISHERS);
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


            String toWrite =inputYamlPath.getFileName().toString() + "," +scenario.getFishers() + "," +
                    true + "," +
                    name + "," + diameter + "," +
                    averageDistance.getAverage() + "," + degree.getAverage() + "," + profits + "\n";
            System.out.print(toWrite);
            Files.write(filePath, toWrite.getBytes(), StandardOpenOption.APPEND);
            for(Fisher fisher : state.getFishers())
                fisher.turnOff();
            state.finish();


            double[] predeccesors = new double[NUMBER_OF_FISHERS];
            double[] successors = new double[NUMBER_OF_FISHERS];
            double[] degreeArray = new double[NUMBER_OF_FISHERS];
            double[] profitArray = new double[NUMBER_OF_FISHERS];
            String incipit = inputYamlPath.getFileName().toString() + "," +scenario.getFishers() + "," +
                    name + "," + diameter + "," +
                    averageDistance.getAverage() + "," + degree.getAverage() + ",";
            for(int i=0; i<NUMBER_OF_FISHERS; i++)
            {
                Fisher fisher = state.getFishers().get(i);
                degreeArray[i] = fisher.getAllFriends().size();
                predeccesors[i] = state.getSocialNetwork().getBackingnetwork().getPredecessorCount(fisher);
                successors[i] =fisher.getDirectedFriends().size();
                profitArray[i] = fisher.getLatestYearlyObservation("NET_CASH_FLOW");


                if(covariance2Path != null)
                {
                    toWrite = incipit +
                            predeccesors[i]  + "," +
                            successors[i]  + "," +
                            degreeArray[i]  + "," +
                            profitArray[i]  + "\n";
                    Files.write(covariance2Path,toWrite.getBytes(),StandardOpenOption.APPEND);
                }



            }

            toWrite = incipit +
                    FishStateUtilities.computeCorrelation(predeccesors,profitArray) +  "," +
                    FishStateUtilities.computeCorrelation(successors,profitArray) +  "," +
                    FishStateUtilities.computeCorrelation(degreeArray,profitArray) +
                    "\n";
            Files.write(covariancePath, toWrite.getBytes(), StandardOpenOption.APPEND);
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


            scenario.setFishers(NUMBER_OF_FISHERS);
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


            String toWrite =inputYamlPath.getFileName().toString() + "," +scenario.getFishers() + "," +
                    false + "," +
                    name + "," + diameter + "," +
                    averageDistance.getAverage() + "," + degree.getAverage() + "," + profits + "\n";
            System.out.print(toWrite);
            Files.write(filePath, toWrite.getBytes(), StandardOpenOption.APPEND);
            for(Fisher fisher : state.getFishers())
                fisher.turnOff();
            state.finish();

        }
    }


}
