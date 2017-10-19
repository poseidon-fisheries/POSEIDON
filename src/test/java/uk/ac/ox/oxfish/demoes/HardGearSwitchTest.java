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

package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.experiments.HardGearSwitch;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 11/12/15.
 */
public class HardGearSwitchTest {


    @Test
    public void prototypeWorldHardGearSwitch() throws Exception {
        WellMixedBiologyFactory biologyInitializer = new WellMixedBiologyFactory();
        biologyInitializer.setFirstSpeciesCapacity(new FixedDoubleParameter(5000));
        biologyInitializer.setCapacityRatioSecondToFirst(new FixedDoubleParameter(1d));
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();

        FishState model = HardGearSwitch.buildHardSwitchGearDemo(biologyInitializer, mapInitializer, 0, 1, 500, 4500);


        model.start();


        while(model.getYear()<15)
            model.schedule.step(model);

        Double zeroCatchers = model.getLatestYearlyObservation("Species " + 0 + " Catchers");
        Double firstCatchers = model.getLatestYearlyObservation("Species " + 1 + " Catchers");

        assertTrue(zeroCatchers <100); //not everybody is catching 0
        assertTrue(zeroCatchers > 0); //not everybody is catching 0
        assertTrue(firstCatchers <100); //not everybody is catching 1
        assertTrue(firstCatchers > 0); //not everybody is catching 1
        assertEquals(firstCatchers+zeroCatchers,100,.00001); //not everybody is catching 1
        double firstQuotaEfficiency = model.getLatestYearlyObservation(
                model.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME) / (500 * 100);
        double secondQuotaEfficiency = model.getLatestYearlyObservation(
                model.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME) / (4500 * 100);

        System.out.println(firstQuotaEfficiency + " ------ " + secondQuotaEfficiency);
        assertTrue(firstQuotaEfficiency <=1 + FishStateUtilities.EPSILON);
        assertTrue(firstQuotaEfficiency >.7);
        assertTrue(secondQuotaEfficiency <=1+ FishStateUtilities.EPSILON);
        assertTrue(secondQuotaEfficiency >.7);



    }



}
