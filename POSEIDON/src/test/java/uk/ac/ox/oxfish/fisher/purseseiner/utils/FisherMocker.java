/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.utils;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.IntStream.range;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FisherMocker {

    private final String idPrefix = "Fisher";

    public List<Fisher> mockFishers(int numFishers) {
        return range(0, numFishers)
            .mapToObj(this::mockFisher)
            .collect(toImmutableList());
    }

    private Fisher mockFisher(int id) {
        final Fisher fisher = mock(Fisher.class);
        when(fisher.getTags()).thenReturn(ImmutableList.of(idPrefix + id));
        return fisher;
    }

}
