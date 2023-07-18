/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.IndependentLogisticFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 5/2/16.
 */
public class BetterThanAverageEroteticDestinationFactoryTest {

    private static final int MAX_STEPS = 2000;

    @Test
    public void plainThresholdDoesNotClearMap() {

        int timesItWasTrue = 0;
        for (int i = 0; i < 3; i++) {
            final int steps = stepsItTookErotetic(i, false);
            final int steps2 = stepsItTookErotetic(i, true);
            if (steps > steps2)
                timesItWasTrue++;
        }
        assertTrue(timesItWasTrue > 1);

    }


    private static int stepsItTookErotetic(
        final long seed,
        final boolean adaptive
    ) {
        Logger.getGlobal().setLevel(Level.INFO);
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new IndependentLogisticFactory()); //skip migration which should make this faster.
        scenario.setFishers(300);
        if (adaptive)
            scenario.setDestinationStrategy(new BetterThanAverageEroteticDestinationFactory());
        else {
            final ThresholdEroteticDestinationFactory plainThreshold = new ThresholdEroteticDestinationFactory();
            plainThreshold.setProfitThreshold(new FixedDoubleParameter(0d));
            scenario.setDestinationStrategy(plainThreshold);
        }

        final FishState state = new FishState(seed, 1);
        state.setScenario(scenario);
        state.start();
        final Species onlySpecies = state.getBiology().getSpecie(0);
        final double minimumBiomass = state.getTotalBiomass(
            onlySpecies) * .1; //how much does it take to eat 90% of all the fish?


        int steps;
        for (steps = 0; steps < MAX_STEPS; steps++) {
            state.schedule.step(state);
            if (state.getTotalBiomass(onlySpecies) <= minimumBiomass)
                break;
        }
        //   System.out.println(steps + " -- " + state.getTotalBiomass(onlySpecies));
        return steps;


    }

}