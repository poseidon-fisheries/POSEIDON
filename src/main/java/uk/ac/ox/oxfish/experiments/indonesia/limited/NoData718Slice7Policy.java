/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2021  CoHESyS Lab cohesys.lab@gmail.com
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
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.FishingMortalityAgent;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticSimpleFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.MaximumOfFilters;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static uk.ac.ox.oxfish.experiments.indonesia.limited.NoData718Slice6Policy.runPolicyDirectory;

public class NoData718Slice7Policy {

    public static final Path CANDIDATES_CSV_FILE =
            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("successes_lowmk_ga.csv");
    public static final int SEED = 0;
    private static final int ADDITIONAL_YEARS_TO_RUN = 30;


    private static final LinkedList<String> ADDITIONAL_COLUMNS =
            new LinkedList<>();
    static {
//        ADDITIONAL_COLUMNS.add( "SPR Lutjanus malabaricus spr_agent_forpolicy");
//        ADDITIONAL_COLUMNS.add( "Mean Length Caught Lutjanus malabaricus spr_agent_forpolicy");
//        ADDITIONAL_COLUMNS.add( "CPUE Lutjanus malabaricus spr_agent_forpolicy");
        //  ADDITIONAL_COLUMNS.add( "M/K ratio Lutjanus malabaricus spr_agent_forpolicy");
        // ADDITIONAL_COLUMNS.add("LoptEffortPolicy output");
//        ADDITIONAL_COLUMNS.add("LBSPREffortPolicy output");
        //need to add a lot of multidens collectors here....
        // String species = "Pristipomoides multidens";
        for(String species : NoData718Slice2PriceIncrease.speciesToSprAgent.keySet()){
            final String agent = NoData718Slice2PriceIncrease.speciesToSprAgent.get(species);
            Preconditions.checkNotNull(agent, "species has no agent!");
            // ADDITIONAL_COLUMNS.add("SPR " + species + " " + agent + "_small");
            ADDITIONAL_COLUMNS.add("Exogenous catches of " + species);

            //ADDITIONAL_COLUMNS.add("SPR " + "Lutjanus malabaricus" + " " + "spr_agent" + "_total_and_correct");
            ADDITIONAL_COLUMNS.add("SPR " + species + " " + agent);
            ADDITIONAL_COLUMNS.add("Biomass " + species);
            ADDITIONAL_COLUMNS.add("Bt/K " + species);
            ADDITIONAL_COLUMNS.add("Percentage Mature Catches " + species + " " + agent);
            ADDITIONAL_COLUMNS.add("Percentage Lopt Catches " + species + " " + agent);
            ADDITIONAL_COLUMNS.add("Mean Length Caught " + species + " " + agent);
            ADDITIONAL_COLUMNS.add("CPUE " + species + " " + agent);
            ADDITIONAL_COLUMNS.add(species + " Earnings");
            ADDITIONAL_COLUMNS.add(species + " Landings");
            ADDITIONAL_COLUMNS.add("Average Daily Fishing Mortality " + species);
            ADDITIONAL_COLUMNS.add("Yearly Fishing Mortality " + species);
        }

    }

    private static LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> majorPolicies = new LinkedHashMap<>();
    static {
        for (int cmShift : new int[]{6, 8}) {
            majorPolicies.put("cmshift_reallyallboats_allspecies_" + cmShift,
                    selectivityShiftSimulations(cmShift, 3, 1.0)

            );
        }
        majorPolicies.put("BAU",addSPRAgents());


        majorPolicies.put(
                "noentry",
                shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear,
                        new String[]{"population0", "population1", "population2"}
                        , 999, false).andThen(
                        NoDataPolicy.removeEntry(shockYear)
                )

        );

