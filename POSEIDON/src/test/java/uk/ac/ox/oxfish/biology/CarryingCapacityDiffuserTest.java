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

package uk.ac.ox.oxfish.biology;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightLogisticPlusClimateChangeFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 4/11/17.
 */
public class CarryingCapacityDiffuserTest {


    @Test
    public void carryingCapacityMovesSouthEast() throws Exception {


        final PrototypeScenario scenario = new PrototypeScenario();
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(4));
        mapInitializer.setWidth(new FixedDoubleParameter(4));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1d));
        scenario.setFishers(0);
        //make the fish migrate south east
        final FromLeftToRightLogisticPlusClimateChangeFactory biologyInitializer = new FromLeftToRightLogisticPlusClimateChangeFactory();
        biologyInitializer.setWestMigration(-1);
        biologyInitializer.setNorthMigration(-1);
        biologyInitializer.setClimateChangePercentageMovement(new FixedDoubleParameter(.2));
        biologyInitializer.setCarryingCapacity(new FixedDoubleParameter(1000));
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setMapInitializer(mapInitializer);

        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        while (state.getYear() < 5)
            state.schedule.step(state);

        final Species species = state.getBiology().getSpecies().get(0);
        Assertions.assertEquals(state.getMap().getSeaTile(0, 0).getBiomass(species), 0d, 1);
        Assertions.assertEquals(state.getMap().getSeaTile(2, 3).getBiomass(species), 9000d, 1);


    }
}
