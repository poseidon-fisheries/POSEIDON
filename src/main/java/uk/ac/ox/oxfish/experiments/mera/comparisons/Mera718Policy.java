package uk.ac.ox.oxfish.experiments.mera.comparisons;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.boxcars.AbundanceGathererBuilder;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FlexibleAbundanceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketProxy;
import uk.ac.ox.oxfish.model.market.ThresholdWeightPrice;
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

public class Mera718Policy {


    public static final Path COLUMNS_TO_PRINT = MeraOMHotstartsCalibration.MAIN_DIRECTORY.resolve("full_columns_to_print.yaml");
    static private LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
            new LinkedHashMap<>();
    static {



        String[] otherPolicies = {
                "BAU",
                "multi_lastcatch_qb",
                "multi_lastcatch_70_qb",
                "multi_lastcatch_50_qb" ,
                "lastcatch",
                "lastcatch_70",
                "closed_multi_itarget1cpue",
                "LBSPR_season",
                "LTARGETE_1_fleet",
                "LTARGETE_1_season",
                "LTARGETE_4_fleet",
                "LTARGETE_4_season",
                "YEARLY_LTARGETE_1_season",
                "YEARLY_LTARGETE_4_season",
        "closed_multi_itarget2cpue"

        };


        for(String
                policy : otherPolicies){
            final AlgorithmFactory<? extends AdditionalStartable> factory = MeraOneSpeciesSlice1.ALL_OF_THEM.get(policy);
            Preconditions.checkArgument(factory!=null,policy);
            selectedPolicies.put(
                    policy,
                    factory
            );
        }

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




    }

    public static final AdditionalStartable PRICE_CHANGE = new AdditionalStartable() {
        @Override
        public void start(FishState model) {

            for (Port port : model.getPorts()) {
                for (Market market : port.getDefaultMarketMap().getMarkets()) {
                    final FlexibleAbundanceMarket castMarket = (FlexibleAbundanceMarket) ((MarketProxy) market).getDelegate();

                    castMarket.setPricingStrategy(
                            new ThresholdWeightPrice(
                                    castMarket.getMarginalPrice(),
                                    0,
                                    0.5
                            )
                    );
                }
            }
        }
    };

    public static void main(String[] args) throws IOException {




        Path mainDirectory = Paths.get("docs","mera_hub","slice4_nogeography_twospecies").resolve("results");
       // Path mainDirectory = Paths.get("docs","mera_hub","slice5_yesgeography_twospecies").resolve("results");


        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list_faster.csv");
        Path pathToOutput = mainDirectory.resolve("policy_monitored");


        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies = new LinkedHashMap<>();
        //adding additional startables!
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
                            builder.apply(fishState);

                            return policyFactory.getValue().apply(fishState);


                        }
                    }
            );
        }
        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 50, COLUMNS_TO_PRINT, null);



    }


}
