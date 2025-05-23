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

package uk.ac.ox.oxfish.fisher.strategies.gear.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

/**
 * Created by carrknight on 6/14/16.
 */
@SuppressWarnings("unchecked")
public class PeriodicUpdateFromListFactoryTest {


    @Test
    public void readFromYamlCorrectly() throws Exception {

        final String toRead = "Periodic Gear Update from List:\n" +
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

        final FishYAML yamler = new FishYAML();
        final AlgorithmFactory<? extends GearStrategy> gearStrategy = yamler.loadAs(toRead, AlgorithmFactory.class);

        Assertions.assertEquals(gearStrategy.getClass(), PeriodicUpdateFromListFactory.class);
        final PeriodicUpdateFromListFactory casted = (PeriodicUpdateFromListFactory) gearStrategy;
        Assertions.assertEquals(2, casted.getAvailableGears().size());
        for (final AlgorithmFactory<? extends Gear> gearFactory : casted.getAvailableGears()) {
            Assertions.assertEquals(gearFactory.getClass(), FixedProportionGearFactory.class);
            final DoubleParameter catchabilityPerHour = ((FixedProportionGearFactory) gearFactory).getCatchabilityPerHour();
            Assertions.assertTrue(((FixedDoubleParameter) catchabilityPerHour).getValue() == 0.01 ||
                ((FixedDoubleParameter) catchabilityPerHour).getValue() == 0.02);
        }


    }
}
