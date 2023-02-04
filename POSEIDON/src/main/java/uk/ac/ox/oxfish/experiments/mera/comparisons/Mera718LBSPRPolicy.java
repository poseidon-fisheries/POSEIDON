package uk.ac.ox.oxfish.experiments.mera.comparisons;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.boxcars.AbundanceGathererBuilder;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilderFixedSample;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.OffSwitchDecorator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
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

public class Mera718LBSPRPolicy {
    public static final Path COLUMNS_TO_PRINT = MeraOMHotstartsCalibration.MAIN_DIRECTORY.resolve("full_columns_to_print.yaml");
    static private LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
            new LinkedHashMap<>();

    public static void main(String[] args) throws IOException {


        final AlgorithmFactory<? extends AdditionalStartable> factory = MeraOneSpeciesSlice1.ALL_OF_THEM.get("LBSPR_season");
        Preconditions.checkArgument(factory!=null);
        selectedPolicies.put(
                "lbspr_hadrian",
                factory
        );


        Path mainDirectory = Paths.get("docs","mera_hub","slice5_yesgeography_twospecies").resolve("results");


        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list_faster.csv");
        Path pathToOutput = mainDirectory.resolve("policy_lbspr_hadrian");
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

                        ((SPRAgentBuilderFixedSample) ((FlexibleScenario) scenario).getPlugins().get(1)).setUseTNCFormula(false);
                        ((SPRAgentBuilderFixedSample) ((FlexibleScenario) scenario).getPlugins().get(2)).setUseTNCFormula(false);
                    }
                });



    }
}