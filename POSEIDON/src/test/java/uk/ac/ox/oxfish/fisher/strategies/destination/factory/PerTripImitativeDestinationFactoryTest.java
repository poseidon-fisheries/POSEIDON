/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.destination.PerTripIterativeDestinationStrategy;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;


public class PerTripImitativeDestinationFactoryTest {


    /**
     * 5 fishers, how often do they go on MPAs?
     */

    @Test
    public void fiveguysNotAvoid() throws Exception {

        int protectedTargets = protectedTargetsHit(false);

        Assertions.assertTrue(protectedTargets > 0);
        System.out.println(protectedTargets);


    }

    public int protectedTargetsHit(final boolean avoidMPAs) {
        PrototypeScenario scenario = new PrototypeScenario();
        PerTripImitativeDestinationFactory destinationStrategy = new PerTripImitativeDestinationFactory();
        destinationStrategy.setAutomaticallyIgnoreMPAs(avoidMPAs);
        scenario.setDestinationStrategy(destinationStrategy);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(5));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);
        scenario.setFishers(5);

        scenario.getStartingMPAs().add(new StartingMPA(0, 0, 2, 2));

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        //burn in
        for (int i = 0; i < 20; i++)
            state.schedule.step(state);
        int protectedTargets = 0;
        for (int i = 0; i < 100; i++) {
            state.schedule.step(state);
            for (Fisher fisher : state.getFishers())
                if (((PerTripIterativeDestinationStrategy) fisher.getDestinationStrategy()).getDelegate()
                    .getFavoriteSpot()
                    .isProtected())
                    protectedTargets++;
        }
        return protectedTargets;
    }

    @Test
    public void fiveguysAvoid() throws Exception {

        int protectedTargets = protectedTargetsHit(true);

        Assertions.assertTrue(protectedTargets == 0);
        System.out.println(protectedTargets);


    }
}
