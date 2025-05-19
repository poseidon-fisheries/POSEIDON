/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SelectivityFromListGearFactoryTest {

    @Test
    public void selectFromListCorrectly() {


        Species species = mock(Species.class);
        when(species.getNumberOfSubdivisions()).thenReturn(1);
        when(species.getNumberOfBins()).thenReturn(3);

        SelectivityFromListGearFactory factory = new SelectivityFromListGearFactory();
        factory.setSelectivityPerBin("0d,1d,.5d");
        factory.setAverageCatchability(new FixedDoubleParameter(.5d));

        final HomogeneousAbundanceGear gear = factory.apply(mock(FishState.class));
        final double[][] selected = gear.filter(
            species,
            new double[][]{{100, 100, 100}}
        );


        Assertions.assertEquals(selected.length, 1);
        //1/(1+exp(15.0948823-0.5391899*(c(25,40,100))))
        // 0.1658769 0.9984574 1.0000000
        Assertions.assertEquals(selected[0][0], 0, .001);
        Assertions.assertEquals(selected[0][1], 50, .001);
        Assertions.assertEquals(selected[0][2], 25, .001);

    }
}
