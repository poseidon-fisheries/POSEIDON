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

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.growers.SimpleLogisticGrowerFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.factory.ExhaustiveAcquisitionFunctionFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.KernelTransductionFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.*;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.SocialAnnealingProbabilityFactory;
import uk.ac.ox.oxfish.utility.bandit.factory.SoftmaxBanditFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class IndirectInferencePaper {



    private final static Path mainDirectory = Paths.get("docs","indirect_inference", "simulation");

    private final static Path mlogitScript = mainDirectory.resolve("mlogit_fit.R");


    /**
     * list of names and associated "initializers" which are supposed to randomize some scenario parameters
     */
    private final static LinkedHashMap<String,
            ScenarioInitializer> initializers = new LinkedHashMap<>();
    public static final int TARGET_RUNS = 10;



    public static final int SIMULATION_YEARS = 10;

    static
    {

        //the baseline scenario: fishing front and all
        initializers.put(
                "baseline",
                new ScenarioInitializer() {
                    @Override
                    public void initialize(Scenario scenario, long seed,
                                           AlgorithmFactory<? extends DestinationStrategy> strategy)
                    {

                        PrototypeScenario cast = (PrototypeScenario) scenario;
                        //randomize biomass, speed and port position
                        MersenneTwisterFast random = new MersenneTwisterFast(seed);
                        DiffusingLogisticFactory biology = (DiffusingLogisticFactory) cast
                                .getBiologyInitializer();
                        biology.setCarryingCapacity(
                                new FixedDoubleParameter(random.nextDouble()*9000+1000)
                        );
                        biology.setDifferentialPercentageToMove(
                                new FixedDoubleParameter(random.nextDouble()*.003)
                        );
                        ((SimpleLogisticGrowerFactory) biology.getGrower()).setSteepness(
                                new FixedDoubleParameter(random.nextDouble()*.5 + .3)
                        );

                        SimpleMapInitializerFactory map = new SimpleMapInitializerFactory();
                        map.setHeight(new FixedDoubleParameter(50));
                        map.setWidth(new FixedDoubleParameter(50));
                        map.setCoastalRoughness(new FixedDoubleParameter(0));
                        map.setMaxLandWidth(new FixedDoubleParameter(10));
                        cast.setMapInitializer(map);
                        cast.setPortPositionX(40);
                        cast.setPortPositionY(random.nextInt(50));

                        cast.setMapMakerDedicatedRandomSeed(seed);

                        cast.setDestinationStrategy(strategy);

                    }
                }
        );



    }


    /**
     * store list of names of the algorithms to use and their factory; this is used for the model-selection bit
     */
    private final static LinkedHashMap<String,
            AlgorithmFactory<? extends DestinationStrategy>> strategies =
            new LinkedHashMap<>();



    //fill up the strategies map with pre-made models
    static {

        //perfect agents
        LogitRPUEDestinationFactory perfect = new LogitRPUEDestinationFactory();
        SquaresMapDiscretizerFactory discretizer = new SquaresMapDiscretizerFactory();
        discretizer.setHorizontalSplits(new FixedDoubleParameter(2));
        discretizer.setVerticalSplits(new FixedDoubleParameter(2));
        perfect.setDiscretizer(discretizer);
        strategies.put(
                "perfect3by3",
                perfect
        );


        //3 variants of explore-exploit-imitate
        PerTripImitativeDestinationFactory exploreExploit = new PerTripImitativeDestinationFactory();
        exploreExploit.setProbability(new FixedProbabilityFactory(.2,1));
        exploreExploit.setStepSize(new FixedDoubleParameter(5));
        exploreExploit.setAlwaysCopyBest(true);
        exploreExploit.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreExploit.setAutomaticallyIgnoreMPAs(true);
        strategies.put("explore20",exploreExploit);

        PerTripImitativeDestinationFactory exploreExploit80 = new PerTripImitativeDestinationFactory();
        exploreExploit80.setProbability(new FixedProbabilityFactory(.8,1));
        exploreExploit80.setStepSize(new FixedDoubleParameter(5));
        exploreExploit80.setAlwaysCopyBest(true);
        exploreExploit80.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreExploit80.setAutomaticallyIgnoreMPAs(true);
        strategies.put("explore80",exploreExploit80);

        PerTripImitativeDestinationFactory exploreLarge = new PerTripImitativeDestinationFactory();
        exploreLarge.setProbability(new FixedProbabilityFactory(.2,1));
        exploreLarge.setStepSize(new FixedDoubleParameter(20));
        exploreLarge.setAlwaysCopyBest(true);
        exploreLarge.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreLarge.setAutomaticallyIgnoreMPAs(true);
        strategies.put("exploreLarge",exploreLarge);

        //heatmapper (these are the parameters in the original kernel regression)
        HeatmapDestinationFactory heatmap = new HeatmapDestinationFactory();
        ExhaustiveAcquisitionFunctionFactory acquisition = new ExhaustiveAcquisitionFunctionFactory();
        acquisition.setProportionSearched(new FixedDoubleParameter(.1));
        heatmap.setAcquisition(acquisition);
        heatmap.setExplorationStepSize(new FixedDoubleParameter(1));
        heatmap.setProbability(new FixedProbabilityFactory(.5,1));
        KernelTransductionFactory regression = new KernelTransductionFactory();
        regression.setForgettingFactor(new FixedDoubleParameter(0.999989));
        regression.setSpaceBandwidth(new FixedDoubleParameter(20));
        heatmap.setRegression(regression);
        strategies.put("kernel",heatmap);

        //social annealing
        PerTripImitativeDestinationFactory annealing = new PerTripImitativeDestinationFactory();
        annealing.setAlwaysCopyBest(true);
        annealing.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        annealing.setAutomaticallyIgnoreMPAs(true);
        annealing.setStepSize(new FixedDoubleParameter(5));
        annealing.setProbability(new SocialAnnealingProbabilityFactory(.7));
        strategies.put("annealing",annealing);


        //2 softmax bandits (differ in number of splits!)
        BanditDestinationFactory bandit = new BanditDestinationFactory();
        SoftmaxBanditFactory softmax = new SoftmaxBanditFactory();
        bandit.setBandit(softmax);
        SquaresMapDiscretizerFactory banditDiscretizer = new SquaresMapDiscretizerFactory();
        banditDiscretizer.setVerticalSplits(new FixedDoubleParameter(2));
        banditDiscretizer.setHorizontalSplits(new FixedDoubleParameter(2));
        bandit.setDiscretizer(banditDiscretizer);
        strategies.put("bandit3by3",bandit);

        BanditDestinationFactory bandit2 = new BanditDestinationFactory();
        SoftmaxBanditFactory softmax2 = new SoftmaxBanditFactory();
        bandit2.setBandit(softmax2);
        SquaresMapDiscretizerFactory banditDiscretizer2 = new SquaresMapDiscretizerFactory();
        banditDiscretizer2.setVerticalSplits(new FixedDoubleParameter(4));
        banditDiscretizer2.setHorizontalSplits(new FixedDoubleParameter(4));
        bandit2.setDiscretizer(banditDiscretizer2);
        strategies.put("bandit5by5",bandit2);

        //gravitational pull
        GravitationalSearchDestinationFactory gravitational = new GravitationalSearchDestinationFactory();
        strategies.put("gravitational",gravitational);

        //randomizer
        strategies.put(
                "random",
                new RandomThenBackToPortFactory()
        );

    }




    public static void main(String[] args) throws IOException, InterruptedException {


        //reader and randomizer
        FishYAML yamler = new FishYAML();
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());


        for (Map.Entry<String, ScenarioInitializer> initializer : initializers.entrySet())
        {

            Path scenarioDirectory = mainDirectory.resolve(initializer.getKey());
            Path inputDirectory = scenarioDirectory.resolve("inputs");


            for (Map.Entry<String, AlgorithmFactory<? extends DestinationStrategy>> targetStrategy :
                    strategies.entrySet())
            {

                for(int run = 0; run< TARGET_RUNS; run++)
                {

                    Scenario mainScenario = yamler.loadAs(
                            new FileReader(
                                    scenarioDirectory.resolve(initializer.getKey() + ".yaml").toFile()
                            ), Scenario.class

                    );
                    //first run the target!
                    initializer.getValue().initialize(mainScenario,run,targetStrategy.getValue());
                    String targetName = targetStrategy.getKey() + "_" + run;
                    Path output = scenarioDirectory.resolve("output").resolve(targetName);
                    output.toFile().mkdirs();
                    //write down the scenario to file;
                    //this is in order to keep a record of everything
                    inputDirectory.toFile().mkdirs();
                    yamler.dump(mainScenario,
                                new FileWriter(
                                        inputDirectory.resolve(targetName+".yaml").toFile())
                    );

                    Log.info("Starting target run : " + targetName);
                    FishStateUtilities.run(
                            targetName,
                            inputDirectory.resolve(targetName+".yaml"),
                            output,
                            (long)run,
                            Log.LEVEL_INFO,
                            false,
                            null,
                            SIMULATION_YEARS,
                            false,
                            -1
                    );

                    //at the end I'd like a CSV like this:
                    // run, scenario, seed, target-strategy,current-strategy,isTargetRun,beta_0,beta_0_sd,beta_1,beta_1_sd,....

                    /*
                    Rscript ~/code/oxfish/docs/indirect_inference/simulation/baseline/mlogit_fit.R   ~/code/oxfish/docs/indirect_inference/simulation/baseline/output/perfect3by3_1/logistic_long.csv ~/code/oxfish/docs/indirect_inference/simulation/baseline/baseline.csv 2 baseline 2 perfect3by3 perfect3by3 TRUE
                     */
                    String pathToRScript  = mlogitScript.toAbsolutePath().toString();
                    String pathToLogbook = output.resolve("logistic_long.csv").toAbsolutePath().toString();
                    String pathToCSV = scenarioDirectory.resolve(initializer.getKey() + ".csv").toAbsolutePath().toString();
                    String runArgument = Integer.toString(run);
                    String scenario = initializer.getKey();
                    String seedArgument = runArgument;
                    String targetStrategyArgument = targetStrategy.getKey();
                    String currentStrategyArgument = targetStrategyArgument;
                    String isTargetRun = "TRUE";

                    String[] arguments =
                            new String[]{
                            "Rscript",
                            pathToRScript,
                                    pathToLogbook,
                                    pathToCSV,
                                    runArgument,
                                    scenario,
                                    seedArgument,
                                    targetStrategyArgument,
                                    currentStrategyArgument,
                                    isTargetRun
                            };
                    Log.info(Arrays.toString(arguments));
                    Process exec = Runtime.getRuntime().exec(arguments);
                    int code = exec.waitFor();
                    switch (code) {
                        case 0:
                            //normal termination, everything is fine
                            break;
                        case 1:
                            //Read the error stream then
                            String message =convertStreamToString(exec.getErrorStream());
                            throw new RuntimeException(message);
                    }


                }


            }


        }

    }


    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }



    private interface ScenarioInitializer
    {

        void initialize(Scenario scenario, long seed,
                        AlgorithmFactory<? extends DestinationStrategy> destinationStrategy);


    }



}
