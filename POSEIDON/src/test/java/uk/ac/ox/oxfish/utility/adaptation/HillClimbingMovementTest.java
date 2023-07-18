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

package uk.ac.ox.oxfish.utility.adaptation;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


public class HillClimbingMovementTest {

    @Test
    public void hillclimber() throws Exception {


        final SeaTile old = mock(SeaTile.class);
        final SeaTile current = mock(SeaTile.class);
        final SeaTile newTile = mock(SeaTile.class);
        final MersenneTwisterFast random = new MersenneTwisterFast(System.currentTimeMillis());


        //if new is better than old, go random
        final BeamHillClimbing<SeaTile> algo = new BeamHillClimbing<SeaTile>(
            (state, random1, fisher, current1) -> newTile
        );

        assertEquals(newTile, algo.randomize(random, mock(Fisher.class), 0, current));

        //current better than old? stay!
        assertEquals(current, algo.judgeRandomization(random,
            mock(Fisher.class), 0, 100, old, current
        ));

        //if old is better than new, go back to new
        assertEquals(old, algo.judgeRandomization(random,
            mock(Fisher.class), 100, 0, old, current
        ));

    }
}