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

package uk.ac.ox.oxfish.biology.growers;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.PurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;

import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;

public class FadAwareLogisticGrowerTest {

    @Test
    public void jonLandings() {

        final TunaScenario scenario = new TunaScenario();
        scenario.setCostsFile(input("no_costs.csv"));
        scenario.setBoatsFile(input("dummy_boats.csv"));
        scenario.setAttractionWeightsFile(input("dummy_action_weights.csv"));
        final FisherDefinition fisherDefinition = scenario.getFisherDefinition();
        ((GravityDestinationStrategyFactory) fisherDefinition.getDestinationStrategy())
            .setMaxTripDurationFile(input("dummy_boats.csv"));
        ((PurseSeineGearFactory) fisherDefinition.getGear())
            .setLocationValuesFile(input("dummy_location_values.csv"));

        scenario.getExogenousCatchesFactory().setCatchesFile(Paths.get("inputs", "tests", "exogenous_catches.csv"));
        scenario.getFisherDefinition().setRegulation(new NoFishingFactory());

        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        final Supplier<Double> yellowfinBiomass =
            () -> state.getTotalBiomass(state.getBiology().getSpecie("Yellowfin tuna"));
        while (state.getYear() < 5) {
            state.schedule.step(state);
        }
        state.schedule.step(state);
        final double diff = Math.abs(889195.40 - yellowfinBiomass.get() / 1000d);
        assertTrue(diff < 10);

    }

}