/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.indonesia.limited;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.SPRAgentBuilder;
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesAbundanceFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBoxcarFactory;
import uk.ac.ox.oxfish.experiments.indonesia.Slice6Sweeps;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketProxy;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoData718Utilities {


        static final AlgorithmFactory<? extends AdditionalStartable>  CORRECT_LIFE_HISTORIES_CONSUMER(Scenario scenario) {

                final FlexibleScenario flexible = (FlexibleScenario) scenario;
                final SingleSpeciesBoxcarFactory malabaricus = (SingleSpeciesBoxcarFactory) ((MultipleIndependentSpeciesAbundanceFactory) flexible.getBiologyInitializer()).getFactories().
                        get(1);
                Preconditions.checkArgument(malabaricus.getSpeciesName().equals("Lutjanus malabaricus"));
                SPRAgentBuilder builder = new SPRAgentBuilder();
                builder.setAssumedKParameter(malabaricus.getK().makeCopy());
                builder.setAssumedLengthAtMaturity(malabaricus.getLengthAtMaturity().makeCopy());
                builder.setAssumedLinf(malabaricus.getLInfinity().makeCopy());
                builder.setAssumedNaturalMortality(malabaricus.getYearlyMortality().makeCopy());
                builder.setAssumedVarA(malabaricus.getAllometricAlpha().makeCopy());
                builder.setAssumedVarB(malabaricus.getAllometricBeta().makeCopy());

                builder.setSpeciesName("Lutjanus malabaricus");
                builder.setSurveyTag("total_and_correct");
                builder.setProbabilityOfSamplingEachBoat(new FixedDoubleParameter(1));

                ((FlexibleScenario) scenario).getPlugins().add(builder);


                return builder;
        }

        static public LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> onlyBAU = new LinkedHashMap();

        static {

                onlyBAU.put(
                        "BAU",
                        shockYear -> scenario -> {
                        }

                );
        }

        static public LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policies = new LinkedHashMap();


        private static Function<Integer,Consumer<Scenario>> decreasePricesForAllSpeciesByAPercentage(double taxRate) {

                return new Function<Integer, Consumer<Scenario>>() {
                        public Consumer<Scenario> apply(Integer shockYear) {


                                return new Consumer<Scenario>() {

                                        @Override
                                        public void accept(Scenario scenario) {

                                                ((FlexibleScenario) scenario).getPlugins().add(
                                                        new AlgorithmFactory<AdditionalStartable>() {
                                                                @Override
                                                                public AdditionalStartable apply(FishState state) {

                                                                        return new AdditionalStartable() {
                                                                                @Override
                                                                                public void start(FishState model) {

                                                                                        model.scheduleOnceAtTheBeginningOfYear(
                                                                                                new Steppable() {
                                                                                                        @Override
                                                                                                        public void step(SimState simState) {

                                                                                                                //shock the prices
                                                                                                                for (Port port : ((FishState) simState).getPorts()) {
                                                                                                                        for (Market market : port.getDefaultMarketMap().getMarkets()) {

                                                                                                                                if(port.getName().equals("Port 0")) {
                                                                                                                                        final FixedPriceMarket delegate = (FixedPriceMarket) ((MarketProxy) market).getDelegate();
                                                                                                                                        delegate.setPrice(
                                                                                                                                                delegate.getPrice() * (1 - taxRate)
                                                                                                                                        );
                                                                                                                                }
                                                                                                                                else {

                                                                                                                                        final FixedPriceMarket delegate = ((FixedPriceMarket) ((MarketProxy) ((MarketProxy) market).getDelegate()).getDelegate());
                                                                                                                                        delegate.setPrice(
                                                                                                                                                delegate.getPrice() * (1 - taxRate)
                                                                                                                                        );
                                                                                                                                }
                                                                                                                        }
                                                                                                                }

                                                                                                        }
                                                                                                }, StepOrder.DAWN, shockYear);

                                                                                }
                                                                        };


                                                                }
                                                        });


                                        }
                                };
                        }

                        ;

                };
        }



        static {


                for(double yearlyReduction = .01; yearlyReduction<=.05; yearlyReduction= FishStateUtilities.round5(yearlyReduction+.01)) {
                        double finalYearlyReduction = yearlyReduction;
                        policies.put(
                                yearlyReduction+"_yearlyReduction_noentry",
                                shockYear -> Slice6Sweeps.setupFleetReductionConsumer(
                                        shockYear,
                                        finalYearlyReduction
                                ).andThen(
                                        NoDataPolicy.removeEntry(shockYear)
                                )

                        );
                }

                policies.put(
                        "BAU",
                        shockYear -> scenario -> {
                        }

                );


                policies.put(
                        "noentry",
                        shockYear -> NoDataPolicy.removeEntry(shockYear)

                );


                for(int days = 250; days>=100; days-=10) {
                        int finalDays = days;
                        policies.put(
                                days+"_days_noentry",
                                shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear,
                                        new String[]{"population0", "population1", "population2"}
                                        , finalDays).andThen(
                                        NoDataPolicy.removeEntry(shockYear)
                                )

                        );
                }



                policies.put(
                        "tax_20",
                        shockYear -> NoDataPolicy.removeEntry(shockYear).andThen(
                                decreasePricesForAllSpeciesByAPercentage(.2d).apply(shockYear)
                        )

                );


        }


        private static Consumer<Scenario> protectBestCell(int shockYear){
                return new Consumer<Scenario>() {
                        @Override
                        public void accept(Scenario scenario) {
                                ((FlexibleScenario) scenario).getPlugins().add(
                                        new AlgorithmFactory<AdditionalStartable>() {
                                                @Override
                                                public AdditionalStartable apply(FishState state) {

                                                        return new AdditionalStartable() {
                                                                @Override
                                                                public void start(FishState model) {

                                                                        model.scheduleOnceAtTheBeginningOfYear(
                                                                                new Steppable() {
                                                                                        @Override
                                                                                        public void step(SimState simState) {

//go through all possible tiles; find the one that has now the most atrobucca
//protected it!
                                                                                                final FishState model = (FishState) simState;
                                                                                                final Species brevis = model.getSpecies("Atrobucca Brevis");
                                                                                                final SeaTile toProtect = model.getMap().getAllSeaTilesExcludingLandAsList().stream().max(
                                                                                                        new Comparator<SeaTile>() {
                                                                                                                @Override
                                                                                                                public int compare(SeaTile thisTile,
                                                                                                                                   SeaTile thatTile) {
                                                                                                                        return Double.compare(
                                                                                                                                thisTile.getBiomass(brevis),
                                                                                                                                thatTile.getBiomass(brevis)

                                                                                                                        );
                                                                                                                }
                                                                                                        }
                                                                                                ).get();
                                                                                                toProtect.assignMpa(NauticalMap.MPA_SINGLETON);

                                                                                                //now go through all fishers and make them follow MPA
                                                                                                for (Fisher fisher : model.getFishers()) {
                                                                                                        fisher.setRegulation(new ProtectedAreasOnly());
                                                                                                }
                                                                                                for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                                                                                                        fisherFactory.getValue().setRegulations(
                                                                                                                new ProtectedAreasOnlyFactory()
                                                                                                        );
                                                                                                }

                                                                                        }
                                                                                }, StepOrder.DAWN, shockYear);

                                                                }
                                                        };


                                                }
                                        });
                        }
                };
        }

        static public LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> policiesMPA = new LinkedHashMap();

        static{
                policiesMPA.put(
                        "BAU",
                        shockYear -> scenario -> {
                        }

                );

                policiesMPA.put(
                        "MPA_entry",
                        shockYear -> protectBestCell(shockYear)


                );


                policiesMPA.put(
                        "MPA_noentry",
                        shockYear -> protectBestCell(shockYear).andThen(NoDataPolicy.removeEntry(shockYear))


                );


                for(int days = 250; days>=100; days-=10) {
                        int finalDays = days;
                        policiesMPA.put(
                                days+"_days_MPA_noentry",
                                //max days regulations include respect protected areas so it works if put in this order
                                shockYear ->  protectBestCell(shockYear).andThen(NoDataPolicy.buildMaxDaysRegulation(shockYear,
                                        new String[]{"population0", "population1", "population2"}
                                        , finalDays).andThen(
                                        NoDataPolicy.removeEntry(shockYear)
                                        )
                                )

                        );
                }





        }




        static public LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> lbsprMsePolicies = buildLBSPRPolicies(false);

        static private LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> buildLBSPRPolicies(boolean blockEntryWhenSeasonIsNotFull){

                LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> toReturn = new LinkedHashMap<>();
                double[] guessedMSE = new double[]{0.6, 0.8, 1, 1.2, 1.5, 2};


                FishYAML fishYAML = new FishYAML();

                for (double mkRatio : guessedMSE) {
                        for (boolean includeGillnetters : new boolean[]{true, false}) {
                                for (boolean useTncFormula : new boolean[]{false,true}) {


                                        String sprAgent =
                                                "SPR Fixed Sample Agent:\n" +
                                                        "      assumedKParameter: '" + 0.3775984 / mkRatio + "'\n" +
                                                        "      assumedLengthAtMaturity: '50.0'\n" +
                                                        "      assumedLengthBinCm: '5.0'\n" +
                                                        "      assumedLinf: '86.0'\n" +
                                                        "      assumedNaturalMortality: '0.3775984'\n" +
                                                        "      assumedVarA: '0.00853'\n" +
                                                        "      assumedVarB: '3.137'\n" +
                                                        "      simulatedMaxAge: '100.0'\n" +
                                                        "      simulatedVirginRecruits: '1000.0'\n" +
                                                        "      speciesName: Lutjanus malabaricus\n" +
                                                        "      surveyTag: spr_agent_forpolicy\n" +
                                                        "      useTNCFormula: " + useTncFormula + "\n" +
                                                        "      tagsToSample:\n" +
                                                        "        population0: 16\n" +
                                                        "        population1: 16\n" +
                                                        (includeGillnetters ? "        population2: 4" : "        population2: 0");

                                        Function<Integer, Consumer<Scenario>> policy =
                                                new Function<Integer, Consumer<Scenario>>() {
                                                        @Override
                                                        public Consumer<Scenario> apply(Integer shockYear) {
                                                                return new Consumer<Scenario>() {
                                                                        @Override
                                                                        public void accept(Scenario scenario) {

                                                                                //add sensor
                                                                                ((FlexibleScenario) scenario).getPlugins().
                                                                                        add(fishYAML.loadAs(
                                                                                                sprAgent,
                                                                                                AlgorithmFactory.class));
                                                                                //add policy
                                                                                String policyString =
                                                                                        "LBSPR Effort Controller:\n" +
                                                                                                "      cubicParameter: '0.3'\n" +
                                                                                                "      linearParameter: '0.05'\n" +
                                                                                                "      maxChangeEachYear: '0.1'\n" +
                                                                                                "      sprColumnName: SPR Lutjanus malabaricus spr_agent_forpolicy\n" +
                                                                                                "      sprTarget: '0.4'\n" +
                                                                                                "      blockEntryWhenSeasonIsNotFull: " + blockEntryWhenSeasonIsNotFull + "\n" +
                                                                                                "      startingYear: " + shockYear;

                                                                                //add policy
                                                                                ((FlexibleScenario) scenario).getPlugins().
                                                                                        add(fishYAML.loadAs(
                                                                                                policyString,
                                                                                                AlgorithmFactory.class));


                                                                        }
                                                                };
                                                        }
                                                };

                                        toReturn.put("lbspr_policy_mk" + mkRatio + "_gill" + includeGillnetters + "_tncformula" + useTncFormula,
                                                policy);
                                }
                        }
                }

                toReturn.put(
                        "BAU",
                        //bau will also have the new SPR agent to (i) avoid scheduling discrepancies (ii) fill in that column for later printing
                        new Function<Integer, Consumer<Scenario>>() {
                                @Override
                                public Consumer<Scenario> apply(Integer integer) {
                                        return new Consumer<Scenario>() {
                                                @Override
                                                public void accept(Scenario scenario) {
                                                        String sprAgent =
                                                                "SPR Fixed Sample Agent:\n" +
                                                                        "      assumedKParameter: '" + 0.3775984 + "'\n" +
                                                                        "      assumedLengthAtMaturity: '50.0'\n" +
                                                                        "      assumedLengthBinCm: '5.0'\n" +
                                                                        "      assumedLinf: '86.0'\n" +
                                                                        "      assumedNaturalMortality: '0.3775984'\n" +
                                                                        "      assumedVarA: '0.00853'\n" +
                                                                        "      assumedVarB: '3.137'\n" +
                                                                        "      simulatedMaxAge: '100.0'\n" +
                                                                        "      simulatedVirginRecruits: '1000.0'\n" +
                                                                        "      speciesName: Lutjanus malabaricus\n" +
                                                                        "      surveyTag: spr_agent_forpolicy\n" +
                                                                        "      useTNCFormula: " + true + "\n" +
                                                                        "      tagsToSample:\n" +
                                                                        "        population0: 16\n" +
                                                                        "        population1: 16\n" +
                                                                        "        population2: 0";
                                                        FishYAML yaml = new FishYAML();
                                                        ((FlexibleScenario) scenario).getPlugins().add(yaml.loadAs(sprAgent,
                                                                AlgorithmFactory.class));

                                                        //fill in another column so that there is something to print out
                                                        ((FlexibleScenario) scenario).getPlugins().add(new AlgorithmFactory<AdditionalStartable>() {
                                                                @Override
                                                                public AdditionalStartable apply(FishState fishState) {

                                                                        return new AdditionalStartable() {
                                                                                @Override
                                                                                public void start(FishState model) {
                                                                                        model.getYearlyDataSet().registerGatherer("LBSPREffortPolicy output",
                                                                                                new Gatherer<FishState>() {
                                                                                                        @Override
                                                                                                        public Double apply(FishState fishState) {
                                                                                                                return 1d;
                                                                                                        }
                                                                                                },1);
                                                                                }
                                                                        };
                                                                }
                                                        });
                                                }
                                        };

                                }
                        }

                );
                return toReturn;

        }



        static public LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> loptMsePolicies = buildLoptPolicies(true);


        static private LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> buildLoptPolicies(boolean blockEntryWhenSeasonIsNotFull){

                LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> toReturn = new LinkedHashMap<>();
                double[] guessedMK = new double[]{0.6, 0.8, 1, 1.2, 1.5, 2};


                FishYAML fishYAML = new FishYAML();

                for (double mkRatio : guessedMK) {
                        for (boolean includeGillnetters : new boolean[]{true, false}) {
                         //       for (boolean useTncFormula : new boolean[]{false,true}) {


                                        String sprAgent =
                                                "SPR Fixed Sample Agent:\n" +
                                                        "      assumedKParameter: '" +  0.3775984 + "'\n" +
                                                        "      assumedLengthAtMaturity: '50.0'\n" +
                                                        "      assumedLengthBinCm: '5.0'\n" +
                                                        "      assumedLinf: '86.0'\n" +
                                                        "      assumedNaturalMortality: '0.3775984'\n" +
                                                        "      assumedVarA: '0.00853'\n" +
                                                        "      assumedVarB: '3.137'\n" +
                                                        "      simulatedMaxAge: '100.0'\n" +
                                                        "      simulatedVirginRecruits: '1000.0'\n" +
                                                        "      speciesName: Lutjanus malabaricus\n" +
                                                        "      surveyTag: spr_agent_forpolicy\n" +
                                                        "      tagsToSample:\n" +
                                                        "        population0: 16\n" +
                                                        "        population1: 16\n" +
                                                        (includeGillnetters ? "        population2: 4" : "        population2: 0");

                                        Function<Integer, Consumer<Scenario>> policy =
                                                new Function<Integer, Consumer<Scenario>>() {
                                                        @Override
                                                        public Consumer<Scenario> apply(Integer shockYear) {
                                                                return new Consumer<Scenario>() {
                                                                        @Override
                                                                        public void accept(Scenario scenario) {

                                                                                //add sensor
                                                                                ((FlexibleScenario) scenario).getPlugins().
                                                                                        add(fishYAML.loadAs(
                                                                                                sprAgent,
                                                                                                AlgorithmFactory.class));
                                                                                //add policy
                                                                                String policyString =
                                                                                        "Lopt Effort Controller:\n" +
                                                                                                "      bufferValue: '0.9'\n" +
                                                                                                "      effortDefinition: 'season_hours_out'\n" +
                                                                                                "      howManyYearsToLookBackTo: '5'\n" +
                                                                                                "      meanLengthColumnName: Mean Length Caught Lutjanus malabaricus spr_agent_forpolicy\n" +
                                                                                                "      targetLength: " + (86d*3d)/(3d+mkRatio) +"\n" +
                                                                                                "      blockEntryWhenSeasonIsNotFull: " + blockEntryWhenSeasonIsNotFull + "\n" +
                                                                                                "      startingYear: " + shockYear;

                                                                                //add policy
                                                                                ((FlexibleScenario) scenario).getPlugins().
                                                                                        add(fishYAML.loadAs(
                                                                                                policyString,
                                                                                                AlgorithmFactory.class));


                                                                        }
                                                                };
                                                        }
                                                };

                                        toReturn.put("lopt_policy_mk" + mkRatio + "_gill" + includeGillnetters,
                                                policy);
                              //  }
                        }
                }

                toReturn.put(
                        "BAU",
                        //bau will also have the new SPR agent to (i) avoid scheduling discrepancies (ii) fill in that column for later printing
                        new Function<Integer, Consumer<Scenario>>() {
                                @Override
                                public Consumer<Scenario> apply(Integer integer) {
                                        return new Consumer<Scenario>() {
                                                @Override
                                                public void accept(Scenario scenario) {
                                                        String sprAgent =
                                                                "SPR Fixed Sample Agent:\n" +
                                                                        "      assumedKParameter: '" + 0.3775984 + "'\n" +
                                                                        "      assumedLengthAtMaturity: '50.0'\n" +
                                                                        "      assumedLengthBinCm: '5.0'\n" +
                                                                        "      assumedLinf: '86.0'\n" +
                                                                        "      assumedNaturalMortality: '0.3775984'\n" +
                                                                        "      assumedVarA: '0.00853'\n" +
                                                                        "      assumedVarB: '3.137'\n" +
                                                                        "      simulatedMaxAge: '100.0'\n" +
                                                                        "      simulatedVirginRecruits: '1000.0'\n" +
                                                                        "      speciesName: Lutjanus malabaricus\n" +
                                                                        "      surveyTag: spr_agent_forpolicy\n" +
                                                                        "      useTNCFormula: " + true + "\n" +
                                                                        "      tagsToSample:\n" +
                                                                        "        population0: 16\n" +
                                                                        "        population1: 16\n" +
                                                                        "        population2: 0";
                                                        FishYAML yaml = new FishYAML();
                                                        ((FlexibleScenario) scenario).getPlugins().add(yaml.loadAs(sprAgent,
                                                                AlgorithmFactory.class));

                                                        //fill in another column so that there is something to print out
                                                        ((FlexibleScenario) scenario).getPlugins().add(new AlgorithmFactory<AdditionalStartable>() {
                                                                @Override
                                                                public AdditionalStartable apply(FishState fishState) {

                                                                        return new AdditionalStartable() {
                                                                                @Override
                                                                                public void start(FishState model) {
                                                                                        model.getYearlyDataSet().registerGatherer("LoptEffortPolicy output",
                                                                                                new Gatherer<FishState>() {
                                                                                                        @Override
                                                                                                        public Double apply(FishState fishState) {
                                                                                                                return 1d;
                                                                                                        }
                                                                                                },1);
                                                                                }
                                                                        };
                                                                }
                                                        });
                                                }
                                        };

                                }
                        }

                );
                return toReturn;

        }


        static public LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> lbsprMsePoliciesNoEntry = buildLBSPRPolicies(true);



        static public LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> adaptivelbsprMsePolicies = new LinkedHashMap<>();
        static {
                FishYAML fishYAML = new FishYAML();

                for (boolean includeGillnetters : new boolean[]{true, false}) {

                        Function<Integer, Consumer<Scenario>> policy =
                                new Function<Integer, Consumer<Scenario>>() {
                                        @Override
                                        public Consumer<Scenario> apply(Integer shockYear) {
                                                return new Consumer<Scenario>() {
                                                        @Override
                                                        public void accept(Scenario scenario) {

                                                                String policyString =
                                                                        "LBSPR Effort Adaptive Controller:\n" +
                                                                                "  blockEntryWhenSeasonIsNotFull: true\n" +
                                                                                "  cpueHalfPeriod: '3.0'\n" +
                                                                                "  cubicParameter: '0.3'\n" +
                                                                                "  highestMKAllowed: '2.0'\n" +
                                                                                "  linearParameter: '0.05'\n" +
                                                                                "  lowerDiscrepancyThreshold: '-0.36'\n" +
                                                                                "  lowestMKAllowed: '0.4'\n" +
                                                                                "  maxChangeEachYear: '0.1'\n" +
                                                                                "  sprAgentDelegate:\n" +
                                                                                "    SPR Fixed Sample Agent:\n" +
                                                                                "      assumedKParameter: '" + 0.3775984 / 0.6 + "'\n" +
                                                                                "      assumedLengthAtMaturity: '50.0'\n" +
                                                                                "      assumedLengthBinCm: '5.0'\n" +
                                                                                "      assumedLinf: '86.0'\n" +
                                                                                "      assumedNaturalMortality: '0.3775984'\n" +
                                                                                "      assumedVarA: '0.00853'\n" +
                                                                                "      assumedVarB: '3.137'\n" +
                                                                                "      simulatedMaxAge: '100.0'\n" +
                                                                                "      simulatedVirginRecruits: '1000.0'\n" +
                                                                                "      speciesName: Lutjanus malabaricus\n" +
                                                                                "      surveyTag: spr_agent_forpolicy\n" +
                                                                                "      useTNCFormula: true" + "\n" +
                                                                                "      tagsToSample:\n" +
                                                                                "        population0: 16\n" +
                                                                                "        population1: 16\n" +
                                                                                (includeGillnetters ? "        population2: 4" : "        population2: 0") + "\n"+
                                                                                "  sprTarget: '0.4'\n" +
                                                                                "  startUpdatingMKAfterYear: '-1.0'\n" +
                                                                                "  startingYear: " + shockYear + '\n'+
                                                                                "  upperDiscrepancyThreshold: '-0.24'";


                                                                //add policy
                                                                ((FlexibleScenario) scenario).getPlugins().
                                                                        add(fishYAML.loadAs(
                                                                                policyString,
                                                                                AlgorithmFactory.class));


                                                        }
                                                };
                                        }
                                };


                        adaptivelbsprMsePolicies.put("adaptive_lbspr_policy_gill" + includeGillnetters,
                                policy);

                }

        }




}
