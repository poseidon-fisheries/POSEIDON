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

package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 6/14/16.
 */
public class PeriodicUpdateFromListFactoryTest {


    @Test
    public void readFromYamlCorrectly() throws Exception {

        String toRead = "Periodic Gear Update from List:\n" +
            "  availableGears:\n" +
            "    - Fixed Proportion:\n" +
            "        catchabilityPerHour: '0.01'\n" +
            "    - Fixed Proportion:\n" +
            "        catchabilityPerHour: '0.02'\n" +
            "  probability:\n" +
            "    Fixed Probability:\n" +
            "      explorationProbability: '0.2'\n" +
            "      imitationProbability: '0.6'\n" +
            "  yearly: true";

        FishYAML yamler = new FishYAML();
        AlgorithmFactory<? extends GearStrategy> gearStrategy = yamler.loadAs(toRead, AlgorithmFactory.class);

        assertTrue(gearStrategy.getClass().equals(PeriodicUpdateFromListFactory.class));
        PeriodicUpdateFromListFactory casted = (PeriodicUpdateFromListFactory) gearStrategy;
        assertEquals(2, casted.getAvailableGears().size());
        for (AlgorithmFactory<? extends Gear> gearFactory : casted.getAvailableGears()) {
            assertTrue(gearFactory.getClass().equals(FixedProportionGearFactory.class));
            DoubleParameter catchabilityPerHour = ((FixedProportionGearFactory) gearFactory).getCatchabilityPerHour();
            assertTrue(((FixedDoubleParameter) catchabilityPerHour).getFixedValue() == 0.01 ||
                ((FixedDoubleParameter) catchabilityPerHour).getFixedValue() == 0.02);
        }


    }
}