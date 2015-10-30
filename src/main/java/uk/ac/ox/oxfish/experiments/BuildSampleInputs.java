package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.Scenarios;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Dump every scenario and every algorithm factory as an example
 * Created by carrknight on 7/13/15.
 */
public class BuildSampleInputs
{


    public static void main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();

        Path directory = Paths.get("inputs", "YAML Samples","scenario");
        directory.toFile().mkdirs();
        //start with each Scenario; populate them with the standard variables
        for(Scenario scenario : Scenarios.SCENARIOS.values())
        {
            String name = Scenarios.SCENARIOS.inverse().get(scenario);
            final Path file = directory.resolve(name+ ".yaml");
            final String dump = yaml.dump(scenario);
            FileWriter writer = new FileWriter(file.toFile());
            if(Objects.equals(name, "Prototype"))
            {
                File base =                Dashboard.DASHBOARD_INPUT_DIRECTORY.resolve("base.yaml").toFile();

                FileWriter auxiliary = new FileWriter(base);
                auxiliary.write(dump);
                auxiliary.close();
            }
            writer.write(dump);
            writer.close();
        }

        directory = Paths.get("inputs", "YAML Samples","components");
        directory.toFile().mkdirs();
        //the same for all
        for(Map.Entry<Class,Map<String,? extends Supplier<? extends AlgorithmFactory<?>>>> algorithms : AlgorithmFactories.CONSTRUCTOR_MAP.entrySet())
        {
            String name = algorithms.getKey().getSimpleName();
            final Path file = directory.resolve(name + ".yaml");
            StringBuilder buffer = new StringBuilder();
            for(Supplier<? extends AlgorithmFactory> factory : algorithms.getValue().values())
            {
                buffer.append(yaml.dump(factory.get()));
                buffer.append("-------");
                buffer.append("\n");
            }
            FileWriter writer = new FileWriter(file.toFile());
            writer.write(buffer.toString());
            writer.close();



        }

    }


}
