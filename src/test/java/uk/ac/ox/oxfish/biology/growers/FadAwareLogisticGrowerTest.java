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
import uk.ac.ox.oxfish.biology.initializer.factory.MultipleIndependentSpeciesBiomassFactory;
import uk.ac.ox.oxfish.biology.initializer.factory.SingleSpeciesBiomassNormalizedFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Paths;
import java.util.function.Supplier;

import static org.junit.Assert.assertTrue;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;

public class FadAwareLogisticGrowerTest {

    @Test
    public void jonLandings() {

        TunaScenario scenario = new TunaScenario();
        scenario.setBoatsFile(input("dummy_boats.csv"));
        scenario.setDeploymentValuesFile(input("dummy_deployment_values.csv"));
        scenario.getExogenousCatchesFactory().setCatchesFile(Paths.get("inputs", "tests", "exogenous_catches.csv"));
        ((GravityDestinationStrategyFactory) scenario.getFisherDefinition().getDestinationStrategy())
            .setMaxTripDurationFile(input("dummy_boats.csv"));
        scenario.getFisherDefinition().setRegulation(new NoFishingFactory());
        ((MultipleIndependentSpeciesBiomassFactory) scenario.getBiologyInitializers())
            .getFactories()
            .forEach(factory -> {
                final AlgorithmFactory<? extends LogisticGrowerInitializer> growerFactory =
                    ((SingleSpeciesBiomassNormalizedFactory) factory).getGrower();
                ((FadAwareLogisticGrowerFactory) growerFactory).setUseLastYearBiomass(true);
            });

        FishState state = new FishState();
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