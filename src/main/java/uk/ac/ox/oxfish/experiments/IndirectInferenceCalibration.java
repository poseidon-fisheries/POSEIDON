package uk.ac.ox.oxfish.experiments;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbstractScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IndirectInferenceCalibration {


    private final static Path MAIN_DIRECTORY = Paths.get("docs","indirect_inference", "calibration_short_epsilon");

    private final static Path MLOGIT_SCRIPT = MAIN_DIRECTORY.resolve("mlogit_fit_full.R");

    private final static int NUMBER_OF_PAIRS=5000;


    private final static boolean ONE_DIMENSIONAL = true;

    /**
     * run the model many times to guess the
     * @param args name of the scenario
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        baselineScenarioTargets();
        //generateTrainingData(args[0]);


    }

    public static void generateTrainingData(String arg) throws IOException, InterruptedException {
        //reader and randomizer
        FishYAML yamler = new FishYAML();
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        String onlyScenario = arg;
        IndirectInferencePaper.ScenarioInitializer selected = IndirectInferencePaper.initializers.get(onlyScenario);
        IndirectInferencePaper.initializers.clear();
        IndirectInferencePaper.initializers.put(arg, selected);


        // the explore-exploit-imitate
        PerTripImitativeDestinationFactory exploreExploit = new PerTripImitativeDestinationFactory();


        Path scenarioDirectory = MAIN_DIRECTORY.resolve(onlyScenario);
        // here we keep the mlogit results
        String mlogitCSV = scenarioDirectory.resolve(onlyScenario + ".csv").toAbsolutePath().toString();
        Path aggregatesCSV = scenarioDirectory.resolve(onlyScenario + "_aggregates.csv");
        boolean alreadyExists = aggregatesCSV.toFile().exists();
        if(!alreadyExists) {

            try (FileWriter writer =
                         new FileWriter(aggregatesCSV.toFile(),true)) {
                writer.append(
                        "landings,effort,distance,trips,hours,profits,run,target_strategy,current_strategy,scenario,isTargetRun,seed");

                writer.append("\n");
                writer.close();
            }
        }
        // here we keep the parameters that originated them
        File eeiParameterCSV = scenarioDirectory.resolve(onlyScenario + "_parameters.csv").toFile();
        FileWriter writer = new FileWriter(eeiParameterCSV);
        writer.write("run,seed,exploration,imitation,step_size\n");

        long seed;
        for(int run=0; run<NUMBER_OF_PAIRS; run++) {

            //set up the scenario
            FileReader reader = new FileReader(
                    scenarioDirectory.resolve(arg + ".yaml").toFile()
            );
            Scenario mainScenario = yamler.loadAs(
                    reader, Scenario.class

            );
            updateStrategy(random, exploreExploit);
            if(mainScenario instanceof PrototypeScenario)
                ((PrototypeScenario) mainScenario).setDestinationStrategy(exploreExploit);
            else
                ((CaliforniaAbstractScenario) mainScenario).setDestinationStrategy(exploreExploit);


            seed = random.nextLong();
            String targetName = "run_" + run + "_" + seed;
            Path output = scenarioDirectory.resolve("output").resolve(targetName);
            //run "target"
            Path inputDirectory = scenarioDirectory.resolve("inputs");
            inputDirectory.toFile().mkdirs();
            FileWriter yamlWriter = new FileWriter(
                    inputDirectory.resolve(targetName + ".yaml").toFile());
            yamler.dump(mainScenario,
                        yamlWriter
            );
            yamlWriter.close();


            IndirectInferencePaper.runOneSimulation(
                    inputDirectory,
                    seed,
                    targetName,
                    output,
                    mlogitCSV,
                    Integer.toString(run),
                    arg,
                    Long.toString(seed),
                    "explore20",
                    "explore20",
                    "TRUE",
                    MLOGIT_SCRIPT,
                    1,
                    aggregatesCSV

            );
            //  writer.write("run,seed,exploration,imitation,step_size\n");

            writer.write(Integer.toString(run));
            writer.write(",");
            writer.write(Long.toString(seed));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter)
                    ((FixedProbabilityFactory) exploreExploit.getProbability()).getExplorationProbability()).getFixedValue()));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter)
                    ((FixedProbabilityFactory) exploreExploit.getProbability()).getImitationProbability()).getFixedValue()));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter) exploreExploit.getStepSize()).getFixedValue()));
            writer.write("\n");
            writer.flush();



            //run "other"
            updateStrategy(random, exploreExploit);
            if(mainScenario instanceof PrototypeScenario)
                ((PrototypeScenario) mainScenario).setDestinationStrategy(exploreExploit);
            else
                ((CaliforniaAbstractScenario) mainScenario).setDestinationStrategy(exploreExploit);
            seed = random.nextLong();
            targetName = "run_" + run + "_" + seed;
            output = scenarioDirectory.resolve("output").resolve(targetName);
            yamlWriter = new FileWriter(
                    inputDirectory.resolve(targetName + ".yaml").toFile());
            yamler.dump(mainScenario,
                        yamlWriter
            );
            yamlWriter.close();
            IndirectInferencePaper.runOneSimulation(
                    scenarioDirectory.resolve("inputs"),
                    seed,
                    targetName,
                    output,
                    mlogitCSV,
                    Integer.toString(run),
                    arg,
                    Long.toString(seed),
                    "explore20",
                    "explore20",
                    "FALSE",
                    MLOGIT_SCRIPT,
                    IndirectInferencePaper.SIMULATION_YEARS,
                    aggregatesCSV
            );

            writer.write(Integer.toString(run));
            writer.write(",");
            writer.write(Long.toString(seed));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter)
                    ((FixedProbabilityFactory) exploreExploit.getProbability()).getExplorationProbability()).getFixedValue()));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter)
                    ((FixedProbabilityFactory) exploreExploit.getProbability()).getImitationProbability()).getFixedValue()));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter) exploreExploit.getStepSize()).getFixedValue()));
            writer.write("\n");
            writer.flush();
        }


        writer.close();
    }

    private static void updateStrategy(MersenneTwisterFast random, PerTripImitativeDestinationFactory exploreExploit) {
        FixedProbabilityFactory probability = new FixedProbabilityFactory(.2, 1);
        probability.setExplorationProbability(new FixedDoubleParameter(random.nextDouble()*.90+.05));
        if (ONE_DIMENSIONAL) {
            probability.setImitationProbability(new FixedDoubleParameter(1));
        } else {
            probability.setImitationProbability(new FixedDoubleParameter(random.nextDouble()*.90+.05));
        }
        exploreExploit.setProbability(probability);
        if (ONE_DIMENSIONAL) {
            exploreExploit.setStepSize(new FixedDoubleParameter(5));
        } else {
            exploreExploit.setStepSize(new FixedDoubleParameter(random.nextInt(9)+1));
        }

        exploreExploit.setAlwaysCopyBest(true);
        exploreExploit.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreExploit.setAutomaticallyIgnoreMPAs(true);
    }



    public static void baselineScenarioTargets() throws IOException, InterruptedException {


        Path scenarioDirectory =  Paths.get("docs","indirect_inference", "bayes","target");
        // here we keep the mlogit results
        String mlogitCSV = scenarioDirectory.resolve("baseline.csv").toAbsolutePath().toString();
        Path aggregatesCSV = scenarioDirectory.resolve("baseline_aggregates.csv");
        boolean alreadyExists = aggregatesCSV.toFile().exists();
        if(!alreadyExists) {

            try (FileWriter writer =
                         new FileWriter(aggregatesCSV.toFile(),true)) {
                writer.append(
                        "landings,effort,distance,trips,hours,profits,run,target_strategy,current_strategy,scenario,isTargetRun,seed");

                writer.append("\n");
                writer.close();
            }
        }
        // here we keep the parameters that originated them
        File eeiParameterCSV = scenarioDirectory.resolve("baseline_parameters.csv").toFile();
        FileWriter writer = new FileWriter(eeiParameterCSV);
        writer.write("run,seed,exploration,imitation,step_size\n");

        long seed;
        FishYAML yamler = new FishYAML();
        MersenneTwisterFast random = new MersenneTwisterFast();

        PerTripImitativeDestinationFactory exploreExploit = new PerTripImitativeDestinationFactory();
        FixedProbabilityFactory probability = new FixedProbabilityFactory(.2, 1);
        probability.setImitationProbability(new FixedDoubleParameter(1));

        exploreExploit.setProbability(probability);
        exploreExploit.setStepSize(new FixedDoubleParameter(5));

        exploreExploit.setAlwaysCopyBest(true);
        exploreExploit.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreExploit.setAutomaticallyIgnoreMPAs(true);


        for(double explorationRate=0; explorationRate<=1; explorationRate= FishStateUtilities.round(explorationRate + .1)) {

            //set up the scenario
            FileReader reader = new FileReader(
                    scenarioDirectory.resolve("baseline.yaml").toFile()
            );
            probability.setExplorationProbability(new FixedDoubleParameter(explorationRate));



            PrototypeScenario mainScenario = yamler.loadAs(reader, PrototypeScenario.class);
            mainScenario.setDestinationStrategy(exploreExploit);

            seed = (int)(explorationRate * 100);
            String targetName = "run_" + explorationRate + "_" + seed;
            Path output = scenarioDirectory.resolve("output").resolve(targetName);
            //run "target"
            Path inputDirectory = scenarioDirectory.resolve("inputs");
            inputDirectory.toFile().mkdirs();
            FileWriter yamlWriter = new FileWriter(
                    inputDirectory.resolve(targetName + ".yaml").toFile());
            yamler.dump(mainScenario,
                        yamlWriter
            );
            yamlWriter.close();


            IndirectInferencePaper.runOneSimulation(
                    inputDirectory,
                    seed,
                    targetName,
                    output,
                    mlogitCSV,
                    Long.toString(seed),
                    "baseline",
                    Long.toString(seed),
                    "explore20",
                    "explore20",
                    "TRUE",
                    MLOGIT_SCRIPT,
                    IndirectInferencePaper.SIMULATION_YEARS,
                    aggregatesCSV

            );
            writer.write(Long.toString(seed));
            writer.write(",");
            writer.write(Long.toString(seed));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter)
                    ((FixedProbabilityFactory) exploreExploit.getProbability()).getExplorationProbability()).getFixedValue()));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter)
                    ((FixedProbabilityFactory) exploreExploit.getProbability()).getImitationProbability()).getFixedValue()));
            writer.write(",");
            writer.write(Double.toString(((FixedDoubleParameter) exploreExploit.getStepSize()).getFixedValue()));
            writer.write("\n");
            writer.flush();

        }

    }
}


