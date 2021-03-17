package uk.ac.ox.oxfish.experiments.mera.comparisons;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.experiments.indonesia.limited.NoDataPolicy;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.RejectionSampling;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeraOneSpeciesSlice1 {

    private static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TEST_POLICY_MAP =
            new LinkedHashMap<>();

    static {
        //business as usual: nothing happens!
        TEST_POLICY_MAP.put("BAU",
                fishState -> model -> {

                }
        );
        //200 days at sea
        TEST_POLICY_MAP.put("200_days",
                fishState -> {
                    return buildMaxDaysOutPolicy(200);
                }
        );
        //150 days at sea
        TEST_POLICY_MAP.put("150_days",
                fishState -> {
                    return buildMaxDaysOutPolicy(150);
                }
        );
    }


    private static final Path MAIN_DIRECTORY =
            Paths.get("docs","mera_hub","slice_1");

    public static void main(String[] args) throws IOException {

        //the rejection sampling bit
        //       rejectionSampling("parameters.yaml", MAIN_DIRECTORY.resolve("results"));

        //rejection sampling with lowmk
        rejectionSampling("parameters_lowmk.yaml",
                MAIN_DIRECTORY.resolve("lowmk").resolve("results"));
        //running policy on rough scenarios
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("rough_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("rough").resolve("test"),
//                TEST_POLICY_MAP
//                );

        //running policies on "best 100" (distance)
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("best_distance_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("best_distance").resolve("test"),
//                TEST_POLICY_MAP
//        );
        //running policies on "best 100" (catchcurve)
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("catchcurve_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("catchcurve").resolve("test"),
//                TEST_POLICY_MAP
//        );

//
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("catchcurve_pass.csv"),
//                MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("catchcurve").resolve("test"),
//                TEST_POLICY_MAP
//        );
    }

    private static void rejectionSampling(String parameterFile,
                                          Path outputFolder) throws IOException {
        RejectionSampling.runSlice(
                MAIN_DIRECTORY.resolve("base.yaml"),
                MAIN_DIRECTORY.resolve(parameterFile),
                MAIN_DIRECTORY.resolve("full_columns_to_print.yaml"),
                outputFolder,
                0,
                40,
                null
        );
    }

    private static void runSetOfScenarios(Path scenarioFileList,
                                          Path outputDirectory,
                                          LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> policyMap
    ) throws IOException{
        FishYAML yaml = new FishYAML();
        final List<String> columnsToPrint = yaml.loadAs(new FileReader(
                        MAIN_DIRECTORY.resolve("full_columns_to_print.yaml").toFile()),
                List.class);

        outputDirectory.toFile().mkdirs();

        final List<String> strings = Files.readAllLines(scenarioFileList);
        for(int row=1; row<strings.size(); row++){
            final String[] currentSimulation = strings.get(row).trim().split(",");
            Path scenario = Paths.get(currentSimulation[0].replaceAll("\"",""));
            Integer shockYear = Integer.parseInt(currentSimulation[1]);

            RejectionSampling.runOneAcceptedScenario(
                    scenario,
                    shockYear,
                    10,
                    0,
                    outputDirectory,
                    policyMap,
                    columnsToPrint,
                    false,
                    null,
                    null
            );



        }


    }


    @NotNull
    private static AdditionalStartable buildMaxDaysOutPolicy(int maxDaysOut) {
        return model -> {
            //first remove all possible entries
            NoDataPolicy.REMOVE_ENTRY_EVENT.step(model);
            //create a factory, feed it to the fisher factories just in case you turn entry back on
            MaxHoursOutFactory factory = new MaxHoursOutFactory();
            factory.setMaxHoursOut(new FixedDoubleParameter(maxDaysOut*24));
            for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                fisherFactory.getValue().setRegulations(factory);
            }
            //also change the rule to all existing agents!
            for (Fisher fisher : model.getFishers()) {
                fisher.setRegulation(factory.apply(model));
            }
        };
    }

}
