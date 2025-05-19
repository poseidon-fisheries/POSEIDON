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

package uk.ac.ox.oxfish.demoes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.RockyLogisticFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HabitatAwareGearFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.habitat.rectangles.RockyRectanglesHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.rectangles.RockyRectanglesHabitatInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * A lot of fishing occurs at the border much like an MPA
 * Created by carrknight on 10/5/15.
 */
public class FishTheRockyLineDemo {

    @Test
    public void fishTheRockyLine() throws Exception {


        int rockyAreas = 0;
        FishState state;

        do {
            rockyAreas = 0;
            PrototypeScenario scenario = new PrototypeScenario();
            scenario.setHabitatInitializer(new RockyRectanglesHabitatFactory());
            HabitatAwareGearFactory gear = new HabitatAwareGearFactory();
            gear.setMeanCatchabilityRocky(new FixedDoubleParameter(0));
            scenario.setFishers(200); //this way resources are consumed faster, makes for a stronger fish the line
            //one can get reliably between 35-50% fishing the border (which is a small area anyway) just with 100
            //but there are times where a corner of the map recovers enough that temporal dips happen; that makes
            //for an ineffectual measure to test against. Instead with 200 fishers i have never seen the percentage of effort
            //spent in the border to be below 60%
            scenario.setGear(gear);
            RockyLogisticFactory biologyInitializer = new RockyLogisticFactory();
            scenario.setBiologyInitializer(biologyInitializer);
            biologyInitializer.setSandyCarryingCapacity(new FixedDoubleParameter(1000)); //make depletion faster
            SimpleMapInitializerFactory simpleMap = new SimpleMapInitializerFactory();
            simpleMap.setCellSizeInKilometers(new FixedDoubleParameter(2d));
            scenario.setMapInitializer(simpleMap);

            state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();
            NauticalMap map = state.getMap();
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    if (map.getSeaTile(x, y).getRockyPercentage() > .9)
                        rockyAreas++;
                }
            }
        }
        while (rockyAreas < 50); //keep resetting if the map has too few rocky areas


        while (state.getYear() < 10)
            state.schedule.step(state);


        System.out.println("Border fishing intensity: " + state.getDailyDataSet().getColumn(
            RockyRectanglesHabitatInitializer.BORDER_FISHING_INTENSITY).getLatest()
            + "\n -------- \nRocky fishing intensity: " +
            state.getDailyDataSet().getColumn(
                RockyRectanglesHabitatInitializer.ROCKY_FISHING_INTENSITY).getLatest()

        );

        Assertions.assertTrue(state.getDailyDataSet().getColumn(
            RockyRectanglesHabitatInitializer.BORDER_FISHING_INTENSITY).getLatest()
            > 35); //on average more than 35% of all tows happened in rocky areas
    }


}
