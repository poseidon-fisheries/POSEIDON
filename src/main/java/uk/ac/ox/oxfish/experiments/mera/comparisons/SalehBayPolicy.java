package uk.ac.ox.oxfish.experiments.mera.comparisons;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.PenalizedGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PenalizedGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FlexibleAbundanceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketProxy;
import uk.ac.ox.oxfish.model.market.ThresholdWeightPrice;
import uk.ac.ox.oxfish.model.plugins.FisherEntryByProfitFactory;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.regs.factory.IQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SalehBayPolicy {

    static private LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> selectedPolicies =
            new LinkedHashMap<>();

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


    public static final AdditionalStartable MPA_CHANGE = new AdditionalStartable() {
        @Override
        public void start(FishState model) {
            {
                int[][] coordinatesForMPA = new int[][]{
                        {16,28},
                        {15,20},
                        {16,20},
                        {16,19},
                        {15,19},
                        {9,21},
                        {8,16},
                        {9,17},
                        {10,17},
                        {9,18},
                        {10,18}
                };
                for (int[] coordinate : coordinatesForMPA) {
                    StartingMPA.quicklyAddMPAToSeaTile(model.getMap().getSeaTile(coordinate[0], coordinate[1]));

                }

                for (Fisher fisher : model.getFishers())
                    fisher.setRegulation(new ProtectedAreasOnly());
                for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                    fisherFactory.getValue().setRegulations(new ProtectedAreasOnlyFactory());
                }

            }
        }
    };

    //single TAC for every species and across all boats
    public static final AdditionalStartable GLOBAL_TAC = new AdditionalStartable() {
        @Override
        public void start(FishState model) {
            {

                TACMonoFactory tacMonoFactory = new TACMonoFactory();
                tacMonoFactory.setQuota(new FixedDoubleParameter(7464000));

                for (Fisher fisher : model.getFishers())
                    fisher.setRegulation(tacMonoFactory.apply(model));
                for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                    fisherFactory.getValue().setRegulations(tacMonoFactory);
                }

            }
        }
    };
    //single TAC for every species and across all boats
    public static final AdditionalStartable GLOBAL_IQ = new AdditionalStartable() {
        @Override
        public void start(FishState model) {
            {

                IQMonoFactory iqMonoFactory = new IQMonoFactory();
                iqMonoFactory.setIndividualQuota(new FixedDoubleParameter(7464000d/448d));

                for (Fisher fisher : model.getFishers())
                    fisher.setRegulation(iqMonoFactory.apply(model));
                //new entrants get nothing!
                FishingSeasonFactory seasonFactory = new FishingSeasonFactory(0,true);
                for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                    fisherFactory.getValue().setRegulations(seasonFactory);
                }

            }
        }
    };


    static {


        selectedPolicies.put(
                "global_tac",
                fishState -> GLOBAL_TAC
        );


        selectedPolicies.put(
                "global_iq",
                fishState -> GLOBAL_IQ
        );


        selectedPolicies.put("price_change",
                fishState -> PRICE_CHANGE);


        selectedPolicies.put(
                "mpa_estimate",
                fishState -> MPA_CHANGE
        );



        selectedPolicies.put(
                "reduce_catchability_10",
                fishState -> model -> {
                    {

                        for (Fisher fisher : model.getFishers()) {
                            final HoldLimitingDecoratorGear parentGear = (HoldLimitingDecoratorGear) fisher.getGear();
                            final Gear originalDelegate = parentGear.getDelegate();
                            parentGear.setDelegate(
                                    new PenalizedGear(.1,originalDelegate)
                            );

                        }
                        for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                            final HoldLimitingDecoratorFactory parentFactory = (HoldLimitingDecoratorFactory) fisherFactory.getValue().getGear();
                            final AlgorithmFactory<? extends Gear> originalDelegate = parentFactory.getDelegate();
                            final PenalizedGearFactory penaltyDelegate = new PenalizedGearFactory();
                            penaltyDelegate.setDelegate(originalDelegate);
                            penaltyDelegate.setPercentageCatchLost(new FixedDoubleParameter(.1));
                            parentFactory.setDelegate(penaltyDelegate);


                        }

                    }
                }
        );


