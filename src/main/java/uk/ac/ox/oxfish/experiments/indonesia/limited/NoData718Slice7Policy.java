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
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.*;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.StepOrder;
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
        String species = "Pristipomoides multidens";
        final String agent = NoData718Slice2PriceIncrease.speciesToSprAgent.get(species);
        Preconditions.checkNotNull(agent, "species has no agent!");
        // ADDITIONAL_COLUMNS.add("SPR " + species + " " + agent + "_small");
        ADDITIONAL_COLUMNS.add("Exogenous catches of "+species);

        //ADDITIONAL_COLUMNS.add("SPR " + "Lutjanus malabaricus" + " " + "spr_agent" + "_total_and_correct");
        ADDITIONAL_COLUMNS.add("SPR " + species + " " + agent);
        ADDITIONAL_COLUMNS.add("Biomass " + species);
        ADDITIONAL_COLUMNS.add("Bt/K " + species);
        ADDITIONAL_COLUMNS.add("Percentage Mature Catches " + species + " "+ agent);
        ADDITIONAL_COLUMNS.add("Percentage Lopt Catches " + species + " "+ agent);
        ADDITIONAL_COLUMNS.add(species + " Earnings");
        ADDITIONAL_COLUMNS.add(species + " Landings");
    }



    private final static int[] validShifts = new int[]{20,12,8,6,2,0};

    private static LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> simpleSelectivityShift =
            new LinkedHashMap<>();
    static {

        for(int cmShift:validShifts) {
            int finalCmShift = cmShift;
            simpleSelectivityShift.put("cmshift_allboats_allspecies_" + cmShift,
                    selectivityShiftSimulations(finalCmShift, 2)

            );
            simpleSelectivityShift.put("cmshift_faroff_allspecies_" + cmShift,
                    selectivityShiftSimulations(finalCmShift, 1)

            );
        }


    }

    @NotNull
    private static Function<Integer, Consumer<Scenario>> selectivityShiftSimulations(int finalCmShift, final int howManyPopulations) {
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
                                            final SimpleLogisticGearFactory individualLogistic = (SimpleLogisticGearFactory) individualGear.getValue();

                                            System.out.println(individualGear.getKey() + ": " +
                                                    ((FixedDoubleParameter) individualLogistic.getSelexParameter1()).getFixedValue() + "--->" +
                                                    (((FixedDoubleParameter) individualLogistic.getSelexParameter1()).getFixedValue() +
                                                            (finalCmShift * ((FixedDoubleParameter) individualLogistic.getSelexParameter2()).getFixedValue() ))

                                                    );


                                            individualLogistic.setSelexParameter1(
                                                    new FixedDoubleParameter(
                                                            ((FixedDoubleParameter) individualLogistic.getSelexParameter1()).getFixedValue() +
                                                                    (finalCmShift * ((FixedDoubleParameter) individualLogistic.getSelexParameter2()).getFixedValue() )
                                                    )
                                            );
                                        }

                                        //all new boats will use this
                                        final String populationTag = "population" + populations;
                                        fishState.getFisherFactory(populationTag).setGear(
                                                gearFactory
                                        );
                                        //all old boats will use this
                                        fishState.getFishers().stream().
                                                filter(fisher -> fisher.getTags().contains(populationTag)).forEach(
                                                new Consumer<Fisher>() {
                                                    @Override
                                                    public void accept(Fisher fisher) {
                                                        fisher.setGear(((DelayGearDecoratorFactory) gearFactory).apply(fishState));
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

    private static final int[] SPR_AGENT_NUMBERS = new int[]{2,3,5,10,20,50,100,9999};

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
                                "        population2: 0";
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
                return selectivityShiftSimulations(5,2).apply(integer).andThen(addSPRAgents().apply(0));
            }
        });



    }


    private static Path OUTPUT_FOLDER =

            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("selectivityshift");

    private static LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> simulatedPolicies =
            simpleSelectivityShift;
    // NoData718Utilities.simpleSelectivityShift;




    public static void main(String[] args) throws IOException {

        if(args[0].equals("selectivity")) {

            final LinkedList<String> columns = new LinkedList<>(ADDITIONAL_COLUMNS);
            for (String agents : new String[]{"spr_agent_agent_onlygillnetters","spr_agent_agent_onlylongliners"}) {
                columns.add( "SPR Lutjanus malabaricus " + agents);
                columns.add( "Mean Length Caught Lutjanus malabaricus " + agents);
                columns.add( "CPUE Lutjanus malabaricus " + agents);
                columns.add( "M/K ratio Lutjanus malabaricus " + agents);
            }

            runPolicyDirectory(
                    CANDIDATES_CSV_FILE.toFile(),
                    NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("selectivityshift"),
                    simpleSelectivityShift,
                    ADDITIONAL_COLUMNS,
                    new LinkedList<Pair<Integer, AlgorithmFactory<? extends AdditionalStartable>>>());
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
                    new LinkedList<Pair<Integer, AlgorithmFactory<? extends AdditionalStartable>>>());
        }

    }

}
