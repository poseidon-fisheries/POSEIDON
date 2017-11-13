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

package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/6/17.
 */
public class StandardAgingProcessTest {


    @Test
    public void oldFishDies() throws Exception {

        Species species = mock(Species.class);
        when(species.getMaxAge()).thenReturn(2);
        double[] male = {10, 20, 30};
        double[] female = {100, 200, 300};

        StandardAgingProcess process = new StandardAgingProcess(false);

        AbundanceBasedLocalBiology bio = mock(AbundanceBasedLocalBiology.class);
        when(bio.getNumberOfMaleFishPerAge(species)).thenReturn(male);
        when(bio.getNumberOfFemaleFishPerAge(species)).thenReturn(female);


        process.ageLocally(bio, species, null, true);

        assertArrayEquals(male,new double[]{0,10,20},.0001);
        assertArrayEquals(female,new double[]{0,100,200},.0001);

    }


    @Test
    public void oldFishStays() throws Exception {

        Species species = mock(Species.class);
        when(species.getMaxAge()).thenReturn(2);
        double[] male = {10, 20, 30};
        double[] female = {100, 200, 300};

        StandardAgingProcess process = new StandardAgingProcess(true);

        AbundanceBasedLocalBiology bio = mock(AbundanceBasedLocalBiology.class);
        when(bio.getNumberOfMaleFishPerAge(species)).thenReturn(male);
        when(bio.getNumberOfFemaleFishPerAge(species)).thenReturn(female);


        process.ageLocally(bio, species, null, true);

        assertArrayEquals(male,new double[]{0,10,50},.0001);
        assertArrayEquals(female,new double[]{0,100,500},.0001);

    }
}