//        selectedPolicies.put(
//                "reduce_catchability_90",
//                fishState -> model -> {
//                    {
//
//                        for (Fisher fisher : model.getFishers()) {
//                            final HoldLimitingDecoratorGear parentGear = (HoldLimitingDecoratorGear) fisher.getGear();
//                            final Gear originalDelegate = parentGear.getDelegate();
//                            parentGear.setDelegate(
//                                    new PenalizedGear(.9,originalDelegate)
//                            );
//
//                        }
//                        for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
//                            final HoldLimitingDecoratorFactory parentFactory = (HoldLimitingDecoratorFactory) fisherFactory.getValue().getGear();
//                            final AlgorithmFactory<? extends Gear> originalDelegate = parentFactory.getDelegate();
//                            final PenalizedGearFactory penaltyDelegate = new PenalizedGearFactory();
//                            penaltyDelegate.setDelegate(originalDelegate);
//                            penaltyDelegate.setPercentageCatchLost(new FixedDoubleParameter(.9));
//                            parentFactory.setDelegate(penaltyDelegate);
//
//
//                        }
//
//                    }
//                }
//        );

        selectedPolicies.put(
                "75pc_effort",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy((int)((3456*.75)/24), true);
                }
        );
        selectedPolicies.put(
                "3mo_closed",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildSeasonalLimit(365-(30*3), true);
                }
        );
        selectedPolicies.put(
                "0_days",
                fishState -> {
                    return MeraOneSpeciesSlice1.buildMaxDaysOutPolicy(0, true);
                }
        );

//        selectedPolicies.put(
//                "mpa_10",
//                fishState -> model -> {
//                    {
//
//                        StartingMPA mpa = new StartingMPA(7,6,2,4);
//                        mpa.buildMPA(model.getMap());
//                        model.getMap().recomputeTilesMPA();
//
//                        for (Fisher fisher : model.getFishers())
//                            fisher.setRegulation(new ProtectedAreasOnly());
//                        for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
//                            fisherFactory.getValue().setRegulations(new ProtectedAreasOnlyFactory());
//                        }
//
//                    }
//                }
//        );
//


        selectedPolicies.put("BAU",
                new AlgorithmFactory<AdditionalStartable>() {
                    @Override
                    public AdditionalStartable apply(FishState fishState) {
                        return new AdditionalStartable() {
                            @Override
                            public void start(FishState model) {

                            }
                        };
                    }
                });


    }


    private static final Consumer<Scenario>  addEntryAndExit = new Consumer<Scenario>() {
        @Override
        public void accept(Scenario scenario) {

            FlexibleScenario flexible = ((FlexibleScenario) scenario);

            //exit...
            for (FisherDefinition fisherDefinition : flexible.getFisherDefinitions()) {
                //we are going to have to change the departing strategy here...
                FullSeasonalRetiredDecoratorFactory exitRules = new FullSeasonalRetiredDecoratorFactory();
                exitRules.setVariableName("PROFITS_PER_TRIP");
                exitRules.setInertia(new FixedDoubleParameter(1d));
                exitRules.setDecorated(fisherDefinition.getDepartingStrategy());
                exitRules.setTargetVariable(new FixedDoubleParameter(1875000d)); //that's 375000 times 5
                exitRules.setMinimumVariable(new FixedDoubleParameter(0));
                exitRules.setFirstYearYouCanSwitch(new FixedDoubleParameter(0));
                exitRules.setCanReturnFromRetirement(true);
                exitRules.setMaxHoursOutWhenSeasonal(new FixedDoubleParameter(7200d/2d));
                fisherDefinition.setDepartingStrategy(exitRules);

            }
            //entry
            final FisherEntryByProfitFactory entryObject = new FisherEntryByProfitFactory();
            entryObject.setFixedCostsToCover(new FixedDoubleParameter(1875000d)); //basically they need to cover the target before getting in!
            entryObject.setProfitDataColumnName("Average Trip Income");
            entryObject.setCostsFinalColumnName("Average Trip Variable Costs");
            entryObject.setPopulationName("population0");
            flexible.getPlugins().add(
                    entryObject
            );

        }
    };


    public static void main(String[] args) throws IOException {


        //  generateScenarioFiles();


        Path mainDirectory = SalehBayCalibration.MAIN_DIRECTORY.resolve("results");


        Path pathToScenarioFiles = mainDirectory.resolve("scenarios").resolve("scenario_list.csv");
        Path pathToOutput = mainDirectory.resolve("non-hybrid");
        pathToOutput.toFile().mkdir();

        //what we do is that we intercept policies from the original slice 1 and before we let them start we also apply
        //our prepareScenarioForPolicy consumer ahead of time

        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies =
                selectedPolicies;
        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 30, SalehBayCalibration.MAIN_DIRECTORY.resolve("columnsToPrint.yaml"),
                addEntryAndExit);


    }


}
