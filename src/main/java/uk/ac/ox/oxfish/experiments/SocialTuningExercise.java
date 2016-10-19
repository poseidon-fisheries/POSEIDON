package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.CaliforniaBathymetryScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.SocialAnnealingProbabilityFactory;
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




    public static void main(String[] args) throws IOException {


   //       defaults("nn.yaml", "_fronts",YEARS_TO_RUN,0);
   //     defaults("fine.yaml", "_fine",YEARS_TO_RUN,0);
        defaults("cali_anarchy.yaml", "_calianarchy",YEARS_TO_RUN,1);
 //       defaults("cali_itq.yaml", "_caliitq",YEARS_TO_RUN,1);
/*

        batchRun("nn.yaml", "_fronts",
                 pair -> ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);

        batchRun("fine.yaml", "_fine",
                 pair -> ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);




        batchRun("front_plan.yaml", "-plan_fronts",
                 pair -> ((SocialTuningRegressionFactory) ((PlanningHeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);


        batchRun("fine_plan.yaml", "-plan_fine",
                 pair -> ((SocialTuningRegressionFactory) ((PlanningHeatmapDestinationFactory)
                         ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 0);





        batchRun("cali_anarchy.yaml", "_calianarchy",
                 pair -> ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                         ((CaliforniaBathymetryScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 1);


        batchRun("cali_itq.yaml", "_caliitq",
                 pair -> ((SocialTuningRegressionFactory) ((HeatmapDestinationFactory)
                         ((CaliforniaBathymetryScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 1);



        batchRun("cali_anarchy_plan.yaml", "-plan_calianarchy",
                 pair -> ((SocialTuningRegressionFactory) ((PlanningHeatmapDestinationFactory)
                         ((CaliforniaBathymetryScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 1);
*/

/*

        batchRun("cali_itq_plan.yaml", "-plan_caliitq",
                 pair -> ((SocialTuningRegressionFactory) ((PlanningHeatmapDestinationFactory)
                         ((CaliforniaBathymetryScenario) pair.getFirst()).getDestinationStrategy()).getRegression()).setNested(
                         pair.getSecond()
                 ), YEARS_TO_RUN, 1);
*/






        //personals
        Consumer<Pair<Scenario, AlgorithmFactory<? extends GeographicalRegression<Double>>>> personal =
                pair -> {
                    PersonalTuningRegressionFactory personalTuning = new PersonalTuningRegressionFactory();
                    ((HeatmapDestinationFactory) ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).
                            setRegression(personalTuning);
                    personalTuning.setNested(pair.getSecond());

                };

        Consumer<Pair<Scenario, AlgorithmFactory<? extends GeographicalRegression<Double>>>> personalPlan =
                pair -> {
                    PersonalTuningRegressionFactory personalTuning = new PersonalTuningRegressionFactory();
                    ((PlanningHeatmapDestinationFactory) ((PrototypeScenario) pair.getFirst()).getDestinationStrategy()).
                            setRegression(personalTuning);
                    personalTuning.setNested(pair.getSecond());

                };
/*
        batchRun("nn.yaml", "-personal_fronts",
                 personal
                , YEARS_TO_RUN, 0);

        batchRun("fine.yaml", "-personal_fine",
                 personal, YEARS_TO_RUN, 0);

       batchRun("front_plan.yaml", "-personal-plan_fronts",
                 personalPlan, YEARS_TO_RUN, 0);


        batchRun("fine_plan.yaml", "-personal-plan_fine",
                 personalPlan, YEARS_TO_RUN, 0);





        personal =
                pair -> {
                    PersonalTuningRegressionFactory personalTuning = new PersonalTuningRegressionFactory();
                    ((HeatmapDestinationFactory) ((CaliforniaBathymetryScenario) pair.getFirst()).getDestinationStrategy()).
                            setRegression(personalTuning);
                    personalTuning.setNested(pair.getSecond());

                };




        batchRun("cali_anarchy.yaml", "-personal_calianarchy",
                 personal, YEARS_TO_RUN, 1);


        batchRun("cali_itq.yaml", "-personal_caliitq",
                 personal, YEARS_TO_RUN, 1);

*/


        personalPlan =
                pair -> {
                    PersonalTuningRegressionFactory personalTuning = new PersonalTuningRegressionFactory();
                    ((PlanningHeatmapDestinationFactory) ((CaliforniaBathymetryScenario) pair.getFirst()).getDestinationStrategy()).
                            setRegression(personalTuning);
                    personalTuning.setNested(pair.getSecond());

                };

/*
        batchRun("cali_anarchy_plan.yaml", "-personal-plan_calianarchy",
                 personalPlan, YEARS_TO_RUN, 1);



        batchRun("cali_itq_plan.yaml", "-personal-plan_caliitq",
                 personalPlan, YEARS_TO_RUN, 1);
                 */
    }


    public static void defaults(
            final String inputFile, final String outputName,
            final int yearsToRun,
            final int firstValidYear) throws IOException
    {

        HashMap<String, AlgorithmFactory<? extends DestinationStrategy>> strategies = new LinkedHashMap<>();

        strategies.put("eei", new PerTripImitativeDestinationFactory());

        PlanningHeatmapDestinationFactory perfectPlanner = new PlanningHeatmapDestinationFactory();
        perfectPlanner.setAlmostPerfectKnowledge(true);
        ExhaustiveAcquisitionFunctionFactory acquisition = new ExhaustiveAcquisitionFunctionFactory();
        perfectPlanner.setAcquisition(acquisition);
        acquisition.setProportionSearched(new FixedDoubleParameter(.1));
//        strategies.put("perfect",perfectPlanner);


        strategies.put("gsa",new GravitationalSearchDestinationFactory());
        strategies.put("pso",new PerTripParticleSwarmFactory());
        PerTripImitativeDestinationFactory annealing = new PerTripImitativeDestinationFactory();
        annealing.setProbability(new SocialAnnealingProbabilityFactory());
        annealing.setBacktracksOnBadExploration(false);
        strategies.put("annealing",annealing);


        for (Map.Entry<String, AlgorithmFactory<? extends DestinationStrategy>>
                strategy : strategies.entrySet()) {

            StringBuilder output = new StringBuilder();
            output.append("cash");

            Log.set(Log.LEVEL_INFO);
            Log.info("starting " + strategy.getKey());
            String inputScenario = String.join("\n", Files.readAllLines(
                    MAIN_DIRECTORY.resolve(inputFile)));

            for (int experiment = 1; experiment < NUMBER_OF_EXPERIMENTS; experiment++) {
                Log.info("Starting experiment " + experiment);
                FishYAML yaml = new FishYAML();
                Scenario scenario = yaml.loadAs(inputScenario, Scenario.class);

                if(scenario instanceof  PrototypeScenario)
                    ((PrototypeScenario) scenario).setDestinationStrategy(strategy.getValue());
                else
                {
                    assert scenario instanceof CaliforniaBathymetryScenario;
                    ((CaliforniaBathymetryScenario) scenario).setDestinationStrategy(strategy.getValue());
                }

                FishState state = new FishState(experiment);
                state.setScenario(scenario);
                state.start();
                while (state.getYear() < yearsToRun)
                    state.schedule.step(state);
                output.append("\n");
                double total = 0;
                DataColumn cashColumn = state.getYearlyDataSet().getColumn("Average Cash-Flow");
                for (int i = firstValidYear; i < cashColumn.size(); i++  )
                    total += cashColumn.get(i);
                output.append(total);


            }

            Files.write(MAIN_DIRECTORY.resolve(strategy.getKey()+ outputName + ".csv"), output.toString().getBytes());


        }


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

        //rbf
        DefaultKernelRegressionFactory rbf = new DefaultKernelRegressionFactory();
        rbf.setTimeBandwidth(new UniformDoubleParameter(100,100000));
        rbf.setNumberOfObservations(new FixedDoubleParameter(100));
        rbf.setxBandwidth(new UniformDoubleParameter(50,500));
        rbf.setyBandwidth(new UniformDoubleParameter(50,500));
        rbf.setDistanceFromPortBandwidth(new UniformDoubleParameter(50,500));
        rbf.setHabitatBandwidth(new UniformDoubleParameter(50,500));
        rbf.setRbfKernel(true);
        strategies.put("rbf", rbf);
        headers.put("rbf", new String[]{"x", "y", "distance", "habitat","time"});

        //epa
        DefaultKernelRegressionFactory epa = new DefaultKernelRegressionFactory();
        epa.setTimeBandwidth(new UniformDoubleParameter(100,100000));
        epa.setNumberOfObservations(new FixedDoubleParameter(100));
        epa.setxBandwidth(new UniformDoubleParameter(50,500));
        epa.setyBandwidth(new UniformDoubleParameter(50,500));
        epa.setDistanceFromPortBandwidth(new UniformDoubleParameter(50,500));
        epa.setHabitatBandwidth(new UniformDoubleParameter(50,500));
        epa.setRbfKernel(false);
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

            Files.write(MAIN_DIRECTORY.resolve(strategy.getKey()+ outputName + ".csv"), output.toString().getBytes());


        }
    }




}
