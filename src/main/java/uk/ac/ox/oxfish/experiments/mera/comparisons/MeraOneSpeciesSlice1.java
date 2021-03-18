package uk.ac.ox.oxfish.experiments.mera.comparisons;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.experiments.indonesia.limited.NoDataPolicy;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.ISlopeToTACControllerFactory;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.LBSPREffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.LastCatchToTACController;
import uk.ac.ox.oxfish.model.regs.policymakers.LoptEffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.SurplusProductionStockAssessment;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SurplusProductionDepletionFormulaController;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeraOneSpeciesSlice1 {

    private static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TEST_POLICY_MAP =
            new LinkedHashMap<>();
    public static final int YEARS_TO_RUN_POLICIES = 40;

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

    private static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> EFFORT_ADAPTIVE =
            new LinkedHashMap<>();
    static {
        Iterable<String> actuators = LBSPREffortPolicyFactory.EFFORT_ACTUATORS.keySet();
        boolean[] aggressiveType = new boolean[]{true,false};
        for (final String actuator : actuators) {

            //policies that look at LBSPR
            for (boolean aggressive : aggressiveType) {
                String name = "LBSPR_"+actuator;
                if(aggressive)
                    name = name + "_aggressive";
                EFFORT_ADAPTIVE.put(name,
                        buildLBSPRPolicy(actuator,
                                aggressive));
            }

            //policies that just look at Lopt
            EFFORT_ADAPTIVE.put("LOPT_"+actuator,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            return new AdditionalStartable() {
                                @Override
                                public void start(FishState model) {
                                    LoptEffortPolicyFactory regulation = new LoptEffortPolicyFactory();
                                    regulation.setEffortDefinition(actuator);
                                    regulation.setBlockEntryWhenSeasonIsNotFull(true);
                                    regulation.setMeanLengthColumnName("Mean Length Caught Lutjanus malabaricus spr_agent2");
                                    regulation.setStartingYear(0);
                                    model.registerStartable(regulation.apply(model));

                                }
                            };
                        }
                    }
            );

        }



    }
    @NotNull
    private static AlgorithmFactory<AdditionalStartable> buildLBSPRPolicy(final String effortType,
                                                                          final boolean aggresssive) {
        return new AlgorithmFactory<AdditionalStartable>() {
            @Override
            public AdditionalStartable apply(FishState fishState) {
                return new AdditionalStartable() {
                    @Override
                    public void start(FishState model) {
                        LBSPREffortPolicyFactory regulation = new LBSPREffortPolicyFactory();
                        regulation.setEffortDefinition(effortType);
                        if(aggresssive)
                            regulation.setMaxChangeEachYear(new FixedDoubleParameter(.2));
                        regulation.setBlockEntryWhenSeasonIsNotFull(true);
                        regulation.setSprColumnName("SPR Lutjanus malabaricus spr_agent2");
                        regulation.setStartingYear(0);
                        model.registerStartable(regulation.apply(model));

                    }
                };
            }
        };
    }

    private static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TAC_ADAPTIVE =
            new LinkedHashMap<>();
    static{
        HashMap<String,String> nicknameOfIndexColumn = new LinkedHashMap<>();
        nicknameOfIndexColumn.put("cpue","CPUE Lutjanus malabaricus spr_agent2");
        nicknameOfIndexColumn.put("meanlength","Mean Length Caught Lutjanus malabaricus spr_agent2");
        nicknameOfIndexColumn.put("accuratecpue","CPUE Lutjanus malabaricus");
        nicknameOfIndexColumn.put("accuratecpho","CPHO Lutjanus malabaricus");

        TAC_ADAPTIVE.put("schaefer",
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        SurplusProductionDepletionFormulaController assessment =
                                new SurplusProductionDepletionFormulaController();
                        assessment.setIndicatorColumnName("CPUE Lutjanus malabaricus spr_agent2");
                        assessment.setCatchColumnName("Landings Lutjanus malabaricus spr_agent2");
                        assessment.setStartingYear(0);
                        return assessment.apply(fishState);

                    }
                }
        );

        TAC_ADAPTIVE.put("lastcatch", new AlgorithmFactory<AdditionalStartable>() {
            @Override
            public AdditionalStartable apply(FishState fishState) {
                LastCatchToTACController controller = new LastCatchToTACController();
                controller.setStartingYear(0);
                return controller.apply(fishState);

            }
        });
        TAC_ADAPTIVE.put("lastcatch_70", new AlgorithmFactory<AdditionalStartable>() {
            @Override
            public AdditionalStartable apply(FishState fishState) {
                LastCatchToTACController controller = new LastCatchToTACController();
                controller.setStartingYear(0);
                controller.setCatchesToTargetMultiplier(new FixedDoubleParameter(.7));
                return controller.apply(fishState);

            }
        });

        for (Map.Entry<String, String> nickNameIndex : nicknameOfIndexColumn.entrySet()) {

            String policyName = "islope"+nickNameIndex.getKey();
            TAC_ADAPTIVE.put(policyName,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            ISlopeToTACControllerFactory islope = new ISlopeToTACControllerFactory();
                            islope.setCatchColumnName("Lutjanus malabaricus Landings");
                            islope.setIndicatorColumnName(nickNameIndex.getValue());
                            islope.setStartingYear(0);
                            return islope.apply(fishState);
                        }
                    }
            );






        }
    }



    private static final Path MAIN_DIRECTORY =
            Paths.get("docs","mera_hub","slice_1");

    public static void main(String[] args) throws IOException {

        //the rejection sampling bit
        //      rejectionSampling("parameters.yaml", MAIN_DIRECTORY.resolve("results"));
        //rejection sampling with lowmk
//        rejectionSampling("parameters_lowmk.yaml",
//                MAIN_DIRECTORY.resolve("lowmk").resolve("results"));

        //running policy on rough scenarios
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("rough_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("rough").resolve("test"),
//                TEST_POLICY_MAP
//                );
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("rough_pass.csv"),
//                MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("rough").resolve("test"),
//                TEST_POLICY_MAP
//                );
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("rough_pass.csv"),
//                MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("rough").resolve("effort"),
//                EFFORT_ADAPTIVE
//        );

        //running policies on "best 100" (distance)
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("best_distance_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("best_distance").resolve("test"),
//                TEST_POLICY_MAP
//        );


//        runSetOfScenarios(MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("best_distance_pass.csv"),
//                MAIN_DIRECTORY.resolve("lowmk").resolve("policy").resolve("best_distance").resolve("test"),
//                TEST_POLICY_MAP
//        );
        //running policies on "best 100" (catchcurve)
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("catchcurve_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("catchcurve").resolve("test"),
//                TEST_POLICY_MAP
//        );

                runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("catchcurve_pass.csv"),
                MAIN_DIRECTORY.resolve("policy").resolve("catchcurve").resolve("effort"),
                EFFORT_ADAPTIVE
        );


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
                    YEARS_TO_RUN_POLICIES,
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
