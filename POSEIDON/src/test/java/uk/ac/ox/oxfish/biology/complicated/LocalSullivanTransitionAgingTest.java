/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.SullivanTransitionProbability;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LocalSullivanTransitionAgingTest {


    @Test
    public void agesAtRightTime() {

        SullivanTransitionProbability probability = new SullivanTransitionProbability(Math.sqrt(.1),
                                                                                      95,
                                                                                      0.1627,
                                                                                      25,
                                                                                      5,
                                                                                      1);

        LocalSullivanTransitionAging aging = new LocalSullivanTransitionAging(new SullivanTransitionProbability[]{probability},
                                                                              2);


        double[] lengths = new double[25];
        for(int i=0; i<lengths.length; i++)
            lengths[i] = i*5+2.5;

        Species species = new Species("test",
                                      new GrowthBinByList(1,lengths,new double[25]),false);
        GlobalBiology biology = new GlobalBiology(species);

        AbundanceLocalBiology bio = new AbundanceLocalBiology(biology);

        bio.getAbundance(species).asMatrix()[0][9] = 1000d;


        //nothing should happen
        FishState mock = mock(FishState.class);
        when(mock.getDay()).thenReturn(1); //does not step on odd days
        aging.start(species);
        aging.ageLocally(bio, species, mock, false, 1);
        assertEquals(bio.getAbundance(species).asMatrix()[0][9],1000d,.0001);
        //again!
        when(mock.getDay()).thenReturn(2); //does not step on odd days
        aging.ageLocally(bio, species, mock, false, 1);
        //now there ought to be only 12% or so of the fish in that bin!
        assertEquals(bio.getAbundance(species).asMatrix()[0][9],130.91,.01);

        when(mock.getDay()).thenReturn(4); //does not step on odd days
        aging.ageLocally(bio, species, mock, false, 1);
        //now there ought to be only 12% or so of the fish in that bin!
        assertEquals(bio.getAbundance(species).asMatrix()[0][9],17.14,.01);



    }
}