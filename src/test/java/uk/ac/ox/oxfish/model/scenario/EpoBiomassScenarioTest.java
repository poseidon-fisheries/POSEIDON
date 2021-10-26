/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.scenario;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.BiomassPurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear.FadRefillGearStrategyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.FishStateYearlyTimeSeries;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

public class EpoBiomassScenarioTest {

    @Test
    public void canSaveToAndLoadFromYamlWithoutCrashing() {
        final EpoBiomassScenario scenario = new EpoBiomassScenario();
        final FishYAML yaml = new FishYAML();
        final String output = yaml.dump(scenario);
        System.out.println(output);
        final EpoBiomassScenario scenario2 = yaml.loadAs(output, EpoBiomassScenario.class);
        assertNotNull(scenario2);
    }

    @Test
    public void noFishGetsCaughtAndThrownOverboardImmediately() {

        final EpoBiomassScenario scenario = new EpoBiomassScenario();
        final Path testInputsPath = EpoScenario.INPUT_PATH.resolve("test");
        scenario.setCostsFile(testInputsPath.resolve("no_costs.csv"));
        scenario.setBoatsFile(testInputsPath.resolve("dummy_boats.csv"));
        scenario.getFadMapFactory().setCurrentFiles(ImmutableMap.of());

        final FisherDefinition fisherDefinition = scenario.getFisherDefinition();
        final GravityDestinationStrategyFactory gravityDestinationStrategyFactory =
            (GravityDestinationStrategyFactory) fisherDefinition.getDestinationStrategy();
        gravityDestinationStrategyFactory
            .setMaxTripDurationFile(testInputsPath.resolve("dummy_boats.csv"));
        gravityDestinationStrategyFactory
            .setAttractionWeightsFile(testInputsPath.resolve("dummy_action_weights.csv"));

        ((PurseSeinerBiomassFishingStrategyFactory) fisherDefinition.getFishingStrategy())
            .setAttractionWeightsFile(testInputsPath.resolve("dummy_action_weights.csv"));

        //noinspection OverlyStrongTypeCast
        ((BiomassPurseSeineGearFactory) fisherDefinition.getGear())
            .setLocationValuesFile(testInputsPath.resolve("dummy_location_values.csv"));
        ((FadRefillGearStrategyFactory) fisherDefinition.getGearStrategy())
            .setMaxFadDeploymentsFile(testInputsPath.resolve("dummy_max_deployments.csv"));

        final Regulation regulation = new FishingSeason(true, 100);
        scenario.addPlugin(state -> model -> {
                model.getFishers().forEach(fisher -> fisher.setRegulation(regulation));
                state.scheduleEveryYear(simState -> {
                    final FishStateYearlyTimeSeries yearlyDataSet =
                        ((FishState) simState).getYearlyDataSet();
                    final double catches =
                        yearlyDataSet.getColumn("Skipjack tuna Catches (kg)").getLatest();
                    final double landings =
                        yearlyDataSet.getColumn("Skipjack tuna Landings").getLatest();
                    System.out.printf("Catches:    %.2f%n", catches);
                    System.out.printf("Landings:   %.2f%n", landings);
                    System.out.printf("Difference: %.2f%n", Math.abs(catches - landings));
                    assertTrue(catches > 1000);
                    assertTrue(landings > 1000);
                    assertTrue(Math.abs(catches - landings) < EPSILON);
                }, StepOrder.AFTER_DATA);
            }
        );

        final FishState state = new FishState();
        state.setScenario(scenario);

        state.start();
        while (state.getYear() < 5) {
            state.schedule.step(state);
        }
        state.schedule.step(state);
    }

}