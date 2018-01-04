package uk.ac.ox.oxfish.experiments;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbstractScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
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


    private final static Path MAIN_DIRECTORY = Paths.get("docs","indirect_inference", "calibration_short");

    private final static Path MLOGIT_SCRIPT = MAIN_DIRECTORY.resolve("mlogit_fit_full.R");

    private final static int NUMBER_OF_PAIRS=5000;


    /**
     * run the model many times to guess the
     * @param args name of the scenario
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {


        //reader and randomizer
        FishYAML yamler = new FishYAML();
        MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());

        String onlyScenario = args[0];
        IndirectInferencePaper.ScenarioInitializer selected = IndirectInferencePaper.initializers.get(onlyScenario);
        IndirectInferencePaper.initializers.clear();
        IndirectInferencePaper.initializers.put(args[0],selected);


        // the explore-exploit-imitate
        PerTripImitativeDestinationFactory exploreExploit = new PerTripImitativeDestinationFactory();



        Path scenarioDirectory = MAIN_DIRECTORY.resolve(onlyScenario);
        // here we keep the mlogit results
        String mlogitCSV = scenarioDirectory.resolve(onlyScenario + ".csv").toAbsolutePath().toString();
        // here we keep the parameters that originated them
        File eeiParameterCSV = scenarioDirectory.resolve(onlyScenario + "_parameters.csv").toFile();
        FileWriter writer = new FileWriter(eeiParameterCSV);
        writer.write("run,seed,exploration,imitation,step_size\n");

        long seed;
        for(int run=0; run<NUMBER_OF_PAIRS; run++) {

            //set up the scenario
            FileReader reader = new FileReader(
                    scenarioDirectory.resolve(args[0] + ".yaml").toFile()
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
                    args[0],
                    Long.toString(seed),
                    "explore20",
                    "explore20",
                    "TRUE",
                    MLOGIT_SCRIPT,
                    1
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
                    args[0],
                    Long.toString(seed),
                    "explore20",
                    "explore20",
                    "FALSE",
                    MLOGIT_SCRIPT, IndirectInferencePaper.SIMULATION_YEARS
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
        probability.setImitationProbability(new FixedDoubleParameter(random.nextDouble()*.90+.05));
        exploreExploit.setProbability(probability);
        exploreExploit.setStepSize(new FixedDoubleParameter(random.nextInt(9)+1));
        exploreExploit.setAlwaysCopyBest(true);
        exploreExploit.setAutomaticallyIgnoreAreasWhereFishNeverGrows(true);
        exploreExploit.setAutomaticallyIgnoreMPAs(true);
    }

}


