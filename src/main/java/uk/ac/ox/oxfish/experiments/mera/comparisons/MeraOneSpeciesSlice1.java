package uk.ac.ox.oxfish.experiments.mera.comparisons;

import com.beust.jcommander.internal.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.experiments.indonesia.limited.NoDataPolicy;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ISlopeToTACControllerFactory;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.LBSPREffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.LastCatchToTACController;
import uk.ac.ox.oxfish.model.regs.policymakers.LoptEffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ITEControllerFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.ITargetTACFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.factory.LTargetEffortPolicyFactory;
import uk.ac.ox.oxfish.model.regs.policymakers.sensors.SurplusProductionDepletionFormulaController;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.RejectionSampling;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class MeraOneSpeciesSlice1 {

    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TEST_POLICY_MAP =
            new LinkedHashMap<>();

    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TEST_POLICY_MAP_2 =
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
                    return buildMaxDaysOutPolicy(200, true);
                }
        );
        //150 days at sea
        TEST_POLICY_MAP.put("150_days",
                fishState -> {
                    return buildMaxDaysOutPolicy(150, true);
                }
        );
        TEST_POLICY_MAP_2.put("150_days_notclosed",
                fishState -> {
                    return buildMaxDaysOutPolicy(150, false);
                }
        );

        //0 days at sea
        TEST_POLICY_MAP.put("0_days",
                fishState -> {
                    return buildMaxDaysOutPolicy(0, true);
                }
        );
    }



    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> SCHAEFER_TEST =
            new LinkedHashMap<>();

    static{

        SCHAEFER_TEST.put("schaefer_total",
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        SurplusProductionDepletionFormulaController assessment =
                                new SurplusProductionDepletionFormulaController();
                        assessment.setIndicatorColumnName("CPUE Lutjanus malabaricus spr_agent2");
                        assessment.setCatchColumnName("Lutjanus malabaricus Landings");
                        assessment.setStartingYear(0);
                        assessment.setCarryingCapacityMaximum(new FixedDoubleParameter(1.5E8));
                        assessment.setCarryingCapacityMinimum(new FixedDoubleParameter(0.5E7));
                        return assessment.apply(fishState);

                    }
                }
        );


        SCHAEFER_TEST.put("schaefer_totalcatches",
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        SurplusProductionDepletionFormulaController assessment =
                                new SurplusProductionDepletionFormulaController();
                        assessment.setIndicatorColumnName("CPUE Lutjanus malabaricus spr_agent2");
                        assessment.setCatchColumnName("Lutjanus malabaricus Catches (kg)");
                        assessment.setStartingYear(0);
                        assessment.setCarryingCapacityMaximum(new FixedDoubleParameter(1.5E8));
                        assessment.setCarryingCapacityMinimum(new FixedDoubleParameter(0.5E7));
                        return assessment.apply(fishState);

                    }
                }
        );

    }

    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> ITE =
            new LinkedHashMap<>();

    static{
        Iterable<String> actuators =
                Lists.newArrayList("season","fleet");
        String[] indicators = new String[]{
                "CPUE Lutjanus malabaricus spr_agent2",
                "SPR Lutjanus malabaricus spr_agent2"
        };
        double[] multipliers = new double[]{1.25,1.5};
        for(final String indicator : indicators){
            for (final String actuator : actuators) {
                for(double multiplier : multipliers){

                    ITE.put("ITE10_"+actuator+"_"+multiplier+"_"+indicator,
                            new AlgorithmFactory<AdditionalStartable>() {
                                @Override
                                public AdditionalStartable apply(FishState fishState) {
                                    ITEControllerFactory iteControllerFactory =
                                            new ITEControllerFactory();
                                    iteControllerFactory.setMultiplier(new FixedDoubleParameter(multiplier));
                                    iteControllerFactory.setIndicatorColumnName(indicator);
                                    iteControllerFactory.setMaxChangePerYear(new FixedDoubleParameter(.1));
                                    iteControllerFactory.setEffortDefinition(actuator);
                                    iteControllerFactory.setBlockEntryWhenSeasonIsNotFull(true);
                                    iteControllerFactory.setYearsBeforeStarting(0);
                                    return iteControllerFactory.apply(fishState);

                                }
                            });



                }

            }
        }
    }

    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> ITEWRONG =
            new LinkedHashMap<>();
    static {
        Iterable<String> actuators =
                Lists.newArrayList("season", "fleet");
        double[] mkRatio = new double[]{0.6,0.85,1,1.5};
        double[] multipliers = new double[]{2, 1.5,2.5};
        for (final double mk : mkRatio) {
            for (final String actuator : actuators) {
                for (double multiplier : multipliers) {

                    ITEWRONG.put("itewrong_"+mk+"_"+multiplier,
                            buildWrongITESPRPolicy(actuator,
                                    multiplier,
                                    mk)
                    );
                }

            }
        }
    }

    public static AlgorithmFactory<AdditionalStartable> buildWrongITESPRPolicy(final String effortType,
                                                                                final double multiplier,
                                                                                double mkRatio){
        final AlgorithmFactory<? extends AdditionalStartable> sprStarter = buildAdditionalSPRAgent(mkRatio);
        return new AlgorithmFactory<AdditionalStartable>() {
            @Override
            public AdditionalStartable apply(FishState fishState) {

                return new AdditionalStartable() {
                    @Override
                    public void start(FishState model) {
                        model.registerStartable(
                                (sprStarter.apply(fishState)));

                        ITEControllerFactory iteControllerFactory =
                                new ITEControllerFactory();
                        iteControllerFactory.setYearsToLookBackForTarget(new FixedDoubleParameter(1));
                        iteControllerFactory.setMultiplier(new FixedDoubleParameter(multiplier));
                        iteControllerFactory.setIndicatorColumnName("SPR Lutjanus malabaricus spr_agent_forpolicy");
                        iteControllerFactory.setMaxChangePerYear(new FixedDoubleParameter(.1));
                        iteControllerFactory.setEffortDefinition(effortType);
                        iteControllerFactory.setBlockEntryWhenSeasonIsNotFull(true);
                        iteControllerFactory.setYearsBeforeStarting(1);
                        iteControllerFactory.apply(fishState).start(model);


                    }
                };



            }
        };


    }

    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> EFFORT_ADAPTIVE =
            new LinkedHashMap<>();

    public static final LinkedHashMap<String,AlgorithmFactory<? extends AdditionalStartable>> EFFORT_ADAPTIVE_ADDITIONAL =
            new LinkedHashMap<>();

    public static final LinkedHashMap<String,AlgorithmFactory<? extends AdditionalStartable>> LTARGETE =
            new LinkedHashMap<>();


    //1.05;1.15;1.30
    static{
        Iterable<String> actuators = LBSPREffortPolicyFactory.EFFORT_ACTUATORS.keySet();
        for (final String actuator : actuators) {
            LTARGETE.put("LTARGETE_"+ 1 +"_"+actuator,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            return new AdditionalStartable() {
                                @Override
                                public void start(FishState model) {
                                    LTargetEffortPolicyFactory regulation =
                                            new LTargetEffortPolicyFactory();
                                    regulation.setEffortDefinition(actuator);
                                    regulation.setBlockEntryWhenSeasonIsNotFull(true);
                                    regulation.setMeanLengthColumnName("Mean Length Caught Lutjanus malabaricus spr_agent2");
                                    regulation.setStartingYear(0);
                                    regulation.setProportionAverageToTarget(
                                            new FixedDoubleParameter(1.05));
                                    regulation.setPolicyPeriodInYears(new FixedDoubleParameter(5));
                                    model.registerStartable(regulation.apply(model));

                                }
                            };
                        }
                    }
            );
            LTARGETE.put("LTARGETE_"+ 4 +"_"+actuator,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            return new AdditionalStartable() {
                                @Override
                                public void start(FishState model) {
                                    LTargetEffortPolicyFactory regulation =
                                            new LTargetEffortPolicyFactory();
                                    regulation.setEffortDefinition(actuator);
                                    regulation.setBlockEntryWhenSeasonIsNotFull(true);
                                    regulation.setMeanLengthColumnName("Mean Length Caught Lutjanus malabaricus spr_agent2");
                                    regulation.setStartingYear(0);
                                    regulation.setProportionAverageToTarget(
                                            new FixedDoubleParameter(1.15));
                                    regulation.setPolicyPeriodInYears(
                                            new FixedDoubleParameter(5));
                                    model.registerStartable(regulation.apply(model));

                                }
                            };
                        }
                    }
            );
            LTARGETE.put("LTARGETE_"+ 8 +"_"+actuator,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            return new AdditionalStartable() {
                                @Override
                                public void start(FishState model) {
                                    LTargetEffortPolicyFactory regulation =
                                            new LTargetEffortPolicyFactory();
                                    regulation.setEffortDefinition(actuator);
                                    regulation.setBlockEntryWhenSeasonIsNotFull(true);
                                    regulation.setMeanLengthColumnName("Mean Length Caught Lutjanus malabaricus spr_agent2");
                                    regulation.setStartingYear(0);
                                    regulation.setProportionAverageToTarget(
                                            new FixedDoubleParameter(1.30));
                                    regulation.setPolicyPeriodInYears(
                                            new FixedDoubleParameter(5));
                                    model.registerStartable(regulation.apply(model));

                                }
                            };
                        }
                    }
            );
        }
    }

    static {
        Iterable<String> actuators = LBSPREffortPolicyFactory.EFFORT_ACTUATORS.keySet();
        boolean[] aggressiveType = new boolean[]{false};
        for (final String actuator : actuators) {

//            //policies that look at LBSPR
            for (boolean aggressive : aggressiveType) {


                //standard SPR
                String name = "LBSPR_"+actuator;
                if(aggressive)
                    name = name + "_aggressive";

                String modifiedName = name + "_1.5mk";
                EFFORT_ADAPTIVE.put(modifiedName,
                        buildWrongMKLBSPRPolicy(actuator,
                                aggressive,1.5));

                modifiedName = name + "_1mk";
                EFFORT_ADAPTIVE.put(modifiedName,
                        buildWrongMKLBSPRPolicy(actuator,
                                aggressive,1));

                modifiedName = name + "_0.6mk";
                EFFORT_ADAPTIVE_ADDITIONAL.put(modifiedName,
                        buildWrongMKLBSPRPolicy(actuator,
                                aggressive,0.6));

                EFFORT_ADAPTIVE.put(name,
                        buildLBSPRPolicy(actuator,
                                aggressive, "SPR Lutjanus malabaricus spr_agent2", 1));


            }

            //policies that just look at Lopt
            EFFORT_ADAPTIVE.put("LOPT_nobuffer_"+actuator,
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
                                    regulation.setBufferValue(new FixedDoubleParameter(1.0));
                                    regulation.setTargetLength(new FixedDoubleParameter(67));
                                    model.registerStartable(regulation.apply(model));

                                }
                            };
                        }
                    }
            );
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
                                    regulation.setTargetLength(new FixedDoubleParameter(67));
                                    model.registerStartable(regulation.apply(model));

                                }
                            };
                        }
                    }
            );

        }



    }



    @NotNull
    public static AlgorithmFactory<AdditionalStartable> buildLBSPRPolicy(final String effortType,
                                                                          final boolean aggresssive,
                                                                          final String sprColumnName,
                                                                          final int yearsBeforeStart) {
        return new AlgorithmFactory<AdditionalStartable>() {
            @Override
            public AdditionalStartable apply(FishState fishState) {
                return new AdditionalStartable() {
                    @Override
                    public void start(FishState model) {
                        LBSPREffortPolicyFactory regulation = new LBSPREffortPolicyFactory();
                        regulation.setEffortDefinition(effortType);
                        if(aggresssive)
                            regulation.setMaxChangeEachYear(new FixedDoubleParameter(.9));
                        regulation.setBlockEntryWhenSeasonIsNotFull(true);
                        regulation.setSprColumnName(sprColumnName);
                        regulation.setStartingYear(yearsBeforeStart);
                        model.registerStartable(regulation.apply(model));

                    }
                };
            }
        };
    }



    public static AlgorithmFactory<AdditionalStartable> buildWrongMKLBSPRPolicy(final String effortType,
                                                                                 final boolean aggresssive,
                                                                                 double mkRatio){
        final AlgorithmFactory<? extends AdditionalStartable> sprStarter = buildAdditionalSPRAgent(mkRatio);
        return new AlgorithmFactory<AdditionalStartable>() {
            @Override
            public AdditionalStartable apply(FishState fishState) {

                return new AdditionalStartable() {
                    @Override
                    public void start(FishState model) {
                        model.registerStartable(
                                (sprStarter.apply(fishState)));
                        buildLBSPRPolicy(
                                effortType,
                                aggresssive,
                                "SPR Lutjanus malabaricus spr_agent_forpolicy",
                                1
                        ).apply(fishState).start(model);
                    }
                };



            }
        };


    }

    public static AlgorithmFactory<? extends AdditionalStartable> buildAdditionalSPRAgent(double mkRatio) {
        String sprAgent =
                "SPR Fixed Sample Agent:\n" +
                        "      assumedKParameter: '" + 0.3775984 / mkRatio + "'\n" +
                        "      assumedLengthAtMaturity: '50.0'\n" +
                        "      assumedLengthBinCm: '5.0'\n" +
                        "      assumedLinf: '86.0'\n" +
                        "      assumedNaturalMortality: '0.3775984'\n" +
                        "      assumedVarA: '0.00853'\n" +
                        "      assumedVarB: '3.137'\n" +
                        "      simulatedMaxAge: '100.0'\n" +
                        "      simulatedVirginRecruits: '1000.0'\n" +
                        "      speciesName: Lutjanus malabaricus\n" +
                        "      surveyTag: spr_agent_forpolicy\n" +
                        "      removeSmallestPercentile: true\n" +
                        "      tagsToSample:\n" +
                        "        population0: 18\n" +
                        "      useTNCFormula: " + true;

        FishYAML fishYAML = new FishYAML();
        final AlgorithmFactory<? extends AdditionalStartable> sprStarter = fishYAML.loadAs(
                sprAgent,
                AlgorithmFactory.class);
        return sprStarter;
    }

    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TAC_ADAPTIVE =
            new LinkedHashMap<>();
    static{
        HashMap<String,String> nicknameOfIndexColumn = new LinkedHashMap<>();



        nicknameOfIndexColumn.put("cpue","CPUE Lutjanus malabaricus spr_agent2");
        nicknameOfIndexColumn.put("meanlength","Mean Length Caught Lutjanus malabaricus spr_agent2");

        for (Map.Entry<String, String> nickNameIndex : nicknameOfIndexColumn.entrySet()) {



            TAC_ADAPTIVE.put("lastcatch", new AlgorithmFactory<AdditionalStartable>() {
                @Override
                public AdditionalStartable apply(FishState fishState) {
                    LastCatchToTACController controller = new LastCatchToTACController();
                    controller.setCatchColumnName("Lutjanus malabaricus Landings");
                    controller.setStartingYear(0);
                    return controller.apply(fishState);

                }
            });
            TAC_ADAPTIVE.put("lastcatch_70", new AlgorithmFactory<AdditionalStartable>() {
                @Override
                public AdditionalStartable apply(FishState fishState) {
                    LastCatchToTACController controller = new LastCatchToTACController();
                    controller.setStartingYear(0);
                    controller.setCatchColumnName("Lutjanus malabaricus Landings");
                    controller.setCatchesToTargetMultiplier(new FixedDoubleParameter(.7));
                    return controller.apply(fishState);

                }
            });

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


            TAC_ADAPTIVE.put("schaefer",
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            SurplusProductionDepletionFormulaController assessment =
                                    new SurplusProductionDepletionFormulaController();
                            assessment.setIndicatorColumnName("CPUE Lutjanus malabaricus spr_agent2");
                            assessment.setCatchColumnName("Landings Lutjanus malabaricus spr_agent2");
                            assessment.setStartingYear(0);
                            assessment.setCarryingCapacityMaximum(new FixedDoubleParameter(1.5E8));
                            assessment.setCarryingCapacityMinimum(new FixedDoubleParameter(0.5E7));
                            return assessment.apply(fishState);

                        }
                    }
            );

            TAC_ADAPTIVE.put("lastcatch_50", new AlgorithmFactory<AdditionalStartable>() {
                @Override
                public AdditionalStartable apply(FishState fishState) {
                    LastCatchToTACController controller = new LastCatchToTACController();
                    controller.setStartingYear(0);
                    controller.setCatchColumnName("Lutjanus malabaricus Landings");
                    controller.setCatchesToTargetMultiplier(new FixedDoubleParameter(.5));
                    return controller.apply(fishState);

                }
            });







        }
    }

    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TAC_ADAPTIVE_ONESPECIES =
            new LinkedHashMap<>();
    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TAC_ADAPTIVE_ONESPECIES_2 =
            new LinkedHashMap<>();
    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> TAC_ADAPTIVE_ITARGET =
            new LinkedHashMap<>();
    static{
        HashMap<String,String> nicknameOfIndexColumn = new LinkedHashMap<>();



        nicknameOfIndexColumn.put("cpue","CPUE Lutjanus malabaricus spr_agent2");
        nicknameOfIndexColumn.put("meanlength","Mean Length Caught Lutjanus malabaricus spr_agent2");

        for (Map.Entry<String, String> nickNameIndex : nicknameOfIndexColumn.entrySet()) {



            TAC_ADAPTIVE_ONESPECIES.put("multi_lastcatch", new AlgorithmFactory<AdditionalStartable>() {
                @Override
                public AdditionalStartable apply(FishState fishState) {
                    LastCatchToTACController controller = new LastCatchToTACController();
                    controller.setCatchColumnName("Lutjanus malabaricus Landings");
                    controller.setStartingYear(0);
                    controller.setTargetedSpecies("Lutjanus malabaricus");
                    return controller.apply(fishState);

                }
            });
            TAC_ADAPTIVE_ONESPECIES.put("multi_lastcatch_70", new AlgorithmFactory<AdditionalStartable>() {
                @Override
                public AdditionalStartable apply(FishState fishState) {
                    LastCatchToTACController controller = new LastCatchToTACController();
                    controller.setStartingYear(0);
                    controller.setCatchColumnName("Lutjanus malabaricus Landings");
                    controller.setCatchesToTargetMultiplier(new FixedDoubleParameter(.7));
                    controller.setTargetedSpecies("Lutjanus malabaricus");

                    return controller.apply(fishState);

                }
            });

            String policyName = "multi_islope"+nickNameIndex.getKey();
            TAC_ADAPTIVE_ONESPECIES.put(policyName,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            ISlopeToTACControllerFactory islope = new ISlopeToTACControllerFactory();
                            islope.setCatchColumnName("Lutjanus malabaricus Landings");
                            islope.setIndicatorColumnName(nickNameIndex.getValue());
                            islope.setStartingYear(0);
                            islope.setTargetedSpecies("Lutjanus malabaricus");

                            return islope.apply(fishState);
                        }
                    }
            );

            policyName = "multi_islope2"+nickNameIndex.getKey();
            TAC_ADAPTIVE_ONESPECIES_2.put(policyName,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            ISlopeToTACControllerFactory islope = new ISlopeToTACControllerFactory();
                            islope.setCatchColumnName("Lutjanus malabaricus Landings");
                            islope.setIndicatorColumnName(nickNameIndex.getValue());
                            islope.setPrecautionaryScaling(new FixedDoubleParameter(0.7));
                            islope.setStartingYear(0);
                            islope.setTargetedSpecies("Lutjanus malabaricus");

                            return islope.apply(fishState);
                        }
                    }
            );

            policyName = "multi_islope3"+nickNameIndex.getKey();
            TAC_ADAPTIVE_ONESPECIES_2.put(policyName,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            ISlopeToTACControllerFactory islope = new ISlopeToTACControllerFactory();
                            islope.setCatchColumnName("Lutjanus malabaricus Landings");
                            islope.setIndicatorColumnName(nickNameIndex.getValue());
                            islope.setPrecautionaryScaling(new FixedDoubleParameter(0.6));
                            islope.setStartingYear(0);
                            islope.setTargetedSpecies("Lutjanus malabaricus");

                            return islope.apply(fishState);
                        }
                    }
            );

            policyName = "multi_islope4"+nickNameIndex.getKey();
            TAC_ADAPTIVE_ONESPECIES_2.put(policyName,
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            ISlopeToTACControllerFactory islope = new ISlopeToTACControllerFactory();
                            islope.setCatchColumnName("Lutjanus malabaricus Landings");
                            islope.setIndicatorColumnName(nickNameIndex.getValue());
                            islope.setPrecautionaryScaling(new FixedDoubleParameter(0.6));
                            islope.setGainLambdaParameter(new FixedDoubleParameter(0.2));
                            islope.setStartingYear(0);
                            islope.setTargetedSpecies("Lutjanus malabaricus");

                            return islope.apply(fishState);
                        }
                    }
            );
            for(boolean closeToo : new boolean[]{false,true}){
                policyName = "multi_itarget1" + nickNameIndex.getKey();
                if(closeToo)
                    policyName = "closed_" + policyName;
                TAC_ADAPTIVE_ITARGET.put(policyName,
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState fishState) {
                                ITargetTACFactory itarget = new ITargetTACFactory();
                                itarget.setCatchColumnName("Lutjanus malabaricus Landings");
                                itarget.setIndicatorColumnName(nickNameIndex.getValue());
                                itarget.setPrecautionaryScaling(new FixedDoubleParameter(0));
                                itarget.setIndicatorMultiplier(new FixedDoubleParameter(1.5));
                                itarget.setInterval(5);
                                itarget.setStartingYear(0);
                                itarget.setTargetedSpecies("Lutjanus malabaricus");

                                if(closeToo)
                                    for (EntryPlugin entryPlugin : fishState.getEntryPlugins()) {
                                        entryPlugin.setEntryPaused(true);
                                    }
                                return itarget.apply(fishState);
                            }
                        }
                );
                policyName = "multi_itarget1_70" + nickNameIndex.getKey();
                if(closeToo)
                    policyName = "closed_" + policyName;
                TAC_ADAPTIVE_ITARGET.put(policyName,
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState fishState) {
                                ITargetTACFactory itarget = new ITargetTACFactory();
                                itarget.setCatchColumnName("Lutjanus malabaricus Landings");
                                itarget.setIndicatorColumnName(nickNameIndex.getValue());
                                itarget.setPrecautionaryScaling(new FixedDoubleParameter(0.3));
                                itarget.setIndicatorMultiplier(new FixedDoubleParameter(1.5));
                                itarget.setInterval(5);
                                itarget.setStartingYear(0);
                                itarget.setTargetedSpecies("Lutjanus malabaricus");

                                if(closeToo)
                                    for (EntryPlugin entryPlugin : fishState.getEntryPlugins()) {
                                        entryPlugin.setEntryPaused(true);
                                    }
                                return itarget.apply(fishState);
                            }
                        }
                );
                policyName = "multi_itarget4" + nickNameIndex.getKey();
                if(closeToo)
                    policyName = "closed_" + policyName;
                TAC_ADAPTIVE_ITARGET.put(policyName,
                        new AlgorithmFactory<AdditionalStartable>() {
                            @Override
                            public AdditionalStartable apply(FishState fishState) {
                                ITargetTACFactory itarget = new ITargetTACFactory();
                                itarget.setCatchColumnName("Lutjanus malabaricus Landings");
                                itarget.setIndicatorColumnName(nickNameIndex.getValue());
                                itarget.setPrecautionaryScaling(new FixedDoubleParameter(0.3));
                                itarget.setIndicatorMultiplier(new FixedDoubleParameter(2.5));
                                itarget.setInterval(5);
                                itarget.setStartingYear(0);
                                itarget.setTargetedSpecies("Lutjanus malabaricus");

                                if(closeToo)
                                    for (EntryPlugin entryPlugin : fishState.getEntryPlugins()) {
                                        entryPlugin.setEntryPaused(true);
                                    }
                                return itarget.apply(fishState);
                            }
                        }
                );
            }


            TAC_ADAPTIVE_ONESPECIES.put("multi_lastcatch_50", new AlgorithmFactory<AdditionalStartable>() {
                @Override
                public AdditionalStartable apply(FishState fishState) {
                    LastCatchToTACController controller = new LastCatchToTACController();
                    controller.setStartingYear(0);
                    controller.setTargetedSpecies("Lutjanus malabaricus");

                    controller.setCatchColumnName("Lutjanus malabaricus Landings");
                    controller.setCatchesToTargetMultiplier(new FixedDoubleParameter(.5));
                    return controller.apply(fishState);

                }
            });







        }
    }


    public static final String[] selectedPolicies = new String[]{
            "BAU",
            "0_days",
            "150_days",

            "multi_lastcatch",
            "multi_lastcatch_70",
            "closed_multi_itarget1cpue",
            "LBSPR_season",
            "LTARGETE_1_fleet",
            "LTARGETE_1_season",
            "LTARGETE_4_fleet",//,

            //    "LTARGETE_1_daysatsea",
            "LTARGETE_4_season"//,
        //    "LOPT_season"
    };
    public static final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> SELECTED =
            new LinkedHashMap<>();

    public static  LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> ALL_OF_THEM =
            new LinkedHashMap<>();

    static {

        ALL_OF_THEM.putAll(EFFORT_ADAPTIVE);
        ALL_OF_THEM.putAll(EFFORT_ADAPTIVE_ADDITIONAL);
        ALL_OF_THEM.putAll(TEST_POLICY_MAP);
        ALL_OF_THEM.putAll(TEST_POLICY_MAP_2);
        ALL_OF_THEM.putAll(TAC_ADAPTIVE);
        ALL_OF_THEM.putAll(TAC_ADAPTIVE_ONESPECIES);
        ALL_OF_THEM.putAll(TAC_ADAPTIVE_ONESPECIES_2);
        ALL_OF_THEM.putAll(SCHAEFER_TEST);
        ALL_OF_THEM.putAll(ITE);
        ALL_OF_THEM.putAll(TAC_ADAPTIVE_ITARGET);
        ALL_OF_THEM.putAll(ITEWRONG);
        ALL_OF_THEM.putAll(LTARGETE);

        for (String policy : selectedPolicies) {
            SELECTED.put(policy,ALL_OF_THEM.get(policy));
        }
    }

    private static final Path MAIN_DIRECTORY =
            Paths.get("docs","mera_hub","slice_1");
    public static final Path DEFAULT_PATH_TO_COLUMNS_TO_PRINT = MAIN_DIRECTORY.resolve("full_columns_to_print.yaml");

    public static void main(String[] args) throws IOException {

        //java -jar oxfish_meraslice1.jar effort ./policy_new/distance_pass.csv ./policy_new/distance/effort
        //java -jar oxfish_meraslice1.jar tac ./policy_new/distance_pass.csv ./policy_new/distance/tac
        //java -jar oxfish_meraslice1.jar test ./policy_new/distance_pass.csv ./policy_new/distance/test
        if(args[0].equals("rejectionSampling"))
            rejectionSampling("parameters.yaml", MAIN_DIRECTORY.resolve("results"));
        else if(args[0].equals("rejectionSampling_lowmk"))
            rejectionSampling("parameters_lowmk.yaml",
                    MAIN_DIRECTORY.resolve("lowmk").resolve("results"));
        else {

            String typeOfPolicies = args[0];
            LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies = null;
            if (typeOfPolicies.equals("effort"))
                selectedPolicies = EFFORT_ADAPTIVE;
            if (typeOfPolicies.equals("effort_additional"))
                selectedPolicies = EFFORT_ADAPTIVE_ADDITIONAL;
            if (typeOfPolicies.equals("test"))
                selectedPolicies = TEST_POLICY_MAP;
            if (typeOfPolicies.equals("test2"))
                selectedPolicies = TEST_POLICY_MAP_2;
            if (typeOfPolicies.equals("tac"))
                selectedPolicies = TAC_ADAPTIVE;
            if (typeOfPolicies.equals("mtac"))
                selectedPolicies = TAC_ADAPTIVE_ONESPECIES;
            if (typeOfPolicies.equals("mtac2"))
                selectedPolicies = TAC_ADAPTIVE_ONESPECIES_2;
            if (typeOfPolicies.equals("schaefer"))
                selectedPolicies = SCHAEFER_TEST;
            if (typeOfPolicies.equals("ite"))
                selectedPolicies = ITE;
            if (typeOfPolicies.equals("itarget"))
                selectedPolicies = TAC_ADAPTIVE_ITARGET;
            if (typeOfPolicies.equals("wrongite"))
                selectedPolicies = ITEWRONG;
            if (typeOfPolicies.equals("ltargete"))
                selectedPolicies = LTARGETE;
            if (typeOfPolicies.equals("select"))
                selectedPolicies = SELECTED;
            if (selectedPolicies == null)
                throw new RuntimeException("failed to find the right type of policies");

            Path pathToScenarioFiles = MAIN_DIRECTORY.resolve(args[1]);
            Path pathToOutput = MAIN_DIRECTORY.resolve(args[2]);

            runSetOfScenarios(pathToScenarioFiles,
                    pathToOutput,
                    selectedPolicies, YEARS_TO_RUN_POLICIES, DEFAULT_PATH_TO_COLUMNS_TO_PRINT, null);
        }
        //the rejection sampling bit
        //      rejectionSampling("parameters.yaml", MAIN_DIRECTORY.resolve("results"));
        //rejection sampling with lowmk
//        rejectionSampling("parameters_lowmk.yaml",
//                MAIN_DIRECTORY.resolve("lowmk").resolve("results"));

//        running policy on rough scenarios
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("rough_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("rough").resolve("test"),
//                TEST_POLICY_MAP
//                );
//
//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("rough_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("rough").resolve("effort"),
//                EFFORT_ADAPTIVE
//        );

//        runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("rough_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("rough").resolve("tac"),
//                TAC_ADAPTIVE
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

//                runSetOfScenarios(MAIN_DIRECTORY.resolve("policy").resolve("catchcurve_pass.csv"),
//                MAIN_DIRECTORY.resolve("policy").resolve("catchcurve").resolve("effort"),
//                EFFORT_ADAPTIVE
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

    public static void runSetOfScenarios(Path scenarioFileList,
                                         Path outputDirectory,
                                         LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> policyMap, int yearsToRun,
                                         Path pathToColumnsToPrint, @Nullable Consumer<Scenario> commonScenarioConsumer
    ) throws IOException{
        FishYAML yaml = new FishYAML();
        final List<String> columnsToPrint = yaml.loadAs(new FileReader(
                        pathToColumnsToPrint.toFile()),
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
                    yearsToRun,
                    0,
                    outputDirectory,
                    policyMap,
                    columnsToPrint,
                    false,
                    commonScenarioConsumer,
                    null
            );



        }


    }


    @NotNull
    public static AdditionalStartable buildMaxDaysOutPolicy(int maxDaysOut, boolean blockEntry) {
        return model -> {
            //first remove all possible entries
            if(blockEntry)
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
