package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.initializer.DerisoParameters;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FixedRestTimeDepartingFactory;
import uk.ac.ox.oxfish.geography.sampling.SampledMap;
import uk.ac.ox.oxfish.model.regs.factory.ITQMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PolicyScript;
import uk.ac.ox.oxfish.model.scenario.PolicyScripts;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.Scenarios;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

/**
 * Dump every scenario and every algorithm factory as an example
 * Created by carrknight on 7/13/15.
 */
public class  BuildSampleInputs
{


    public static void main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();

        Path directory = Paths.get("inputs", "YAML Samples","scenario");
        directory.toFile().mkdirs();
        //start with each Scenario; populate them with the standard variables
        for(Supplier<Scenario> scenario : Scenarios.SCENARIOS.values())
        {
            String name = Scenarios.SCENARIOS.inverse().get(scenario);
            final Path file = directory.resolve(name+ ".yaml");
            final String dump = yaml.dump(scenario.get());
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

        //write a sample policy script
        { //put it in a bracket for no reason at all except to recycle the variable names later
            PolicyScripts scripts = new PolicyScripts();
            scripts.getScripts().put(1, mockYear1Script);
            scripts.getScripts().put(10, mockYear2Script);
            final Path file = directory.resolve("sample policy script.yaml");
            final String dump = yaml.dump(scripts);
            FileWriter writer = new FileWriter(file.toFile());
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


        directory = Paths.get("inputs", "california");
        Path bioDirectory = directory.resolve("biology");

        DirectoryStream<Path> folders = Files.newDirectoryStream(bioDirectory);
        LinkedHashMap<String,Path> spatialFiles = new LinkedHashMap<>();
        LinkedHashMap<String, Path> folderMap = new LinkedHashMap<>();
        //sort it alphabetically to insure folders are consistently ranked
        List<Path> sortedFolders = new LinkedList<>();
        folders.forEach(path -> sortedFolders.add(path));
        Collections.sort(sortedFolders, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getFileName().toString(),
                                                                                          o2.getFileName().toString()));

        //each folder is supposedly a species
        for(Path folder : sortedFolders)
        {

            Path file = folder.resolve("spatial.csv");
            if(file.toFile().exists())
            {
                String name = folder.getFileName().toString();
                spatialFiles.put(name, file);
                Preconditions.checkArgument(folder.resolve("count.csv").toFile().exists(),
                                            "The folder "+ name +
                                                    "  doesn't contain the abundance count.csv");

                Preconditions.checkArgument(folder.resolve("meristics.yaml").toFile().exists(),
                                            "The folder "+ name +
                                                    "  doesn't contain the abundance count.csv");

                folderMap.put(folder.getFileName().toString(),folder);
            }
            else
            {
                if(Log.WARN)
                    Log.warn(folder.getFileName() + " does not have a spatial.txt file and so cannot be distributed on the map. It will be ignored");
            }

        }

        SampledMap sampledMap = new SampledMap(Paths.get("inputs", "california",
                                                         "california.csv"),
                                               50,
                                               spatialFiles);

        ObjectOutputStream objectStream = new ObjectOutputStream(new FileOutputStream(directory.resolve("premade.data").toFile()));
        objectStream.writeObject(sampledMap);
        objectStream.close();


        DerisoParameters.main(null);
    }



    private final static PolicyScript mockYear1Script = new PolicyScript();
    private final static PolicyScript mockYear2Script = new PolicyScript();
    static {
        mockYear1Script.setChangeInNumberOfFishers(100);
        mockYear1Script.setGear(new FixedProportionGearFactory());
        mockYear2Script.setRegulation(new ITQMonoFactory());
        mockYear2Script.setDepartingStrategy(new FixedRestTimeDepartingFactory());
    }
}
