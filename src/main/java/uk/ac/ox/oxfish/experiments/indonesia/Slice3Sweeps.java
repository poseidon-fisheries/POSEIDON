/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments.indonesia;




import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.HeterogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.BatchRunner;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMappedFactory;
import uk.ac.ox.oxfish.model.market.factory.ThreePricesMarketFactory;
import uk.ac.ox.oxfish.model.plugins.TowAndAltitudePluginFactory;
import uk.ac.ox.oxfish.model.regs.*;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.oxfish.model.regs.factory.TriggerRegulationFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Slice3Sweeps {

    //public static String DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
    public static String DIRECTORY = "docs/indonesia_hub/runs/712/slice4/non-recalibrated/policy/";
    public static final int MIN_DAYS_OUT = 50;
    public static final int RUNS_PER_POLICY = 1;
    public static final int MAX_DAYS_OUT = 250;
    public static  int POPULATIONS = 4;


    public static void main(String[] args) throws IOException {
        DIRECTORY = "docs/indonesia_hub/runs/712/slice4/non-recalibrated/policy/";
        POPULATIONS = 4;

        //effort control
        //all boats are controlled
//        effortControl("all_manyruns",
//                      new String[]{"big","small","medium","small10"},
//                      "pessimistic_spinup",
//                      1, MIN_DAYS_OUT);
//        effortControl("all_manyruns_quick2",
//                      new String[]{"big","small","medium","small10"},
//                      "optimistic_spinup",
//                      1, MIN_DAYS_OUT);
//
//        //only boats >10GT are controlled
//        effortControl("10_manyruns",
//                      new String[]{"big","medium","small10"},
//                      "optimistic_spinup",
//                      1, MIN_DAYS_OUT);
//        effortControl("10_manyruns",
//                      new String[]{"big","medium","small10"},
//                      "pessimistic_spinup",
//                      1, MIN_DAYS_OUT);

//
//        //price premium
//        pricePremium("premium_multidens_quick2","optimistic_spinup",10,"Pristipomoides multidens");
//        pricePremium("premium_malabaricus_quick3","optimistic_spinup",10,"Lutjanus malabaricus");
//        selectivityTest("selectivity_sweep_quick2","optimistic_spinup");
 //       pricePenalty("malus_multidens_manyruns","optimistic_spinup",10,"Pristipomoides multidens");
        //pricePenalty("malus_malabaricus_quick","optimistic_spinup",10,"Lutjanus malabaricus");
//
//        pricePremium("premium_multidens_quick2","pessimistic_spinup",10,"Pristipomoides multidens");
 //       pricePremium("premium_malabaricus_quick2","pessimistic_spinup",10,"Lutjanus malabaricus");
      //  pricePenalty("malus_malabaricus_quick","pessimistic_spinup",10,"Lutjanus malabaricus");


//        selectivityTest("selectivity_sweep_quick2","pessimistic_spinup");
      //  pricePenalty("malus_multidens_manyruns","pessimistic_spinup",10,"Pristipomoides multidens");


        //fleet reduction
//        fleetReduction("fleetreduction_quick","optimistic_spinup",1);
//        fleetReduction("fleetreduction_quick","pessimistic_spinup",1);


//
//            delays("delay_all_quick", new String[]{"big","small","medium","small10"}, "optimistic_spinup", 1, 50);
//              delays("delay_all_quick", new String[]{"big","small","medium","small10"}, "pessimistic_spinup", 1, 50);
//
//            delays("delay_10_quick", new String[]{"big","small10","medium"}, "optimistic_spinup", 1, 50);
//              delays("delay_10_quick", new String[]{"big","small10","medium"}, "pessimistic_spinup", 1, 50);
//

            delaysOnce("delay_once_all_quick", new String[]{"big","small","medium","small10"}, "optimistic_spinup", 1, 50);
              delaysOnce("delay_once_all_quick", new String[]{"big","small","medium","small10"}, "pessimistic_spinup", 1, 50);

            delaysOnce("delay_once_10_quick", new String[]{"big","small10","medium"}, "optimistic_spinup", 1, 50);
              delaysOnce("delay_once_10_quick", new String[]{"big","small10","medium"}, "pessimistic_spinup", 1, 50);
    }

    public static void main3(String[] args) throws IOException {
        DIRECTORY = "docs/indonesia_hub/runs/712/slice3/policy/";
        POPULATIONS = 3;

//        effortControl("all", new String[]{"big","small","medium"}, "fixed_recruits", 4, MIN_DAYS_OUT);
//        effortControl("large", new String[]{"big"}, "fixed_recruits", 4, MIN_DAYS_OUT);
//        effortControl("medium", new String[]{"big","medium"}, "fixed_recruits", 4, MIN_DAYS_OUT);


//
//        effortControl("all_manyruns",
//                      new String[]{"big","small","medium"},
//                      "optimistic_recruits",
//                      1, MIN_DAYS_OUT);
//        effortControl("large", new String[]{"big"}, "optimistic_recruits", 1, MIN_DAYS_OUT);
//        effortControl("medium", new String[]{"big","medium"}, "optimistic_recruits", 1, MIN_DAYS_OUT);

//        effortControl("all_manyruns",
//                      new String[]{"big","small","medium"},
//                      "optimistic_recruits_spinup_fixedmarket",
//                      1,
//                      MIN_DAYS_OUT);
//        effortControl("large", new String[]{"big"}, "optimistic_recruits_spinup_fixedmarket", 1, MIN_DAYS_OUT);
//        effortControl("medium", new String[]{"big","medium"}, "optimistic_recruits_spinup_fixedmarket", 1, MIN_DAYS_OUT);


//        effortControl("all_manyruns",
//                      new String[]{"big","small","medium"},
//                      "pessimistic_recruits_spinup",
//                      1,
//                      MIN_DAYS_OUT);

//       fleetReduction("fleetreduction","optimistic_recruits",1);
//       fleetReduction("fleetreduction","fixed_recruits",4);
//        fleetReduction("fleetreduction_manyruns","optimistic_recruits_spinup_fixedmarket",1);
//        fleetReduction("fleetreduction_manyruns","pessimistic_recruits_spinup",1);


//        pricePremium("premium_malabaricus","fixed_recruits",10,"Lutjanus malabaricus");
//        pricePremium("premium_multidens","fixed_recruits",10,"Pristipomoides multidens");
//        pricePremium("premium_malabaricus","optimistic_recruits",10,"Lutjanus malabaricus");
//        pricePremium("premium_multidens","optimistic_recruits",10,"Pristipomoides multidens");
//        pricePremium("premium_malabaricus3","optimistic_recruits_spinup_fixedmarket",10,"Lutjanus malabaricus");
        //    pricePremium("premium_ero3","optimistic_recruits_spinup_fixedmarket",10,"Lutjanus erythropterus");
        //       pricePremium("premium_multidens3","optimistic_recruits_spinup_fixedmarket",10,"Pristipomoides multidens");
        //   pricePremium("premium_ero3","pessimistic_recruits_spinup",10,"Lutjanus erythropterus");

        //    pricePremium("premium_malabaricus_manyruns","pessimistic_recruits_spinup",10,"Lutjanus malabaricus");
        //       pricePremium("premium_multidens3","pessimistic_recruits_spinup",10,"Pristipomoides multidens");


        // pricePenalty("malus_malabaricus","optimistic_recruits_spinup_fixedmarket",10,"Lutjanus malabaricus");
//        pricePenalty("malus_multidens","optimistic_recruits_spinup_fixedmarket",10,"Pristipomoides multidens");
//        pricePenalty("malus_malabaricus","pessimistic_recruits_spinup",10,"Lutjanus malabaricus");
//        pricePenalty("malus_multidens","pessimistic_recruits_spinup",10,"Pristipomoides multidens");


        //    delays("delay_all_manyruns", new String[]{"big","small","medium"}, "optimistic_recruits_spinup_fixedmarket", 1, 50);
        //      delays("delay_all_manyruns", new String[]{"big","small","medium"}, "pessimistic_recruits_spinup", 1, 50);

//        adaptiveSPR("spr_malabaricus", MIN_DAYS_OUT, "optimistic_recruits", "Lutjanus malabaricus", "100_malabaricus",
//                    false);
//        adaptiveSPR("spr_multidens", MIN_DAYS_OUT, "optimistic_recruits", "Pristipomoides multidens", "100_multidens",
//                    false);
//        adaptiveSPR("spr_malabaricus", MIN_DAYS_OUT, "fixed_recruits", "Lutjanus malabaricus", "100_malabaricus", false);
//        adaptiveSPR("spr_multidens", MIN_DAYS_OUT, "fixed_recruits", "Pristipomoides multidens", "100_multidens", false);
//        adaptiveSPR("spr_malabaricus", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket", "Lutjanus malabaricus", "100_malabaricus",
//                    false);
//        adaptiveSPR("spr_multidens", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket", "Pristipomoides multidens", "100_multidens",
//                    false);

//        adaptiveSPR("oraclespr_malabaricus", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket",
//                    "Lutjanus malabaricus", "100_malabaricus",
//                    true);
//        adaptiveSPR("oraclespr_multidens", MIN_DAYS_OUT, "optimistic_recruits_spinup_fixedmarket",
//                    "Pristipomoides multidens", "100_multidens",
//                    true);

//
        //     recruitmentFailure("recruit_failure2","fixed_recruits",4,10);
//        recruitmentFailure("recruit_failure","optimistic_recruits",4,2);
//        recruitmentFailure("recruit_failure","optimistic_recruits_spinup_fixedmarket",4,2);


//        selectivityTest("selectivity_sweep","optimistic_recruits_spinup_fixedmarket");
//        selectivityTest("selectivity_sweep","pessimistic_recruits_spinup");





        //FURTHER
        DIRECTORY = DIRECTORY + "further/";
        //pricePremium("premium_malabaricus_manyruns","pessimistic_recruits_spinup_doublegear",10,"Lutjanus malabaricus");
    /*    pricePremium("premium_malabaricus_manyruns","optimistic_recruits_spinup_fixedmarket_doublegear",
                     10,"Lutjanus malabaricus");
                     */

    }

    private static void effortControl(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int minDaysOut) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = MAX_DAYS_OUT; maxDaysOut>= minDaysOut; maxDaysOut-=10) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);


            int finalMaxDaysOut = maxDaysOut;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                (Steppable) simState -> {
                                                    fisherloop:
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {
                                                                fisher.setRegulation(
                                                                        new MaxHoursOutRegulation(
                                                                                new ProtectedAreasOnly(),
                                                                                finalMaxDaysOut*24d
                                                                        ));
                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }

                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }





    private static void delays(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int maxDelay) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int waitTimes = 0; waitTimes<= maxDelay; waitTimes+=5) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);


            int finalWaitTime = waitTimes *24;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                (Steppable) simState -> {

                                                    HashMap<String, Integer> waitTimes = new HashMap<>();
                                                    waitTimes.put("Sumenep",finalWaitTime);
                                                    waitTimes.put("Gili Iyang",finalWaitTime);
                                                    waitTimes.put("Bajomulyo",finalWaitTime);
                                                    waitTimes.put("Brondong",finalWaitTime);
                                                    waitTimes.put("Karangsong",finalWaitTime);
                                                    waitTimes.put("Tanjung Pandan",finalWaitTime);
                                                    waitTimes.put("Probolinggo",finalWaitTime);


                                                    fisherloop:
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {
                                                                fisher.setRegulation(
                                                                        new PortBasedWaitTimesDecorator(
                                                                                new ProtectedAreasOnly(),
                                                                                waitTimes
                                                                        ));
                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }

                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalWaitTime).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }

    private static void delaysOnce(
            String name,
            String[] modifiedTags, final String filename, final int shockYear,
            final int maxDelay) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int waitTimes = 0; waitTimes<= maxDelay; waitTimes+=10) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);


            int finalWaitTime = waitTimes;

            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    @Override
                                    public void start(FishState model) {

                                        model.scheduleOnceAtTheBeginningOfYear(
                                                (Steppable) simState -> {



                                                    fisherloop:
                                                    for (Fisher fisher :
                                                            ((FishState) simState).getFishers()) {

                                                        for (String tag : modifiedTags) {
                                                            if (fisher.getTags().contains(tag)) {

                                                                int endDate = model.getRandom().nextInt(365);
                                                                int startDate = endDate-finalWaitTime;
                                                                if(startDate<0) {
                                                                    endDate = endDate + startDate;
                                                                    startDate=0;
                                                                }

                                                                fisher.setRegulation(
                                                                        new ArbitraryPause(
                                                                                startDate,
                                                                                endDate,
                                                                                fisher.getRegulation()
                                                                        ));
                                                                continue fisherloop;
                                                            }
                                                        }
                                                    }
                                                },
                                                StepOrder.DAWN,
                                                shockYear
                                        );


                                    }

                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalWaitTime).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }

    private static void fleetReduction(
            String name,
            final String filename, final int shockYear) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double probability=0; probability<=.05; probability=FishStateUtilities.round5(probability+.005)) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalProbability = probability;
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    /**
                                     * this gets called by the fish-state right after the scenario has started. It's
                                     * useful to set up steppables
                                     * or just to percolate a reference to the model
                                     *
                                     * @param model the model
                                     */
                                    @Override
                                    public void start(FishState model) {
                                        model.scheduleEveryYear(new Steppable() {
                                            @Override
                                            public void step(SimState simState) {
                                                if(model.getYear()<shockYear)
                                                    return;
                                                List<Fisher> toKill = new LinkedList<>();

                                                for(Fisher fisher : model.getFishers()) {
                                                    if (model.getRandom().nextDouble() < finalProbability)
                                                        toKill.add(fisher);
                                                }
                                                for (Fisher sacrifice : toKill) {
                                                    model.killSpecificFisher(sacrifice);

                                                }


                                            }
                                        },StepOrder.DAWN);
                                    }

                                    /**
                                     * tell the startable to turnoff,
                                     */
                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalProbability).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }


    private static void pricePremium(
            String name,
            final String filename, final int maturityBin,
            final String premiumSpecies
    )throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double markup=0; markup<=3; markup=FishStateUtilities.round(markup+.5)) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);





            double finalMarkup = markup;
            //add markup in the scenario
            runner.setScenarioSetup(
                    scenario -> {

                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        ThreePricesMarketFactory market =
                                ((ThreePricesMappedFactory) flexible.getMarket()).getMarkets().get(
                                        premiumSpecies
                                );

                        market.setHighAgeThreshold(new FixedDoubleParameter(maturityBin));
                        double newPrice = ((FixedDoubleParameter) market.getPriceAboveThresholds()).getFixedValue() *
                                (1d + finalMarkup);
                        market.setPriceAboveThresholds(
                                new FixedDoubleParameter(
                                        newPrice
                                )
                        );
                        System.out.println(newPrice);

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMarkup).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }


    /**
     * lowers the price of fish caught below the maturity value
     * @param name
     * @param filename
     * @param premiumSpecies
     * @throws IOException
     */
    private static void pricePenalty(
            String name,
            final String filename, final int maturityBin,
            final String premiumSpecies
    )throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double markup=0; markup<=1; markup=FishStateUtilities.round(markup+.25)) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);





            double finalMarkup = markup;
            //add markup in the scenario
            runner.setScenarioSetup(
                    scenario -> {

                        FlexibleScenario flexible = (FlexibleScenario) scenario;

                        ThreePricesMarketFactory market =
                                ((ThreePricesMappedFactory) flexible.getMarket()).getMarkets().get(
                                        premiumSpecies
                                );

                        market.setLowAgeThreshold(new FixedDoubleParameter(maturityBin));
                        if(((FixedDoubleParameter) market.getHighAgeThreshold()).getFixedValue()<=maturityBin)
                            market.setHighAgeThreshold(new FixedDoubleParameter(maturityBin+1));


                        double newPrice = ((FixedDoubleParameter) market.getPriceBelowThreshold()).getFixedValue() *
                                (finalMarkup);
                        market.setPriceBelowThreshold(
                                new FixedDoubleParameter(
                                        newPrice
                                )
                        );
                        System.out.println(newPrice);

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMarkup).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    //"SPR " + "Pristipomoides multidens" + " " + "100_multidens"
    private static void adaptiveSPR(
            String name,
            final int minDaysOut,
            final String filename,
            final String speciesTargeted,
            final String survey_name,
            boolean oracleTargeting)throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut = MAX_DAYS_OUT; maxDaysOut>= minDaysOut; maxDaysOut-=10) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            int finalMaxDaysOut = maxDaysOut;
            runner.setScenarioSetup(
                    scenario -> {
                        for(FisherDefinition definition : ((FlexibleScenario) scenario).getFisherDefinitions()) {
                            TriggerRegulationFactory regulation = new TriggerRegulationFactory();
                            regulation.setBusinessAsUsual(new AnarchyFactory());
                            regulation.setEmergency(new MaxHoursOutFactory(finalMaxDaysOut *24));
                            regulation.setHighThreshold(new FixedDoubleParameter(.4));
                            regulation.setLowThreshold(new FixedDoubleParameter(.2));
                            if(oracleTargeting)
                                regulation.setIndicatorName("SPR Oracle - "+speciesTargeted);
                            else
                                regulation.setIndicatorName("SPR "+speciesTargeted+ " " + survey_name);



                            definition.setRegulation(
                                    regulation
                            );
                        }


                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalMaxDaysOut).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }



    //no policy, but simulates a year 1 death of all bin 0 and bin 1 population
    private static void recruitmentFailure(
            String name,
            final String filename, final int shockYear, final int runs) throws IOException {
        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();


        BatchRunner runner = setupRunner(filename, 15,POPULATIONS);

        for(int failure = 1; failure>=0; failure--) {


            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            int finalFailure = failure;
            runner.setScenarioSetup(
                    scenario -> {

                        //at year 4, impose regulation
                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        flexible.getPlugins().add(
                                fishState -> new AdditionalStartable() {
                                    /**
                                     * this gets called by the fish-state right after the scenario has started. It's
                                     * useful to set up steppables
                                     * or just to percolate a reference to the model
                                     *
                                     * @param model the model
                                     */
                                    @Override
                                    public void start(FishState model) {
                                        if (finalFailure >0) {
                                            model.scheduleOnceAtTheBeginningOfYear(new Steppable() {
                                                @Override
                                                public void step(SimState simState) {

                                                    for (SeaTile tile : model.getMap().getAllSeaTilesExcludingLandAsList())
                                                        for (Species species : model.getSpecies()) {


                                                            double[][] matrix = tile.getAbundance(
                                                                    species).asMatrix();
                                                            if(matrix == null || matrix.length==0 ||
                                                                    matrix[0].length ==0 ||
                                                                    species.isImaginary())
                                                                continue;
                                                            matrix[0][0] = 0;
                                                            matrix[0][1] = 0;
                                                        }


                                                }
                                            }, StepOrder.DAWN, shockYear);
                                        }
                                    }
                                    /**
                                     * tell the startable to turnoff,
                                     */
                                    @Override
                                    public void turnOff() {

                                    }
                                }
                        );

                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalFailure).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for (int i = 0; i < runs; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();

    }




    @NotNull
    public static BatchRunner setupRunner(String filename, final int yearsToRun,
                                          final int populations) {
        ArrayList<String> columnsToPrint = Lists.newArrayList(
                "Average Cash-Flow",
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
                "Percentage Mature Catches " + "Epinephelus areolatus" + " " + "100_areolatus",
                "Percentage Mature Catches " + "Pristipomoides multidens" + " " + "100_multidens",
                "Percentage Mature Catches " + "Lutjanus malabaricus" + " " + "100_malabaricus",
                "Percentage Mature Catches " + "Lutjanus erythropterus" + " " + "100_erythropterus");

        for(int i=0; i<populations; i++){
            columnsToPrint.add("Total Landings of population"+i);
            columnsToPrint.add("Average Cash-Flow of population"+i);
            columnsToPrint.add("Average Number of Trips of population"+i);
            columnsToPrint.add("Number Of Active Fishers of population"+i);
            columnsToPrint.add("Average Distance From Port of population"+i);
            columnsToPrint.add("Average Trip Duration of population"+i);
            columnsToPrint.add("Epinephelus areolatus Landings of population"+i);
            columnsToPrint.add("Pristipomoides multidens Landings of population"+i);
            columnsToPrint.add("Lutjanus malabaricus Landings of population"+i);
            columnsToPrint.add("Lutjanus erythropterus Landings of population"+i);
            columnsToPrint.add("Others Landings of population"+i);
        }


        for(int i=0; i<25; i++) {
            columnsToPrint.add("Epinephelus areolatus Catches (kg) - age bin " + i);
            columnsToPrint.add("Pristipomoides multidens Catches (kg) - age bin " + i);
            columnsToPrint.add("Lutjanus malabaricus Catches (kg) - age bin " + i);
            columnsToPrint.add("Lutjanus erythropterus Catches (kg) - age bin " + i);

            columnsToPrint.add("Epinephelus areolatus Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Lutjanus malabaricus Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Pristipomoides multidens Abundance 0."+i+" at day " + 365);
            columnsToPrint.add("Lutjanus erythropterus Abundance 0."+i+" at day " + 365);


            columnsToPrint.add("Epinephelus areolatus Catches(#) 0."+i+" 100_areolatus");
            columnsToPrint.add("Lutjanus malabaricus Catches(#) 0."+i+" 100_malabaricus");
            columnsToPrint.add("Pristipomoides multidens Catches(#) 0."+i+" 100_multidens");
            columnsToPrint.add("Lutjanus erythropterus Catches(#) 0."+i+" 100_erythropterus");
        }

        return new BatchRunner(
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
    }



    //sweep selectivity of small boats, see if it makes a difference anyway
    private static void selectivityTest(
            String name,
            final String filename) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+".csv").toFile());
        fileWriter.write("run,year,policy,variable,value\n");
        fileWriter.flush();

        for(double increase=0; increase<=3; increase=FishStateUtilities.round5(increase+.1)) {

            BatchRunner runner = setupRunner(filename, 15,POPULATIONS);




            //basically we want year 4 to change big boats regulations.
            //because I coded "run" poorly, we have to go through this series of pirouettes
            //to get it done right
            double finalIncrease = increase;
            runner.setScenarioSetup(
                    scenario -> {

                        FlexibleScenario flexible = (FlexibleScenario) scenario;
                        Preconditions.checkArgument(flexible.getFisherDefinitions().get(0).getTags().contains("small"));
                        ;
                        HomogeneousGearFactory malabaricus =
                                ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory) flexible.getFisherDefinitions().get(
                                        0).getGear()).getDelegate()).getDelegate()).getGears().get("Lutjanus malabaricus");

                        ((LogisticSelectivityGearFactory) malabaricus).setSelectivityAParameter(
                                new FixedDoubleParameter(
                                        ((FixedDoubleParameter) ((LogisticSelectivityGearFactory) malabaricus).getSelectivityAParameter()).getFixedValue()
                                                * finalIncrease
                                )
                        );


                        if(flexible.getFisherDefinitions().size()==4)
                        {
                            Preconditions.checkArgument(flexible.getFisherDefinitions().get(3).getTags().contains("small10"));


                            malabaricus =
                                    ((HeterogeneousGearFactory) ((GarbageGearFactory) ((HoldLimitingDecoratorFactory) flexible.getFisherDefinitions().get(
                                            3).getGear()).getDelegate()).getDelegate()).getGears().get("Lutjanus malabaricus");

                            ((LogisticSelectivityGearFactory) malabaricus).setSelectivityAParameter(
                                    new FixedDoubleParameter(
                                            ((FixedDoubleParameter) ((LogisticSelectivityGearFactory) malabaricus).getSelectivityAParameter()).getFixedValue()
                                                    * finalIncrease
                                    )
                            );
                        }





                    }
            );


            runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                @Override
                public void consume(StringBuffer writer, FishState model, Integer year) {
                    writer.append(finalIncrease).append(",");
                }
            });


            //while (runner.getRunsDone() < 1) {
            for(int i = 0; i< RUNS_PER_POLICY; i++) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
            }
        }
        fileWriter.close();
    }




    public static void enforcement(
            String name,
            String cheatingTag, final String filename) throws IOException {

        FileWriter fileWriter = new FileWriter(Paths.get(DIRECTORY, filename + "_"+name+"_enforcement.csv").toFile());
        fileWriter.write("run,year,enforcement,policy,variable,value\n");
        fileWriter.flush();

        for(int maxDaysOut=200; maxDaysOut>=50; maxDaysOut-=10) {
            for(double probabilityOfCheating = 0; probabilityOfCheating<=1; probabilityOfCheating+=.2) {

                probabilityOfCheating = FishStateUtilities.round(probabilityOfCheating);
                BatchRunner runner = setupRunner(filename, 15,POPULATIONS);


                int finalMaxDaysOut = maxDaysOut;

                //basically we want year 4 to change big boats regulations.
                //because I coded "run" poorly, we have to go through this series of pirouettes
                //to get it done right
                double finalProbabilityOfCheating = probabilityOfCheating;
                runner.setScenarioSetup(
                        scenario -> {

                            //at year 4, impose regulation
                            FlexibleScenario flexible = (FlexibleScenario) scenario;
                            flexible.getPlugins().add(
                                    fishState -> new AdditionalStartable() {
                                        @Override
                                        public void start(FishState model) {

                                            model.scheduleOnceAtTheBeginningOfYear(
                                                    (Steppable) simState -> {
                                                        fisherloop:
                                                        for (Fisher fisher :
                                                                ((FishState) simState).getFishers()) {

                                                            if (!fisher.getTags().contains(cheatingTag)) {
                                                                fisher.setRegulation(
                                                                        new FishingSeason(true, finalMaxDaysOut));
                                                            } else {
                                                                if (!model.getRandom().nextBoolean(
                                                                        finalProbabilityOfCheating))
                                                                    fisher.setRegulation(
                                                                            new FishingSeason(true, finalMaxDaysOut));

                                                            }


                                                        }
                                                    },
                                                    StepOrder.DAWN,
                                                    4
                                            );


                                        }

                                        @Override
                                        public void turnOff() {

                                        }
                                    }
                            );

                        }
                );


                final String cheatingString = Double.toString(probabilityOfCheating);
                runner.setColumnModifier(new BatchRunner.ColumnModifier() {
                    @Override
                    public void consume(StringBuffer writer, FishState model, Integer year) {
                        writer.append(cheatingString).append(",").append(finalMaxDaysOut).append(",");
                    }
                });


                //while (runner.getRunsDone() < 1) {
                StringBuffer tidy = new StringBuffer();
                runner.run(tidy);
                fileWriter.write(tidy.toString());
                fileWriter.flush();
                //   }
            }
        }
        fileWriter.close();
    }
}
