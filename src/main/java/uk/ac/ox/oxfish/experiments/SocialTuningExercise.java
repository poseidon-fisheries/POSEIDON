package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.HeatmapDestinationFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PlanningHeatmapDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.CaliforniaBathymetryScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Runs social tuning many times, looking for the right parameter
 * Created by carrknight on 8/30/16.
 */
public class SocialTuningExercise {


    private final static int YEARS_TO_RUN = 5;
    public static final Path MAIN_DIRECTORY = Paths.get("runs", "social_tuning");
    public static final int NUMBER_OF_EXPERIMENTS = 100;

    public static void nn(String[] args) throws IOException {


        StringBuilder output = new StringBuilder("time,x,y,distance,habitat,cash");
        Log.set(Log.LEVEL_INFO);
        String inputScenario =  String.join("\n", Files.readAllLines(
                MAIN_DIRECTORY.resolve("nn.yaml")));
        for(int experiment = 1; experiment< NUMBER_OF_EXPERIMENTS; experiment++)
        {
            Log.info("Starting experiment " + experiment);
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(inputScenario, PrototypeScenario.class);

            FishState state = new FishState(experiment);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()<YEARS_TO_RUN)
                state.schedule.step(state);
            output.append("\n");
            for(int i=0; i<5; i++) {
                output.append(
                        state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter "+i)
                ).append(",");

            }

            double total = 0;
            for(double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
                total+=cash;
            output.append(total);


        }

        Files.write(MAIN_DIRECTORY.resolve("nn.csv"), output.toString().getBytes());





    }



    public static void kernel(String[] args) throws IOException {


        StringBuilder output = new StringBuilder("x,y,distance,habitat");
        Log.set(Log.LEVEL_INFO);
        String inputScenario =  String.join("\n", Files.readAllLines(
                MAIN_DIRECTORY.resolve("kernel.yaml")));
        for(int experiment = 1; experiment< NUMBER_OF_EXPERIMENTS; experiment++)
        {
            Log.info("Starting experiment " + experiment);
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(inputScenario, PrototypeScenario.class);

            FishState state = new FishState(experiment);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()<YEARS_TO_RUN)
                state.schedule.step(state);
            output.append("\n");
            for(int i=0; i<4; i++) {
                output.append(
                        state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter "+i)
                ).append(",");

            }

            double total = 0;
            for(double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
                total+=cash;
            output.append(total);


        }

        Files.write(MAIN_DIRECTORY.resolve("kernel.csv"), output.toString().getBytes());





    }


