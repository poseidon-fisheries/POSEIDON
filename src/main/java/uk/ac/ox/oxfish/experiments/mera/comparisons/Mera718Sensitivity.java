package uk.ac.ox.oxfish.experiments.mera.comparisons;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.boxcars.AbundanceGathererBuilder;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.OffSwitchDecorator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class Mera718Sensitivity {


    public static void main(String[] args) throws IOException {




        Path mainDirectory = Paths.get("docs","mera_hub","slice5_yesgeography_twospecies").resolve("results");
        // Path mainDirectory = Paths.get("docs","mera_hub","slice5_yesgeography_twospecies").resolve("results");


        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list_faster.csv");
        Path pathToOutput = mainDirectory.resolve("sensitivity_all");


        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> startingPolicies = new LinkedHashMap<>();
        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies = new LinkedHashMap<>();
        for(int i=0; i<100; i++) {
            int finalI = i;
            startingPolicies.put("currentEffort_" + i, new AlgorithmFactory<AdditionalStartable>() {
                @Override
                public AdditionalStartable apply(FishState fishState) {


                    return new AdditionalStartable() {
                        @Override
                        public void start(FishState model) {
                            model.random = new MersenneTwisterFast(finalI);
                            MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(250, true).start(model);
                        }
                    };

                }
            });
        }
        //adding additional startables!
        for (Map.Entry<String, AlgorithmFactory<? extends AdditionalStartable>> policyFactory : startingPolicies.entrySet()) {
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
                            builder.apply(fishState);

                            return policyFactory.getValue().apply(fishState);


                        }
                    }
            );
        }
        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 50, Mera718Policy.COLUMNS_TO_PRINT, null);



    }


}


