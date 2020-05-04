package uk.ac.ox.oxfish.experiments.indonesia.limited;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.experiments.noisespike.AcceptableRangePredicate;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class NoData718Slice5 {


    final static public Path MAIN_DIRECTORY =
            Paths.get("docs", "indonesia_hub",
                    "runs", "718",
                    "slice5limited");

    final static public Path BASELINE_SCENARIO_FILE = MAIN_DIRECTORY.resolve("base.yaml");

    final static public int MAX_YEARS_TO_RUN = 40;


    public static void main2(String[] args){

        FishYAML yaml = new FishYAML();



    }


    public static void  main(String[] args) throws IOException {

        FishYAML yaml = new FishYAML();
        final List<AcceptableRangePredicate> predicates =
                yaml.loadAs(new FileReader(
                                MAIN_DIRECTORY.resolve("predicates.yaml").toFile()
                        ),
                        LinkedList.class);

        final List<OptimizationParameter> parameters =
                yaml.loadAs(new FileReader(
                                MAIN_DIRECTORY.resolve("parameters.yaml").toFile()
                        ),
                        LinkedList.class);


        String computerName = FishStateUtilities.getComputerName();
        MersenneTwisterFast random = new MersenneTwisterFast();
        int directoryIndex =  random.nextInt(999999);
        Path scenarioDirectory = MAIN_DIRECTORY.resolve("scenarios").resolve(computerName+"_"+directoryIndex);
        scenarioDirectory.toFile().mkdirs();
        Path summaryDirectory = scenarioDirectory.resolve("summaries");
        summaryDirectory.toFile().mkdir();


        final FileWriter summaryStatisticsWriter =
                NoData718Slice1.prepareSummaryStatisticsMasterFile(summaryDirectory,
                        0l,
                        predicates);

        System.out.println("working in directory: " + scenarioDirectory);

        FileWriter parameterMasterFile = NoData718Slice3.initializeParameterMasterFile(summaryDirectory, parameters);


        for(int i=0; i<50000; i++) {
            final Path writtenScenario = NoData718Slice3.writeToFileOneScenario(scenarioDirectory,
                    parameters,
                    BASELINE_SCENARIO_FILE,
                    parameterMasterFile,
                    new MersenneTwisterFast(),
                    i
            );
            NoData718Slice1.runOneScenario(
                    0l,
                    predicates,
                    summaryStatisticsWriter,
                    writtenScenario.toFile(),
                    MAX_YEARS_TO_RUN
            );
        }

    }

}


