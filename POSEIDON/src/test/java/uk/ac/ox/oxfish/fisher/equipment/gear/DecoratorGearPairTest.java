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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.mockito.Mockito.mock;

public class DecoratorGearPairTest {


    @Test
    public void single() {

        FixedProportionGear single = new FixedProportionGear(.1);
        DecoratorGearPair returned = DecoratorGearPair.getActualGear(single);
        Assertions.assertEquals(returned.getDeepestDecorator(), null);
        Assertions.assertEquals(returned.getDecorated(), single);

    }

    @Test
    public void oneDown() {

        FixedProportionGear delegate = new FixedProportionGear(.1);
        GarbageGearDecorator decorator = new GarbageGearDecorator(
            mock(Species.class),
            .1,
            delegate,
            true
        );
        DecoratorGearPair returned = DecoratorGearPair.getActualGear(decorator);
        Assertions.assertEquals(returned.getDeepestDecorator(), decorator);
        Assertions.assertEquals(returned.getDecorated(), delegate);

    }

    @Test
    public void twoDown() {

        FixedProportionGear delegate = new FixedProportionGear(.1);
        GarbageGearDecorator decorator = new GarbageGearDecorator(
            mock(Species.class),
            .1,
            delegate,
            true
        );
        GarbageGearDecorator decorator2 = new GarbageGearDecorator(
            mock(Species.class),
            .1,
            decorator,
            true
        );
        DecoratorGearPair returned = DecoratorGearPair.getActualGear(decorator2);
        Assertions.assertEquals(returned.getDeepestDecorator(), decorator);
        Assertions.assertEquals(returned.getDecorated(), delegate);

        //and I can change the tree leaf without affecting the tree structure
        Gear mock = mock(Gear.class);
        returned.getDeepestDecorator().setDelegate(mock);
        returned = DecoratorGearPair.getActualGear(decorator2);
        Assertions.assertEquals(returned.getDeepestDecorator(), decorator);
        Assertions.assertEquals(returned.getDecorated(), mock);

    }
}