        int daysToTry[] = new int[]{250,200,150,100,50,25,0};
        for(int days : daysToTry) {
            int finalDays = days;
            majorPolicies.put(
                    days+"_days_noentry",
                    shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear,
                            new String[]{"population0", "population1", "population2"}
                            , finalDays, false).andThen(
                            NoDataPolicy.removeEntry(shockYear)
                    )

            );
        }
        for(int days : new int[]{200,100,   50}) {
            int finalDays = days;
            majorPolicies.put(
                    days+"_days_yesentry",
                    shockYear -> NoDataPolicy.buildMaxDaysRegulation(shockYear,
                            new String[]{"population0", "population1", "population2"}
                            , finalDays, false)
            );

        }
        final LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> lbsprs =
                NoData718Utilities.buildLBSPRPolicies(false, true);
        majorPolicies.putAll(lbsprs);

    }


    private final static int[] validShifts = new int[]{12,0,8,6,2,20};

    private static LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> simpleSelectivityShift =
            new LinkedHashMap<>();
    static {

        for(int cmShift:validShifts) {
            simpleSelectivityShift.put("cmshift_reallyallboats_allspecies_" + cmShift,
                    selectivityShiftSimulations(cmShift, 3, 1.0)

            );

            simpleSelectivityShift.put("cmshift_allboats_allspecies_" + cmShift,
                    selectivityShiftSimulations(cmShift, 2, 1.0)

            );
//            simpleSelectivityShift.put("cmshift_faroff_allspecies_" + cmShift,
//                    selectivityShiftSimulations(cmShift, 1)
//
//            );
        }


    }


    private static LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> percentageSelectivityShift =
            new LinkedHashMap<>();
    static {
        int cmShift = 6;
        for(double takeupRatio : new double[]{1.0,0.9,0.8,0.7,0.5,0.25,0}) {
            percentageSelectivityShift.put("cmshift_reallyallboats_allspecies_" + cmShift +"_adoption_" + takeupRatio,
                    selectivityShiftSimulations(cmShift, 3, takeupRatio)

            );

        }


    }

    @NotNull
    private static Function<Integer, Consumer<Scenario>> selectivityShiftSimulations(int finalCmShift, final int howManyPopulations,
                                                                                     double probabilityOfAdoptingGear) {
        return yearOfShock -> scenario -> {
            final FlexibleScenario cast = ((FlexibleScenario) scenario);
            //add some more interesting trackers
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
                            "      surveyTag: spr_agent_agent_onlylongliners\n" +
                            "      useTNCFormula: " + true + "\n" +
                            "      tagsToSample:\n" +
                            "        population0: " + 100 + "\n" +
                            "        population1: " + 100 + "\n" +
                            "        population2: 0";
            FishYAML yaml = new FishYAML();
            cast.getPlugins().add(yaml.loadAs(sprAgent,
                    AlgorithmFactory.class));
            //
            sprAgent =
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
                            "      surveyTag: spr_agent_agent_onlygillnetters\n" +
                            "      useTNCFormula: " + true + "\n" +
                            "      tagsToSample:\n" +
                            "        population0: " + 0 + "\n" +
                            "        population1: " + 0 + "\n" +
                            "        population2: 100";
            cast.getPlugins().add(yaml.loadAs(sprAgent,
                    AlgorithmFactory.class));

            //now add stuff

            cast.getPlugins().add(
                    fishState -> model -> fishState.scheduleOnceInXDays(
                            new Steppable() {
                                @Override
                                public void step(SimState simState) {

                                    for (int populations = 0; populations < howManyPopulations; populations++) {
                                        //weird but effective way to copy
                                        final AlgorithmFactory<? extends Gear> gearFactory =
                                                cast.getFisherDefinitions().get(populations).getGear();


                                        final HeterogeneousGearFactory heterogeneousGear = (HeterogeneousGearFactory) ((GarbageGearFactory) (
                                                ((DelayGearDecoratorFactory) gearFactory).getDelegate())).getDelegate();
                                        for (Map.Entry<String, HomogeneousGearFactory> individualGear : heterogeneousGear.gears.entrySet()) {

                                            //don't bother with fixed proportion
                                            if(individualGear.getValue() instanceof FixedProportionHomogeneousGearFactory)
                                                continue;
                                            //if it is domed, shift it easily
                                            if(individualGear.getValue() instanceof SimpleDomeShapedGearFactory)
                                            {
                                                final SimpleDomeShapedGearFactory domed = (SimpleDomeShapedGearFactory) individualGear.getValue();
                                                domed.setLengthFullSelectivity( new FixedDoubleParameter(
                                                                ((FixedDoubleParameter) domed.getLengthFullSelectivity()).getFixedValue() + finalCmShift
                                                        )
                                                );
                                                System.out.println("dome selectivity shifted by " + finalCmShift);
                                            }
                                            else {
                                                final SimpleLogisticGearFactory individualLogistic = (SimpleLogisticGearFactory) individualGear.getValue();


                                                System.out.println(individualGear.getKey() + ": " +
                                                        ((FixedDoubleParameter) individualLogistic.getSelexParameter1()).getFixedValue() + "--->" +
                                                        (((FixedDoubleParameter) individualLogistic.getSelexParameter1()).getFixedValue() +
                                                                (finalCmShift * ((FixedDoubleParameter) individualLogistic.getSelexParameter2()).getFixedValue()))

                                                );


                                                individualLogistic.setSelexParameter1(
                                                        new FixedDoubleParameter(
                                                                ((FixedDoubleParameter) individualLogistic.getSelexParameter1()).getFixedValue() +
                                                                        (finalCmShift * ((FixedDoubleParameter) individualLogistic.getSelexParameter2()).getFixedValue())
                                                        )
                                                );
                                            }
                                        }

                                        //all new boats will use this
                                        final String populationTag = "population" + populations;
                                        final AlgorithmFactory<? extends Gear> currentGear = fishState.getFisherFactory(populationTag).getGear();
                                        fishState.getFisherFactory(populationTag).setGear(
                                                new AlgorithmFactory<Gear>() {
                                                    @Override
                                                    public Gear apply(FishState fishState) {
                                                        if(fishState.getRandom().nextDouble()<probabilityOfAdoptingGear){
                                                            return gearFactory.apply(fishState);
                                                        }
                                                        else
                                                            return currentGear.apply(fishState);


                                                    }
                                                }
                                        );
                                        //each boat has a fixed % chance of using this
                                        fishState.getFishers().stream().
                                                filter(fisher -> fisher.getTags().contains(populationTag)).forEach(
                                                new Consumer<Fisher>() {
                                                    @Override
                                                    public void accept(Fisher fisher) {
                                                        if(fishState.getRandom().nextDouble()<probabilityOfAdoptingGear) {
                                                            fisher.setGear(((DelayGearDecoratorFactory) gearFactory).apply(fishState));
                                                        }
                                                    }
                                                }
                                        );


                                    }


                                }
                            }

                            , StepOrder.DAWN, 365 * yearOfShock)
            );

        };
    }

    private static final int[] SPR_AGENT_NUMBERS = new int[]{1,2,3,5,10,20,50,100,9999};

    @NotNull
    private static Function<Integer, Consumer<Scenario>> addSPRAgents() {


        return yearOfShock -> scenario -> {
            final FlexibleScenario cast = ((FlexibleScenario) scenario);
            for (int numberOfAgents : SPR_AGENT_NUMBERS) {
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
                                "      surveyTag: spr_agent_agent" + numberOfAgents + "\n" +
                                "      useTNCFormula: " + true + "\n" +
                                "      tagsToSample:\n" +
                                "        population0: " + numberOfAgents + "\n" +
                                "        population1: " + numberOfAgents + "\n" +
                                "        population2: " + numberOfAgents;
                FishYAML yaml = new FishYAML();
                cast.getPlugins().add(yaml.loadAs(sprAgent,
                        AlgorithmFactory.class));

            }

        };
    }

    private static LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> checkSPRDifferentials = new LinkedHashMap<>();
    static {

        checkSPRDifferentials.put("BAU_observed",addSPRAgents());
        checkSPRDifferentials.put("5cmshifted_observed", new Function<Integer, Consumer<Scenario>>() {
            @Override
            public Consumer<Scenario> apply(Integer integer) {
                return selectivityShiftSimulations(5,2, 1.0).apply(integer).andThen(addSPRAgents().apply(0));
            }
        });



    }


    private static LinkedList<String> dailyColumnsToPrint = new LinkedList<>();
    static {
        dailyColumnsToPrint.add("Lutjanus malabaricus Landings");
        dailyColumnsToPrint.add("Other Landings");
        dailyColumnsToPrint.add("Pristipomoides multidens Landings");
        dailyColumnsToPrint.add("Lethrinus laticaudis Landings");
        dailyColumnsToPrint.add("Atrobucca brevis Landings");


    }



    public static void main(String[] args) throws IOException {

        final LinkedList<Pair<Integer, AlgorithmFactory<? extends AdditionalStartable>>> additionalPlugins = new LinkedList<>();
        //add fishing mortality per species!
        //we are going to have to trawl around in the fisher factories, ugh
        String[] species = new String[]{
                "Pristipomoides multidens",
                "Lethrinus laticaudis",
                "Lutjanus malabaricus",
                "Atrobucca brevis"
        };
        for (String speciesName : species) {

            final AdditionalStartable fMortalityAgent =
                    new AdditionalStartable() {

                        @Override
                        public void start(FishState model) {
                            List<LogisticSimpleFilter> fleetFilters = new LinkedList<>();
                            //go through all fisher factories
                            for (Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
                                final FisherFactory factory = fisherFactory.getValue();
                                final HeterogeneousGearFactory gear = ((HeterogeneousGearFactory) ((GarbageGearFactory) ((DelayGearDecoratorFactory) factory.getGear()).getDelegate()).getDelegate());
                                //if they do catch this species
                                if(gear.getGears().get(speciesName)!=null && gear.getGears().get(speciesName) instanceof SimpleLogisticGearFactory){
                                    final SimpleLogisticGearFactory logisticFactory = (SimpleLogisticGearFactory) gear.getGears().get(speciesName);
                                    //copy its parameters
                                    fleetFilters.add(new LogisticSimpleFilter(true,false,
                                            logisticFactory.getSelexParameter1().apply(model.getRandom()),
                                            logisticFactory.getSelexParameter2().apply(model.getRandom())
                                    ));

                                }
                            }

                            Species correctSpecies = model.getSpecies(speciesName);
                            MaximumOfFilters vulnerability = new MaximumOfFilters(Iterables.toArray(fleetFilters,LogisticSimpleFilter.class));
                            FishingMortalityAgent newAgent = new FishingMortalityAgent(vulnerability,correctSpecies,true);
                            model.registerStartable(newAgent);

                        }
                    };

            additionalPlugins.add(new Pair<>(1, fishState -> fMortalityAgent));
        }


        if(args[0].equals("selectivity")) {

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            for (String agents : new String[]{"spr_agent_agent_onlygillnetters","spr_agent_agent_onlylongliners"}) {
                columns.add( "SPR Lutjanus malabaricus " + agents);
                columns.add( "Mean Length Caught Lutjanus malabaricus " + agents);
                columns.add( "CPUE Lutjanus malabaricus " + agents);
                columns.add( "M/K ratio Lutjanus malabaricus " + agents);
                columns.add( "Percentage Mature Catches Lutjanus malabaricus " + agents);
                columns.add( "Percentage Lopt Catches Lutjanus malabaricus " + agents);
            }

            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("selectivityshift_again").toFile().mkdirs();
            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("selectivityshift_again"),
                    simpleSelectivityShift,
                    columns,
                    additionalPlugins, null);
        }
        else if(args[0].equals("selectivity_takeup")) {

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            for (String agents : new String[]{"spr_agent_agent_onlygillnetters","spr_agent_agent_onlylongliners"}) {
                columns.add( "SPR Lutjanus malabaricus " + agents);
                columns.add( "Mean Length Caught Lutjanus malabaricus " + agents);
                columns.add( "CPUE Lutjanus malabaricus " + agents);
                columns.add( "M/K ratio Lutjanus malabaricus " + agents);
                columns.add( "Percentage Mature Catches Lutjanus malabaricus " + agents);
                columns.add( "Percentage Lopt Catches Lutjanus malabaricus " + agents);
            }

            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("selectivityshift_percentage").toFile().mkdirs();
            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("selectivityshift_percentage"),
                    percentageSelectivityShift,
                    columns,
                    additionalPlugins, null);
        }
        else if(args[0].equals("sprnumbers")){

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            for (int numberOfAgents : SPR_AGENT_NUMBERS) {
                columns.add( "SPR Lutjanus malabaricus spr_agent" + numberOfAgents);
                columns.add( "Mean Length Caught Lutjanus malabaricus spr_agent" + numberOfAgents);
                columns.add( "CPUE Lutjanus malabaricus spr_agent" + numberOfAgents);
                columns.add( "M/K ratio Lutjanus malabaricus spr_agent" + numberOfAgents);
            }

            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("sprnumbers"),
                    checkSPRDifferentials,
                    columns,
                    additionalPlugins, null);
        }
        else if(args[0].equals("season_simple")){

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("season_simple").toFile().mkdirs();
            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("season_simple"),
                    NoData718Utilities.nonAdaptiveEffort,
                    columns,
                    additionalPlugins, dailyColumnsToPrint);
        }
        else if(args[0].equals("processor_simple")){

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("processor_simple").toFile().mkdirs();
            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("processor_simple"),
                    NoData718Utilities.processor_simple,
                    columns,
                    additionalPlugins, dailyColumnsToPrint);
        }
        else if(args[0].equals("major_policies")){

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("major_policies").toFile().mkdirs();
            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("major_policies"),
                    majorPolicies,
                    columns,
                    additionalPlugins, dailyColumnsToPrint);
        }
        else if(args[0].equals("days_at_sea")){

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("days_at_sea").toFile().mkdirs();
            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("days_at_sea"),
                    NoData718Utilities.daysAtSea,
                    columns,
                    additionalPlugins, null);
        }

    }

}
