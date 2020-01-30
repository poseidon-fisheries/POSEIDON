package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.DelayGearDecorator;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
import uk.ac.ox.oxfish.fisher.strategies.gear.PeriodicUpdateGearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.factory.PeriodicUpdateFromListFactory;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMappedFactory;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesWithPremium;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.function.Consumer;

public class Slice6SelectivityMarket {

    private static final String SCENARIO_NAME = "tropfish_tl_2y_onemoretime_8h";
    private static final int YEARS_TO_RUN = 12;
    private static final int RUNS_PER_POLICY = 1;
    public static final int MAX_SELECTIVITY_SHIFT = 30;
    //public static String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static String DIRECTORY =
            "/home/carrknight/code/oxfish/docs/indonesia_hub/runs/712/slice6/calibration/sweeps/";



    public static void main(String[] args) throws IOException {
        selectivityIncentive("selectivity_incentive", SCENARIO_NAME,
                MAX_SELECTIVITY_SHIFT,
                3,
                "Lutjanus malabaricus",
                10);
    }


    private static Consumer<Scenario> addGearUpdate(String species,
                                                    double shiftOfSelectivity,
                                                    int[] populations) {

        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario scenario) {

                FishYAML yaml = new FishYAML();

                ((FlexibleScenario) scenario).setTagsToTrackSeparately(
                        "PERIODIC_UPDATE_GEAR_0,PERIODIC_UPDATE_GEAR_1");


                for (int population : populations) {
                    final FisherDefinition fisherDefinition = ((FlexibleScenario) scenario).getFisherDefinitions().get(population);
                    final PeriodicUpdateFromListFactory factory = new PeriodicUpdateFromListFactory();
                    final AlgorithmFactory<? extends Gear> originalGear = fisherDefinition.getGear();
                    //weird but effective way to copy
                    final AlgorithmFactory<? extends Gear> betterGear =
                            yaml.loadAs(yaml.dump(fisherDefinition.getGear()),AlgorithmFactory.class);


                    final SimpleLogisticGearFactory toChange = (SimpleLogisticGearFactory)
                            ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory)
                                    ((DelayGearDecoratorFactory) betterGear).getDelegate()).getDelegate()).getDelegate()) .getGears().get(species);
                    toChange.setSelexParameter1(
                            new FixedDoubleParameter(
                                    ((FixedDoubleParameter) toChange.getSelexParameter1()).getFixedValue()+shiftOfSelectivity));


                    factory.setAvailableGears(
                            Lists.newArrayList(originalGear,betterGear)

                    );

                    fisherDefinition.setGearStrategy(
                            factory
                    );
                }

            }
        };


    }


    private static Consumer<Scenario> addConditionalMarket(double premium, String species,
                                                    double maturityBin, boolean premiumFirst,
                                                    boolean premiumSecond, boolean premiumThird) {


        return new Consumer<Scenario>() {
            @Override
            public void accept(Scenario flexible) {
                ThreePricesMarketFactory nonPremium =
                        (ThreePricesMarketFactory) ((ThreePricesMappedFactory) ((FlexibleScenario) flexible).getMarket()).getMarkets().get(
                                species
                        );

                nonPremium.setHighAgeThreshold(new FixedDoubleParameter(maturityBin));
                ThreePricesWithPremium newMarket = new ThreePricesWithPremium();
                newMarket.setNonPremiumMarket(nonPremium);
                newMarket.setPremiumInPercentage(new FixedDoubleParameter(premium));
                newMarket.setPremiumFirstBin(premiumFirst);
                newMarket.setPremiumSecondBin(premiumSecond);
                newMarket.setPremiumThirdBin(premiumThird);
                ((ThreePricesMappedFactory) ((FlexibleScenario) flexible).getMarket()).getMarkets().put(species, 
                        newMarket);

            }


        };

    }





    public static BatchRunner setupRunner(String filename, final int yearsToRun){

        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Actual Average Cash-Flow",
                "Lutjanus malabaricus Earnings",
                "Lutjanus malabaricus Landings",
                "Epinephelus areolatus Earnings",
                "Epinephelus areolatus Landings",
                "Lutjanus erythropterus Earnings",
                "Lutjanus erythropterus Landings",
                "Pristipomoides multidens Earnings",
                "Pristipomoides multidens Landings",
                "Actual Average Hours Out",

                "Full-time fishers",
                "Seasonal fishers",
                "Retired fishers",
                "SPR " + "Epinephelus areolatus" + " " + "100_areolatus",
                "SPR " + "Pristipomoides multidens" + " " + "100_multidens",
                "SPR " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "SPR " + "Lutjanus erythropterus" + " " + "100_erythropterus",
                "Biomass Epinephelus areolatus",
                "Biomass Pristipomoides multidens",
                "Biomass Lutjanus malabaricus",
                "Biomass Lutjanus erythropterus",
                "SPR Oracle - " + "Epinephelus areolatus",
                "SPR Oracle - " + "Pristipomoides multidens" ,
                "SPR Oracle - " + "Lutjanus malabaricus",
                "SPR Oracle - " + "Lutjanus erythropterus",
                //  "Average Daily Fishing Mortality Lutjanus malabaricus",
                "Yearly Fishing Mortality Lutjanus malabaricus",
                "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
                "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
                "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus");

        for (int i = 0; i < 4; i++) {

            columnsToPrint.add("Total Landings of population" + i);
            columnsToPrint.add("Full-time fishers of population" + i);
            columnsToPrint.add("Retired fishers of population" + i);
            columnsToPrint.add("Seasonal fishers of population" + i);
            columnsToPrint.add("Actual Average Cash-Flow of population" + i);
            columnsToPrint.add("Average Number of Trips of population" + i);
            columnsToPrint.add("Number Of Active Fishers of population" + i);
            columnsToPrint.add("Average Distance From Port of population" + i);
            columnsToPrint.add("Average Trip Duration of population" + i);
            columnsToPrint.add("Epinephelus areolatus Landings of population" + i);
            columnsToPrint.add("Pristipomoides multidens Landings of population" + i);
            columnsToPrint.add("Lutjanus malabaricus Landings of population" + i);
            columnsToPrint.add("Lutjanus erythropterus Landings of population" + i);
            columnsToPrint.add("Others Landings of population" + i);
            columnsToPrint.add("Actual Average Distance From Port of population" + i);
            columnsToPrint.add("Actual Average Variable Costs of population" + i);
            columnsToPrint.add("Total Variable Costs of population" + i);
            columnsToPrint.add("Total Hours Out of population" + i);
        }

        for (int i = 0; i < 2; i++) {

            columnsToPrint.add("Total Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
//            columnsToPrint.add("Full-time fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
//            columnsToPrint.add("Retired fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
//            columnsToPrint.add("Seasonal fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Actual Average Cash-Flow of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Average Number of Trips of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Number Of Active Fishers of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Average Distance From Port of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Average Trip Duration of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Epinephelus areolatus Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Pristipomoides multidens Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Lutjanus malabaricus Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Lutjanus erythropterus Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Others Landings of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Actual Average Distance From Port of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Actual Average Variable Costs of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Total Variable Costs of " + PeriodicUpdateGearStrategy.tag+"_" + i);
            columnsToPrint.add("Total Hours Out of " + PeriodicUpdateGearStrategy.tag+"_" + i);
        }

        BatchRunner batchRunner = new BatchRunner(
                Paths.get(DIRECTORY,
                        filename + ".yaml"),
                yearsToRun,
                columnsToPrint,
                Paths.get(DIRECTORY,
                        filename),
                null,
                System.currentTimeMillis(),
                -1
        );
        return batchRunner;
    }


    private static void selectivityIncentive(
            String name,
            final String filename,
            final  int maxSelectivityShift,
            final int maxPremium,
            String species,
            final int maturityBin) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,selectivity,premium,all,variable,value\n");
        fileWriter.flush();

        final boolean[] allSubsidized = {false, true};

        for(int selectivityIncrease = 0; selectivityIncrease<= maxSelectivityShift; selectivityIncrease++) {
            for (double premium = 1; premium < maxPremium; premium += .5) {
                for (boolean isAllSubsidized :allSubsidized) {


                    BatchRunner runner = setupRunner(filename, YEARS_TO_RUN);


                    int currentShiftSelectivity = selectivityIncrease;
                    double finalPremium = premium;


                    runner.setScenarioSetup(
                            addGearUpdate(species,currentShiftSelectivity,new int[]{0,3}).andThen(
                                    addConditionalMarket(finalPremium, species, maturityBin,
                                            isAllSubsidized,isAllSubsidized,true)
                            )
                    );

                    System.out.println(" selectivity: " + selectivityIncrease +"; premium "+ premium +
                            "; allSubsidized: " + isAllSubsidized);

                    runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                        @Override
                        public void consume(StringBuffer writer, FishState model, Integer year) {
                            writer.
                                    append(currentShiftSelectivity).append(",").
                                    append(finalPremium).append(",").
                                    append(isAllSubsidized).append(",");
                        }
                    });


                    //while (runner.getRunsDone() < 1) {
                    for (int i = 0; i < RUNS_PER_POLICY; i++) {
                        StringBuffer tidy = new StringBuffer();
                        runner.run(tidy);
                        fileWriter.write(tidy.toString());
                        fileWriter.flush();
                    }
                }
            }
        }
        fileWriter.close();
    }

}
