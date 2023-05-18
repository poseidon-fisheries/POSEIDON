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
import java.util.logging.Logger;

/**
 * Dump every scenario and every algorithm factory as an example
 * Created by carrknight on 7/13/15.
 */
public class BuildSampleInputs {


    private final static PolicyScript mockYear1Script = new PolicyScript();
    private final static PolicyScript mockYear2Script = new PolicyScript();

    static {
        mockYear1Script.setChangeInNumberOfFishers(100);
        mockYear1Script.setGear(new FixedProportionGearFactory());
        mockYear2Script.setRegulation(new ITQMonoFactory());
        mockYear2Script.setDepartingStrategy(new FixedRestTimeDepartingFactory());
    }

    public static void main(final String[] args) throws IOException {

        final FishYAML yaml = new FishYAML();

        Path directory = Paths.get("inputs", "YAML Samples", "scenario");
        directory.toFile().mkdirs();
        //start with each Scenario; populate them with the standard variables
        for (final Supplier<Scenario> scenario : Scenarios.SCENARIOS.values()) {
            System.out.println(scenario.get().toString());
            final String name = Scenarios.SCENARIOS.inverse().get(scenario);
            final Path file = directory.resolve(name + ".yaml");
            final String dump = yaml.dump(scenario.get());
            final FileWriter writer = new FileWriter(file.toFile());
            if (Objects.equals(name, "Prototype")) {
                final File base = Dashboard.DASHBOARD_INPUT_DIRECTORY.resolve("base.yaml").toFile();

                final FileWriter auxiliary = new FileWriter(base);
                auxiliary.write(dump);
                auxiliary.close();
            }
            writer.write(dump);
            writer.close();
        }

        //write a sample policy script
        { //put it in a bracket for no reason at all except to recycle the variable names later
            final PolicyScripts scripts = new PolicyScripts();
            scripts.getScripts().put(1, mockYear1Script);
            scripts.getScripts().put(10, mockYear2Script);
            final Path file = directory.resolve("sample policy script.yaml");
            final String dump = yaml.dump(scripts);
            final FileWriter writer = new FileWriter(file.toFile());
            writer.write(dump);
            writer.close();

        }


        directory = Paths.get("inputs", "YAML Samples", "components");
        directory.toFile().mkdirs();
        //the same for all
        for (final Map.Entry<Class, Map<String, ? extends Supplier<? extends AlgorithmFactory<?>>>> algorithms : AlgorithmFactories.CONSTRUCTOR_MAP.entrySet()) {
            final String name = algorithms.getKey().getSimpleName();
            final Path file = directory.resolve(name + ".yaml");
            final StringBuilder buffer = new StringBuilder();
            for (final Supplier<? extends AlgorithmFactory> factory : algorithms.getValue().values()) {
                buffer.append(yaml.dump(factory.get()));
                buffer.append("-------");
                buffer.append("\n");
            }
            final FileWriter writer = new FileWriter(file.toFile());
            writer.write(buffer.toString());
            writer.close();


        }


        directory = Paths.get("inputs", "california");
        final Path bioDirectory = directory.resolve("biology");

        final DirectoryStream<Path> folders = Files.newDirectoryStream(bioDirectory);
        final LinkedHashMap<String, Path> spatialFiles = new LinkedHashMap<>();
        final LinkedHashMap<String, Path> folderMap = new LinkedHashMap<>();
        //sort it alphabetically to insure folders are consistently ranked
        final List<Path> sortedFolders = new LinkedList<>();
        folders.forEach(path -> sortedFolders.add(path));
        Collections.sort(sortedFolders, (o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(
            o1.getFileName().toString(),
            o2.getFileName().toString()
        ));

        //each folder is supposedly a species
        for (final Path folder : sortedFolders) {

            final Path file = folder.resolve("spatial.csv");
            if (file.toFile().exists()) {
                final String name = folder.getFileName().toString();
                spatialFiles.put(name, file);
                Preconditions.checkArgument(
                    folder.resolve("count.csv").toFile().exists(),
                    "The folder " + name +
                        "  doesn't contain the abundance count.csv"
                );

                Preconditions.checkArgument(
                    folder.resolve("meristics.yaml").toFile().exists(),
                    "The folder " + name +
                        "  doesn't contain the abundance count.csv"
                );

                folderMap.put(folder.getFileName().toString(), folder);
            } else {
                Logger.getGlobal().warning(
                    folder.getFileName() + " does not have a spatial.txt file and so cannot be distributed on the map. It will be ignored"
                );
            }

        }

        final SampledMap sampledMap = new SampledMap(
            Paths.get("inputs", "california",
                "california.csv"
            ),
            50,
            spatialFiles
        );

        final ObjectOutputStream objectStream = new ObjectOutputStream(new FileOutputStream(directory.resolve(
                "premade.data")
            .toFile()));
        objectStream.writeObject(sampledMap);
        objectStream.close();


        DerisoParameters.main(null);
    }
}
