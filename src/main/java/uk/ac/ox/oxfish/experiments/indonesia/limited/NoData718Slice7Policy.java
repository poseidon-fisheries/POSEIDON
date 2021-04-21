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
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

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




    private static LinkedHashMap<String, Function<Integer,Consumer<Scenario>>> simpleSelectivityShift =
            new LinkedHashMap<>();
    static {

        for(int cmShift=0; cmShift<16; cmShift=cmShift+2) {
            int finalCmShift = cmShift;
            simpleSelectivityShift.put("cmshift_allboats_allspecies_" + cmShift,
                    selectivityShiftSimulations(finalCmShift, 2)

            );
            simpleSelectivityShift.put("cmshift_faroff_allspecies_" + cmShift,
                    selectivityShiftSimulations(finalCmShift, 2)

            );
        }


    }

    @NotNull
    private static Function<Integer, Consumer<Scenario>> selectivityShiftSimulations(int finalCmShift, final int howManyPopulations) {
        return yearOfShock -> scenario -> {
            final FlexibleScenario cast = ((FlexibleScenario) scenario);
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
                                            individualLogistic.setSelexParameter1(
                                                    new FixedDoubleParameter(
                                                            ((FixedDoubleParameter) individualLogistic.getSelexParameter1()).getFixedValue() + finalCmShift
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
                                                        System.out.println("Changing gear!");
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


    private static Path OUTPUT_FOLDER =

            NoData718Slice7Calibration.MAIN_DIRECTORY.resolve("ga_lowmk_scenarios").resolve("selectivityshift");

    private static LinkedHashMap<String, Function<Integer, Consumer<Scenario>>> simulatedPolicies =
            simpleSelectivityShift;
           // NoData718Utilities.simpleSelectivityShift;


    public static void main(String[] args) throws IOException {

        runPolicyDirectory(
                CANDIDATES_CSV_FILE.toFile(),
                OUTPUT_FOLDER,
                simulatedPolicies,
                ADDITIONAL_COLUMNS);


    }

}
