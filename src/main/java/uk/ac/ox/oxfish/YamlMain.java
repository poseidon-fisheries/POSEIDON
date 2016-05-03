package uk.ac.ox.oxfish;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PolicyScripts;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.oxfish.utility.yaml.ModelResults;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by carrknight on 11/18/15.
 */
public class YamlMain {

    @Parameter(names ={"--seed","-s"}, description ="random seed for simulation")
    private Long seed = System.currentTimeMillis();

    @Parameter(names={"--log","-l"},description = "the verbosity level of the logging")
    private int logLevel = Log.LEVEL_INFO;

    @Parameter(names={"--years","-t"}, description = "number of years the simulation has to run")
    private int yearsToRun = 20;

    @Parameter(names={"--save"},description = "saves model on file at the end of the simulation")
    private boolean saveOnExit = false;

    @Parameter(names = {"--policy","-p"},description = "path to policy script file")
    private String policyScript = null;

    public static void main(String[] args) throws IOException {

        /**
         * the first argument is always the scenario file
         */
        Path inputFile = Paths.get(args[0]);
        String simulationName = inputFile.getFileName().toString();
        simulationName = simulationName.split("\\.")[0];

        YamlMain main = new YamlMain();
        if(args.length>1) //if there are multiple parameters, read them up!
            new JCommander(main, Arrays.copyOfRange(args,1,args.length));
        main.run(simulationName,inputFile);


    }

    public void  run(String simulationName,
                     Path inputFolder) throws IOException {
        Path outputFolder = Paths.get("output", simulationName);
        outputFolder.toFile().mkdirs();

        //create scenario and files
        String fullScenario = String.join("\n", Files.readAllLines(inputFolder));

        FishYAML yaml = new FishYAML();
        Scenario scenario = yaml.loadAs(fullScenario, Scenario.class);
        yaml.dump(scenario,new FileWriter(outputFolder.resolve("scenario.yaml").toFile()));

        FishState model = new FishState(seed);
        Log.setLogger(new FishStateLogger(model,
                                          outputFolder.resolve(simulationName+ "_log.txt")));
        Log.set(logLevel);
        model.setScenario(scenario);
        model.start();

        //if you have a policy script, then follow it
        if(policyScript != null)
        {
            String policyScript = new String(Files.readAllBytes(Paths.get(this.policyScript)));
            PolicyScripts scripts = PolicyScripts.fromYaml(yaml,policyScript);
            model.registerStartable(scripts);
            Files.write(outputFolder.resolve("policy_script.yaml"),
                        yaml.dump(scripts.getScripts()).getBytes());
        }


        while(model.getYear()<yearsToRun) {
            model.schedule.step(model);
            if(Log.DEBUG && model.getDayOfTheYear()==1)
                Log.debug("Year " + model.getYear() + " starting");
        }

        FileWriter writer = new FileWriter(outputFolder.resolve("result.yaml").toFile());
        ModelResults results =  new ModelResults(model);
        yaml.dump(results,writer);

        writer = new FileWriter(outputFolder.resolve("seed.txt").toFile());
        writer.write(Long.toString(seed));
        writer.close();

        if(saveOnExit)
            FishStateUtilities.writeModelToFile(
                    outputFolder.resolve(simulationName+".checkpoint").toFile(),
                    model);


    }


}
