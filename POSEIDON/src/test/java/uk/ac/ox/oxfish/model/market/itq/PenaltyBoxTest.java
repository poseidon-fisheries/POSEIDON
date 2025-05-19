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

package uk.ac.ox.oxfish.model.market.itq;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;


public class PenaltyBoxTest {


    @Test
    public void penaltyBoxTest() throws Exception {
        Logger.getGlobal().info("Here I check that people stay the right amount of time in the penalty box");

        final Fisher one = mock(Fisher.class);
        final Fisher two = mock(Fisher.class);

        final PenaltyBox box = new PenaltyBox(10);
        box.registerTrader(one);
        Assertions.assertTrue(box.has(one));
        Assertions.assertFalse(box.has(two));

        //step it 5 times
        for (int i = 0; i < 5; i++)
            box.step(mock(FishState.class));
        Assertions.assertTrue(box.has(one));
        Assertions.assertFalse(box.has(two));

        //add two to the list
        box.registerTrader(two);
        Assertions.assertTrue(box.has(one));
        Assertions.assertTrue(box.has(two));

        //step it 5 times
        for (int i = 0; i < 5; i++)
            box.step(mock(FishState.class));
        //one should be out, two should be in
        Assertions.assertFalse(box.has(one));
        Assertions.assertTrue(box.has(two));
    }
}
