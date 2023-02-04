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

import org.junit.Test;
import sim.util.Bag;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class LessThanXFishersHereExtractorTest {


    @Test
    public void safeifEmpty() throws Exception {


        SeaTile full = mock(SeaTile.class);
        SeaTile empty = mock(SeaTile.class);
        SeaTile halfFull = mock(SeaTile.class);
        List<SeaTile> toRepresent = Arrays.asList(full, empty,halfFull);



        FishState model = mock(FishState.class);
        when(model.getFishersAtLocation(empty)).thenReturn(new Bag());
        Bag halfFullBag = new Bag();
        halfFullBag.add(new Object());
        halfFullBag.add(new Object());
        halfFullBag.add(new Object());
        when(model.getFishersAtLocation(halfFull)).thenReturn(halfFullBag);
        Bag fullBag = new Bag();
        fullBag.add(new Object());
        fullBag.add(new Object());
        fullBag.add(new Object());
        fullBag.add(new Object());
        fullBag.add(new Object());
        when(model.getFishersAtLocation(full)).thenReturn(fullBag);


        LessThanXFishersHereExtractor safe = new LessThanXFishersHereExtractor(5);
        HashMap<SeaTile, Double> safetyReport = safe.extractFeature(toRepresent, model,
                                                                    mock(Fisher.class)
        );

        assertTrue(safetyReport.get(empty)>0);
        assertTrue(safetyReport.get(halfFull)>0);
        assertTrue(safetyReport.get(full)<=0);


    }
}