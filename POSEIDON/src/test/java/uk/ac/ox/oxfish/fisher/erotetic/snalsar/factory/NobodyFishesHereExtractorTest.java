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

package uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.NobodyFishesHereExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 6/7/16.
 */
public class NobodyFishesHereExtractorTest {

    @Test
    public void WorksAsIntended() throws Exception {

        SeaTile good = mock(SeaTile.class);
        SeaTile bad1 = mock(SeaTile.class);
        SeaTile bad2 = mock(SeaTile.class);

        Fisher dude1 = mock(Fisher.class, RETURNS_DEEP_STUBS);
        Fisher dude2 = mock(Fisher.class, RETURNS_DEEP_STUBS);
        when(dude1.getLastFinishedTrip().getTilesFished()).thenReturn(Sets.newHashSet(bad1));
        when(dude2.getLastFinishedTrip().getTilesFished()).thenReturn(Sets.newHashSet(bad2));

        FishState model = mock(FishState.class);
        when(model.getFishers()).thenReturn(ObservableList.observableList(Lists.newArrayList(dude1, dude2)));

        NobodyFishesHereExtractor extractor = new NobodyFishesHereExtractor();
        Map<SeaTile, Double> map = extractor.extractFeature(
            Lists.newArrayList(good, bad1, bad2),
            model,
            mock(Fisher.class)
        );
        Assertions.assertEquals(map.size(), 3);
        Assertions.assertEquals(map.get(good), 1, .001);
        Assertions.assertEquals(map.get(bad1), -1, .001);
        Assertions.assertEquals(map.get(bad2), -1, .001);

    }
}