    public static void kalman(String[] args) throws IOException {


        StringBuilder output = new StringBuilder("distance,evidence,drift,optimism,penalty");
        Log.set(Log.LEVEL_INFO);
        String inputScenario =  String.join("\n", Files.readAllLines(
                MAIN_DIRECTORY.resolve("kalman.yaml")));
        for(int experiment = 1; experiment< NUMBER_OF_EXPERIMENTS; experiment++)
        {
            Log.info("Starting experiment " + experiment);
            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario = yaml.loadAs(inputScenario, PrototypeScenario.class);

            FishState state = new FishState(experiment);
            state.setScenario(scenario);
            state.start();
            while(state.getYear()<YEARS_TO_RUN)
                state.schedule.step(state);
            output.append("\n");
            for(int i=0; i<5; i++) {
                output.append(
                        state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter "+i)
                ).append(",");

            }

            double total = 0;
            for(double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
                total+=cash;
            output.append(total);


        }

        Files.write(MAIN_DIRECTORY.resolve("kalman.csv"), output.toString().getBytes());





    }


    public static void fine(String[] args) throws IOException {


        HashMap<String, AlgorithmFactory<? extends GeographicalRegression<Double>>> strategies = new LinkedHashMap<>();
        HashMap<String, String[]> headers = new LinkedHashMap<>();

        //nearest neighbor
        CompleteNearestNeighborRegressionFactory nn = new CompleteNearestNeighborRegressionFactory();
        nn.setDistanceFromPortBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setHabitatBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setTimeBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setxBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setyBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setNeighbors(new UniformDoubleParameter(1, 10));
        strategies.put("nn", nn);
        headers.put("nn", new String[]{"time", "x", "y", "distance", "habitat","neighbors"});
        //kalman
        SimpleKalmanRegressionFactory kalman = new SimpleKalmanRegressionFactory();
        kalman.setDistancePenalty(new UniformDoubleParameter(1, 100));
        kalman.setEvidenceUncertainty(new UniformDoubleParameter(1, 100));
        kalman.setFishingHerePenalty(new UniformDoubleParameter(-0.5, 2));
        kalman.setInitialUncertainty(new FixedDoubleParameter(10000));
        kalman.setOptimism(new UniformDoubleParameter(-2, 2));
        kalman.setDrift(new UniformDoubleParameter(1, 100));
        strategies.put("kalman", kalman);
        headers.put("kalman", new String[]{"distance", "evidence", "drift", "optimism", "penalty"});
        //kernel
        DefaultRBFKernelTransductionFactory kernel = new DefaultRBFKernelTransductionFactory();
        kernel.setDistanceFromPortBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setHabitatBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setxBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setyBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setForgettingFactor(new FixedDoubleParameter(.95));
        strategies.put("kernel", kernel);
        headers.put("kernel", new String[]{"x", "y", "distance", "habitat"});


        for (Map.Entry<String, AlgorithmFactory<? extends GeographicalRegression<Double>>>
                strategy : strategies.entrySet()) {

            int numberOfParameters = headers.get(strategy.getKey()).length;
            StringBuilder output = new StringBuilder();
            for(String parameter : headers.get(strategy.getKey())) {
                output.append(parameter).append(",");
            }
            output.append("cash");

            Log.set(Log.LEVEL_INFO);
            Log.info("starting " + strategy.getKey());
            String inputScenario = String.join("\n", Files.readAllLines(
                    MAIN_DIRECTORY.resolve("fine.yaml")));

            for (int experiment = 1; experiment < NUMBER_OF_EXPERIMENTS; experiment++) {
                Log.info("Starting experiment " + experiment);
                FishYAML yaml = new FishYAML();
                PrototypeScenario scenario = yaml.loadAs(inputScenario, PrototypeScenario.class);

                ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                        scenario.getDestinationStrategy()).getRegression()).setNested(
                                strategy.getValue()
                );

                FishState state = new FishState(experiment);
                state.setScenario(scenario);
                state.start();
                while (state.getYear() < YEARS_TO_RUN)
                    state.schedule.step(state);
                output.append("\n");
                for (int i = 0; i < numberOfParameters; i++) {
                    output.append(
                            state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter " + i)
                    ).append(",");

                }

                double total = 0;
                for (double cash : state.getYearlyDataSet().getColumn("Average Cash-Flow"))
                    total += cash;
                output.append(total);


            }

            Files.write(MAIN_DIRECTORY.resolve(strategy.getKey()+"_fine.csv"), output.toString().getBytes());


        }
    }


    public static void main(String[] args) throws IOException {


        /*

        batchRun("nn.yaml", "fronts",
                 pair -> ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);

        batchRun("fine.yaml", "fine",
                 pair -> ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);




        batchRun("front_plan.yaml", "plan",
                 pair -> ((SocialTuningRegressionFactory) ((PlanningHeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);


        batchRun("fine_plan.yaml", "fineplan",
                 pair -> ((SocialTuningRegressionFactory) ((PlanningHeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);

        */


        batchRun("cali_anarchy.yaml", "calianarchy",
                 pair -> ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                         ((CaliforniaBathymetryScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 1);






    }

    public static void batchRun(
            final String inputFile, final String outputName,
            Consumer<Pair<Scenario, AlgorithmFactory<? extends GeographicalRegression<Double>>>>
                    strategyAssigner, final int yearsToRun, final int firstValidYear) throws IOException {


        HashMap<String, AlgorithmFactory<? extends GeographicalRegression<Double>>> strategies = new LinkedHashMap<>();
        HashMap<String, String[]> headers = new LinkedHashMap<>();

        //nearest neighbor
        CompleteNearestNeighborRegressionFactory nn = new CompleteNearestNeighborRegressionFactory();
        nn.setDistanceFromPortBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setHabitatBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setTimeBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setxBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setyBandwidth(new UniformDoubleParameter(1, 1000));
        nn.setNeighbors(new UniformDoubleParameter(1, 10));
        strategies.put("nn", nn);
        headers.put("nn", new String[]{"time", "x", "y", "distance", "habitat","neighbors"});
        //kalman
        SimpleKalmanRegressionFactory kalman = new SimpleKalmanRegressionFactory();
        kalman.setDistancePenalty(new UniformDoubleParameter(1, 100));
        kalman.setEvidenceUncertainty(new UniformDoubleParameter(1, 100));
        kalman.setFishingHerePenalty(new UniformDoubleParameter(-0.5, 2));
        kalman.setInitialUncertainty(new FixedDoubleParameter(10000));
        kalman.setOptimism(new UniformDoubleParameter(-2, 2));
        kalman.setDrift(new UniformDoubleParameter(1, 100));
        strategies.put("kalman", kalman);
        headers.put("kalman", new String[]{"distance", "evidence", "drift", "optimism", "penalty"});
        //gwr
        GeographicallyWeightedRegressionFactory gwr = new GeographicallyWeightedRegressionFactory();
        gwr.setExponentialForgetting(new UniformDoubleParameter(.8,1));
        gwr.setRbfBandwidth(new UniformDoubleParameter(.1,50));
        strategies.put("gwr",gwr);
        headers.put("gwr",new String[]{"forgetting", "bandwidth"});

        //good-bad regression
        GoodBadRegressionFactory goodBad = new GoodBadRegressionFactory();
        goodBad.setBadAverage(new UniformDoubleParameter(-20,0));
        goodBad.setGoodAverage(new UniformDoubleParameter(10,30));
        goodBad.setStandardDeviation(new UniformDoubleParameter(10,30));
        goodBad.setDistancePenalty(new UniformDoubleParameter(.1,50));
        strategies.put("goodBad", goodBad);
        headers.put("goodBad", new String[]{"bad", "good", "std", "distance"});

        //epa
        DefaultEpanechnikovKernelRegressionFactory epa = new DefaultEpanechnikovKernelRegressionFactory();
        epa.setTimeBandwidth(new UniformDoubleParameter(100,10000));
        epa.setNumberOfObservations(new FixedDoubleParameter(100));
        epa.setxBandwidth(new UniformDoubleParameter(1,1000));
        epa.setyBandwidth(new UniformDoubleParameter(1,1000));
        epa.setDistanceFromPortBandwidth(new UniformDoubleParameter(1,1000));
        epa.setHabitatBandwidth(new UniformDoubleParameter(1,1000));
        strategies.put("epa", epa);
        headers.put("epa", new String[]{"x", "y", "distance", "habitat","time"});

        //kernel
        DefaultRBFKernelTransductionFactory kernel = new DefaultRBFKernelTransductionFactory();
        kernel.setDistanceFromPortBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setHabitatBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setxBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setyBandwidth(new UniformDoubleParameter(1, 200));
        kernel.setForgettingFactor(new FixedDoubleParameter(.95));
        strategies.put("kernel", kernel);
        headers.put("kernel", new String[]{"x", "y", "distance", "habitat"});


        for (Map.Entry<String, AlgorithmFactory<? extends GeographicalRegression<Double>>>
                strategy : strategies.entrySet()) {

            int numberOfParameters = headers.get(strategy.getKey()).length;
            StringBuilder output = new StringBuilder();
            for(String parameter : headers.get(strategy.getKey())) {
                output.append(parameter).append(",");
            }
            output.append("cash");

            Log.set(Log.LEVEL_INFO);
            Log.info("starting " + strategy.getKey());
            String inputScenario = String.join("\n", Files.readAllLines(
                    MAIN_DIRECTORY.resolve(inputFile)));

            for (int experiment = 1; experiment < NUMBER_OF_EXPERIMENTS; experiment++) {
                Log.info("Starting experiment " + experiment);
                FishYAML yaml = new FishYAML();
                Scenario scenario = yaml.loadAs(inputScenario, Scenario.class);

                strategyAssigner.accept(new Pair<>(scenario,strategy.getValue()));



                FishState state = new FishState(experiment);
                state.setScenario(scenario);
                state.start();
                while (state.getYear() < yearsToRun)
                    state.schedule.step(state);
                output.append("\n");
                for (int i = 0; i < numberOfParameters; i++) {
                    output.append(
                            state.getYearlyDataSet().getLatestObservation("Average Heatmap Parameter " + i)
                    ).append(",");

                }

                double total = 0;
                DataColumn cashColumn = state.getYearlyDataSet().getColumn("Average Cash-Flow");
                for (int i = firstValidYear; i < cashColumn.size(); i++  )
                    total += cashColumn.get(i);
                output.append(total);


            }

            Files.write(MAIN_DIRECTORY.resolve(strategy.getKey()+ "_" + outputName + ".csv"), output.toString().getBytes());


        }
    }




}
