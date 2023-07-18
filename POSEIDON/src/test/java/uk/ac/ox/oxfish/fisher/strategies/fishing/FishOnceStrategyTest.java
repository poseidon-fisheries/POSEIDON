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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.FishOnceFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;


public class FishOnceStrategyTest {

    @Test
    public void fishOnce() throws Exception {

        TripRecord record = new TripRecord(1, 100d, 0);

        FishOnceStrategy strategy = new FishOnceFactory().apply(mock(FishState.class));


        //should be true: you haven't fished before
        Assertions.assertTrue(strategy.shouldFish(
            mock(Fisher.class),
            new MersenneTwisterFast(),
            mock(FishState.class),
            record
        ));

        //record a single fish
        record.recordFishing(new FishingRecord(
            1,
            mock(SeaTile.class),
            mock(Catch.class)
        ));

        //should be false: you have fished at least once~!
        Assertions.assertFalse(strategy.shouldFish(
            mock(Fisher.class),
            new MersenneTwisterFast(),
            mock(FishState.class),
            record
        ));
    }
}