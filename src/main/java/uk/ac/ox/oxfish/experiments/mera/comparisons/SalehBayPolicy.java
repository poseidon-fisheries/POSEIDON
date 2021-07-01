package uk.ac.ox.oxfish.experiments.mera.comparisons;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.HoldLimitingDecoratorGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.PenalizedGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HoldLimitingDecoratorFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PenalizedGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.FullSeasonalRetiredDecoratorFactory;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.FisherEntryByProfitFactory;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
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

    static {


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

        selectedPolicies.put(
                "mpa_10",
                fishState -> model -> {
                    {

                        StartingMPA mpa = new StartingMPA(7,6,2,4);
                        mpa.buildMPA(model.getMap());
                        model.getMap().recomputeTilesMPA();

                        for (Fisher fisher : model.getFishers())
                            fisher.setRegulation(new ProtectedAreasOnly());
                        for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                            fisherFactory.getValue().setRegulations(new ProtectedAreasOnlyFactory());
                        }

                    }
                }
        );

        selectedPolicies.put(
                "mpa_20",
                fishState -> model -> {
                    {

                        StartingMPA mpa = new StartingMPA(7,6,2,4);
                        mpa.buildMPA(model.getMap());
                        mpa = new StartingMPA(2,0,4,2);
                        mpa.buildMPA(model.getMap());
                        model.getMap().recomputeTilesMPA();

                        for (Fisher fisher : model.getFishers())
                            fisher.setRegulation(new ProtectedAreasOnly());
                        for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                            fisherFactory.getValue().setRegulations(new ProtectedAreasOnlyFactory());
                        }

                    }
                }
        );


        selectedPolicies.put("bau",
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
                exitRules.setVariableName("Average Trip Income");
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
        Path pathToOutput = mainDirectory.resolve("policy");


        //what we do is that we intercept policies from the original slice 1 and before we let them start we also apply
        //our prepareScenarioForPolicy consumer ahead of time

        final LinkedHashMap<String, AlgorithmFactory<? extends AdditionalStartable>> adjustedPolicies = selectedPolicies;
        MeraOneSpeciesSlice1.runSetOfScenarios(pathToScenarioFiles,
                pathToOutput,
                adjustedPolicies, 50, SalehBayCalibration.MAIN_DIRECTORY.resolve("columnsToPrint.yaml"),
                addEntryAndExit);


    }


}
