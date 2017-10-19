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

package uk.ac.ox.oxfish.biology;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.growers.IndependentLogisticBiomassGrower;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class BiomassLocalBiologyTest
{
    @Test
    public void logisticGrowthWorks() throws Exception {

        BiomassLocalBiology bio = new BiomassLocalBiology(
                new Double[]{100d,200d}, new Double[]{100d,400d}
        );
        IndependentLogisticBiomassGrower grower =
                new IndependentLogisticBiomassGrower(new Double[]{.5,.5});
        grower.getBiologies().add(bio);
        Species species0 = new Species("0"); species0.resetIndexTo(0);
        Species species1 = new Species("1"); species1.resetIndexTo(1);
        Species species2 = new Species("2"); species2.resetIndexTo(2);

        assertEquals(100, bio.getBiomass(species0), .1);
        assertEquals(200, bio.getBiomass(species1), .1);
        assertEquals(0, bio.getBiomass(species2), .1);

        //grow it
        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(model.getSpecies()).thenReturn(Lists.newArrayList(species0,species1,species2));
        grower.step(model);

        assertEquals(100, bio.getBiomass(species0), .1); //didn't grow because it is at capacity
        assertEquals(250, bio.getBiomass(species1), .1); //grew by 50%
        assertEquals(0, bio.getBiomass(species2), .1);  //0 doesn't grow

        bio.setCurrentBiomass(species1, 399.88);
        //grow it again
        grower.step(model);
        assertEquals(100, bio.getBiomass(species0), .1); //didn't grow because it is at capacity
        assertEquals(400, bio.getBiomass(species1), .1); //grew until capacity
        assertEquals(0, bio.getBiomass(species2), .1);  //0 doesn't grow
    }


}