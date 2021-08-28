package uk.ac.ox.oxfish.experiments.mera.comparisons;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.OffSwitchDecorator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.OffSwitchFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Mera718Slice1Policy {


    static private LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
            new LinkedHashMap<>();
    static {
        /*
        selectedPolicies.put(
                "currentEffort",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(250, true);
                }
        );
        selectedPolicies.put(
                "effort75",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(187, true);
                }
        );
        selectedPolicies.put(
                "0_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(0, true);
                }
        );
         */
        String[] otherPolicies = {
          //      "BAU",
                "multi_lastcatch_qb",
                "multi_lastcatch_70_qb",
                "multi_lastcatch_50_qb" /*,
//                "lastcatch",
//                "lastcatch_70",

                "closed_multi_itarget1cpue",
                "LBSPR_season",
                "LTARGETE_1_fleet",
                "LTARGETE_1_season",
                "LTARGETE_4_fleet",
                "LTARGETE_4_season",
                "YEARLY_LTARGETE_1_season",
                "YEARLY_LTARGETE_4_season", */

        };

        for(String policy : otherPolicies){
            final AlgorithmFactory<? extends AdditionalStartable> factory = MeraOneSpeciesSlice1.ALL_OF_THEM.get(policy);
            Preconditions.checkArgument(factory!=null,policy);
            selectedPolicies.put(
                    policy,
                    factory
            );
        }


    }


    public static void main(String[] args) throws IOException {


        //  generateScenarioFiles();


        Path mainDirectory = Paths.get("docs","mera_hub","slice2_nogeography_onespecies").resolve("results");


        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list.csv");
        Path pathToOutput = mainDirectory.resolve("policy_qb");


        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies = new LinkedHashMap<>();
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

                            return policyFactory.getValue().apply(fishState);


                        }
                    }
            );
        }
        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 50, MeraOneSpeciesSlice1.DEFAULT_PATH_TO_COLUMNS_TO_PRINT, null);



    }


}
