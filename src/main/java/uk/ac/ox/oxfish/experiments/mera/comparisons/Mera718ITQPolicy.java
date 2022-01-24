package uk.ac.ox.oxfish.experiments.mera.comparisons;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.boxcars.AbundanceGathererBuilder;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.MultiQuotaITQRegulation;
import uk.ac.ox.oxfish.model.regs.OffSwitchDecorator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.*;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Mera718ITQPolicy {
    public static final Path COLUMNS_TO_PRINT = MeraOMHotstartsCalibration.MAIN_DIRECTORY.resolve("full_columns_to_print.yaml");
    static private LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
            new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {

        selectedPolicies.put("itq_5000",
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        return new AdditionalStartable() {
                            @Override
                            public void start(FishState model) {
                                //this quota regulation should be automatically rescaling to # of active fishers!
                                MultiQuotaMapFactory newReg = new MultiQuotaMapFactory();
                                newReg.setQuotaType(MultiQuotaMapFactory.QuotaType.ITQ);
                                newReg.getInitialQuotas().clear();
                                newReg.getInitialQuotas().put("Lutjanus malabaricus",5000d*1000d);
                                newReg.getQuotaExchangedPerMatch().clear();
                                newReg.getQuotaExchangedPerMatch().put("Lutjanus malabaricus",1000d);
                                newReg.setMultipleTradesAllowed(false);

                                for (Fisher fisher : fishState.getFishers()) {
                                    assert fisher.getRegulation() instanceof OffSwitchDecorator;

                                    fisher.setRegulation(newReg.apply(fishState));
                                }
                                fishState.getFisherFactory("population0").setRegulations(new FishingSeasonFactory(0,true));
                            }
                        };


                    }
                });
//
//
//
        selectedPolicies.put("tac_5000",
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        return new AdditionalStartable() {
                            @Override
                            public void start(FishState model) {

                                //this quota regulation should be automatically rescaling to # of active fishers!
                                MultiTACStringFactory newReg = new MultiTACStringFactory();
                                newReg.setYearlyQuotaMaps("0:5000000");
//                                newReg.getQuotaExchangedPerMatch().clear();
//                                newReg.getQuotaExchangedPerMatch().put("Lutjanus malabaricus",1000d);
//                                newReg.setMultipleTradesAllowed(false);

                                for (Fisher fisher : fishState.getFishers()) {
                                    assert fisher.getRegulation() instanceof OffSwitchDecorator;

                                    fisher.setRegulation(newReg.apply(fishState));
                                }
                                fishState.getFisherFactory("population0").setRegulations(new FishingSeasonFactory(0,true));
                            }
                        };


                    }
                });

//        final AlgorithmFactory<? extends AdditionalStartable> factory = MeraOneSpeciesSlice1.ALL_OF_THEM.get("multi_lastcatch_qb");
//        Preconditions.checkArgument(factory!=null);
//        selectedPolicies.put(
//                "multi_lastcatch_qb_monitored",
//                factory
//        );
//
//        selectedPolicies.put("itq_lastcatch_70",
//                new AlgorithmFactory<AdditionalStartable>() {
//                    @Override
//                    public AdditionalStartable apply(FishState fishState) {
//                        return new AdditionalStartable() {
//                            @Override
//                            public void start(FishState model) {
//                                //this quota regulation should be automatically rescaling to # of active fishers!
//                                double lastLandings = fishState.getLatestYearlyObservation("Lutjanus malabaricus Landings")*.7;
//                                System.out.println("last landings: " + lastLandings);
//                                MultiQuotaMapFactory newReg = new MultiQuotaMapFactory();
//                                newReg.setQuotaType(MultiQuotaMapFactory.QuotaType.ITQ);
//                                newReg.getInitialQuotas().clear();
//                                newReg.getInitialQuotas().put("Lutjanus malabaricus",lastLandings*1.2);
//                                newReg.getQuotaExchangedPerMatch().clear();
//                                newReg.getQuotaExchangedPerMatch().put("Lutjanus malabaricus",1000d);
//                                newReg.setMultipleTradesAllowed(false);
//
//                                for (Fisher fisher : fishState.getFishers()) {
//                                    assert fisher.getRegulation() instanceof OffSwitchDecorator;
//
//                                    fisher.setRegulation(newReg.apply(fishState));
//                                }
//                                fishState.getFisherFactory("population0").setRegulations(new FishingSeasonFactory(0,true));
//                            }
//                        };
//
//
//                    }
//                });

      //  Path mainDirectory = Paths.get("docs","mera_hub","slice5_yesgeography_twospecies").resolve("results");
        Path mainDirectory = Paths.get("docs","mera_hub","slice2_nogeography_onespecies").resolve("results");


        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list.csv");
        Path pathToOutput = mainDirectory.resolve("policy_itq");
        LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies = new LinkedHashMap<>();

        for (Map.Entry<String, AlgorithmFactory<? extends AdditionalStartable>> policyFactory : selectedPolicies.entrySet()) {
            adjustedPolicies.put(
                    policyFactory.getKey(),
                    new AlgorithmFactory<AdditionalStartable>() {
                        @Override
                        public AdditionalStartable apply(FishState fishState) {
                            //2. pull up delegate regulation for active agents (keep inactive agents off)
                            for (Fisher fisher : fishState.getFishers()) {
                                assert fisher.getRegulation() instanceof OffSwitchDecorator;
                                fisher.setRegulation(new Anarchy());
                            }
                            //need to change the factory too...
                            final AlgorithmFactory<? extends Regulation> newReg =
                                    new AnarchyFactory();
                            fishState.getFisherFactory("population0").setRegulations(newReg);

                            AbundanceGathererBuilder builder = new AbundanceGathererBuilder();
                            builder.setObservationDay(364);
                            fishState.registerStartable(builder.apply(fishState));

                            return policyFactory.getValue().apply(fishState);


                        }
                    }
            );
        }


        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 50, COLUMNS_TO_PRINT,
                new Consumer<Scenario>() {
                    @Override
                    public void accept(Scenario scenario) {
                        for (FisherDefinition fisherDefinition : ((FlexibleScenario) scenario).getFisherDefinitions()) {
                            fisherDefinition.setUsePredictors(true);
                            fisherDefinition.setRegulation(new AnarchyFactory());
                        }
                    }
                });



    }
}
