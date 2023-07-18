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

package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 5/31/16.
 */
public class NoFriendsHereExtractorTest {


    @Test
    public void doNotGoWhereYourFriendIs() throws Exception {

        SeaTile empty = mock(SeaTile.class);
        SeaTile full = mock(SeaTile.class);
        Fisher friend = mock(Fisher.class);
        TripRecord record = mock(TripRecord.class);
        when(record.getTilesFished()).thenReturn(
            Sets.newHashSet(full)
        );
        when(friend.getLastFinishedTrip()).thenReturn(record);

        NoFriendsHereExtractor extractor =
            new NoFriendsHereExtractor(true);

        Fisher chooser = mock(Fisher.class);
        when(chooser.getDirectedFriends()).thenReturn(
            Lists.newArrayList(friend));

        List<SeaTile> options = Lists.newArrayList(empty, full);
        Map<SeaTile, Double> featureMap = extractor.extractFeature(
            options,
            mock(FishState.class),
            chooser
        );

        Assertions.assertEquals(featureMap.size(), 2);
        Assertions.assertEquals(featureMap.get(empty), 1, .0001);
        Assertions.assertEquals(featureMap.get(full), -1, .0001);


    }
}