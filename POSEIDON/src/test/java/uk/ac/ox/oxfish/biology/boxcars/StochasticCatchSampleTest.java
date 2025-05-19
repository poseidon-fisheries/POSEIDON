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

package uk.ac.ox.oxfish.biology.boxcars;

import com.beust.jcommander.internal.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class StochasticCatchSampleTest {


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void catchSampler() {

        final Fisher yesOne = mock(Fisher.class, RETURNS_DEEP_STUBS);
        final Fisher yesTwo = mock(Fisher.class, RETURNS_DEEP_STUBS);
        final Fisher wrong = mock(Fisher.class, RETURNS_DEEP_STUBS);

        final Species species = new Species(
            "test",
            new FromListMeristics(new double[]{1, 2}, new double[]{10, 100}, 1)
        );

        final FishState model = mock(FishState.class);
        final ArrayList<Fisher> fishers = (ArrayList) Lists.newArrayList(yesOne, yesTwo, wrong);
        final ObservableList fisherList = ObservableList.observableList(fishers);
        when(model.getFishers()).thenReturn(fisherList);

        //one caught 10 small ones
        //two caught 5 big ones
        //wrong caught 100 of both
        when(yesOne.getDailyCounter().getSpecificLandings(species, 0, 0)).thenReturn(10d); //WEIGHT 10 individual
        when(yesOne.getDailyCounter().getSpecificLandings(species, 0, 1)).thenReturn(0d);
        when(yesTwo.getDailyCounter().getSpecificLandings(species, 0, 0)).thenReturn(0d);
        when(yesTwo.getDailyCounter().getSpecificLandings(species, 0, 1)).thenReturn(10d); //WEIGHT 5 individual
        when(wrong.getDailyCounter().getSpecificLandings(species, 0, 0)).thenReturn(100d);
        when(wrong.getDailyCounter().getSpecificLandings(species, 0, 1)).thenReturn(200d);

        final StochasticCatchSampler sampler = new StochasticCatchSampler(fisher -> fisher != wrong, species, null);

        sampler.start(model);
        sampler.observeDaily();
        double[][] sampledAbundance = sampler.getAbundance();
        Assertions.assertEquals(sampledAbundance[0][0], 10, .01);
        Assertions.assertEquals(sampledAbundance[0][1], 5, .01);

        //doesn't reset automatically
        sampler.observeDaily();
        sampledAbundance = sampler.getAbundance();
        Assertions.assertEquals(sampledAbundance[0][0], 20, .01);
        Assertions.assertEquals(sampledAbundance[0][1], 10, .01);

        //feed it the wrong weight and you get the wrong count
        sampledAbundance = sampler.getAbundance(integerIntegerPair -> 1d);
        Assertions.assertEquals(sampledAbundance[0][0], 20, .01);
        Assertions.assertEquals(sampledAbundance[0][1], 20, .01);

        //reset works
        sampler.resetCatchObservations();
        sampledAbundance = sampler.getAbundance();
        Assertions.assertEquals(sampledAbundance[0][0], 0, .01);
        Assertions.assertEquals(sampledAbundance[0][1], 0, .01);
    }
}
