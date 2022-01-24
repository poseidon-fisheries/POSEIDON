package uk.ac.ox.oxfish.experiments.mera.comparisons;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class MeraFakeOMHotstartsPolicy {


    public static final Path COLUMNS_TO_PRINT = MeraOMHotstartsCalibration.MAIN_DIRECTORY.resolve("full_columns_to_print.yaml");
    static private LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
            new LinkedHashMap<>();
    static {
        selectedPolicies.put(
                "250_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(250, true);
                }
        );
        selectedPolicies.put(
                "333_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(333, true);
                }
        );
        selectedPolicies.put(
                "0_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(0, true);
                }
        );
        String[] otherPolicies = {
                "BAU",
                "lastcatch",
                "lastcatch_70",
                "closed_multi_itarget1cpue",
                "LBSPR_season",
                "LTARGETE_1_fleet",
                "LTARGETE_1_season",
                "LTARGETE_4_fleet",
                "LTARGETE_4_season"};

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


        Path mainDirectory = MeraOMHotstartsCalibration.MAIN_DIRECTORY.resolve("results");


        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list.csv");
        Path pathToOutput = mainDirectory.resolve("policy");


        //what we do is that we intercept policies from the original slice 1 and before we let them start we also apply
        //our prepareScenarioForPolicy consumer ahead of time

        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies = new LinkedHashMap<>();
        for (Map.Entry<String, AlgorithmFactory<? extends AdditionalStartable>> policyFactory : selectedPolicies.entrySet()) {
            adjustedPolicies.put(
                    policyFactory.getKey(),
                    fishState -> {
                        MeraOneSpeciesSlice1Negative.prepareHotstartScenarioForPolicy(true).accept(fishState);
                        return policyFactory.getValue().apply(fishState);


                    }
            );
        }
        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 50, COLUMNS_TO_PRINT, null);


    }



}
