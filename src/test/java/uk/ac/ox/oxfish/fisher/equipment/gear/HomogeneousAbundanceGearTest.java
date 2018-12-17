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

package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.FEMALE;

/**
 * Created by carrknight on 3/10/16.
 */
public class HomogeneousAbundanceGearTest
{


    @Test
    public void catchesCorrectly() throws Exception {


        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(1,
                                                                     new FixedProportionFilter(.5, true),
                                                                     new FixedProportionFilter(.5, true));

        SeaTile tile = mock(SeaTile.class,RETURNS_DEEP_STUBS);
        when(tile.getAbundance(any())).thenReturn(new StructuredAbundance(new double[]{0},new double[]{100}));
        when(tile.getBiology().getBiomass(any())).thenReturn(1d);

        Species species = new Species("test", new FromListMeristics(new double[]{1},2));
        GlobalBiology biology = new GlobalBiology(species);


        Catch fish = gear.fish(mock(Fisher.class), tile,tile , 1, biology);
        assertEquals(fish.getWeightCaught(0), FishStateUtilities.weigh(
                new double[]{0}, new double[]{25}, species.getMeristics()), .001);

        assertEquals(fish.getAbundance(0).asMatrix()[FEMALE][0], 25, .001);

        //it shouldn't break if I run it again!
        fish = gear.fish(mock(Fisher.class), tile,tile , 1, biology);
        assertEquals(fish.getWeightCaught(0), FishStateUtilities.weigh(
                new double[]{0}, new double[]{25}, species.getMeristics()), .001);

        assertEquals(fish.getAbundance(0).asMatrix()[FEMALE][0], 25, .001);


    }


    @Test
    public void oneHour() throws Exception {


        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(1,
                                                                     new FixedProportionFilter(1, true),
                                                                     new FixedProportionFilter(.5, true));

        SeaTile tile = mock(SeaTile.class,RETURNS_DEEP_STUBS);
        when(tile.getAbundance(any())).thenReturn(new StructuredAbundance(new double[]{0},new double[]{100}));

        when(tile.getBiology().getBiomass(any())).thenReturn(1d);

        Species species = new Species("test", new FromListMeristics(new double[]{1},2));
        GlobalBiology biology = new GlobalBiology(species);

        Catch fish = gear.fish(mock(Fisher.class), tile,tile , 1, biology);

        assertEquals(fish.getWeightCaught(0), FishStateUtilities.weigh(
                new double[]{0}, new double[]{50}, species.getMeristics()), .001);


    }



    @Test
    public void expectationKillsNoFish() throws Exception {


        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(1,
                                                                     new FixedProportionFilter(1, true),
                                                                     new FixedProportionFilter(.5, true));

        SeaTile tile = mock(SeaTile.class,RETURNS_DEEP_STUBS);
        when(tile.getAbundance(any())).thenReturn(new StructuredAbundance(new double[]{0},new double[]{100}));

        when(tile.getBiology().getBiomass(any())).thenReturn(1d);

        Species species = new Species("test", new FromListMeristics(new double[]{1},2));
        GlobalBiology biology = new GlobalBiology(species);

        double fish[] = gear.expectedHourlyCatch(mock(Fisher.class), tile,1, biology);

        assertEquals(fish[0], FishStateUtilities.weigh(new double[]{0},
                                                       new double[]{50},species.getMeristics()),.001);


    }

    @Test
    public void twoHours() throws Exception {


        HomogeneousAbundanceGear gear = new HomogeneousAbundanceGear(1,
                                                                     new FixedProportionFilter(1, true),
                                                                     new FixedProportionFilter(.5, true));

        SeaTile tile = mock(SeaTile.class,RETURNS_DEEP_STUBS);
        when(tile.getAbundance(any())).thenReturn(new StructuredAbundance(new double[]{0},new double[]{100}));

        when(tile.getBiology().getBiomass(any())).thenReturn(1d);

        Species species = new Species("test", new FromListMeristics(new double[]{1},2));
        GlobalBiology biology = new GlobalBiology(species);

        Catch fish = gear.fish(mock(Fisher.class), tile, tile, 2, biology);

        //you are going to catch 50 on the first hour and 50 in the other second (this is because seatile is mocked and doesn't kill off biology)
        assertEquals(fish.getWeightCaught(0), FishStateUtilities.weigh(
                new double[]{0}, new double[]{50+50}, species.getMeristics()), .001);
        assertEquals(fish.getAbundance(0).asMatrix()[FEMALE][0], 100, .0001);


    }
}