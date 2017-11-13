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

package uk.ac.ox.oxfish.fisher.equipment;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class HoldTest {


    //load small loads correctly


    @Test
        public void loadCorrectly() throws Exception
    {
        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);
        Hold hold = new Hold(100,bio);


        hold.load(new Catch(second,50.0,bio));
        assertEquals(50, hold.getTotalWeightOfCatchInHold(), .001);

        hold.load(new Catch(first, 10.0, bio));
        assertEquals(60, hold.getTotalWeightOfCatchInHold(), .001);

        hold.load(new Catch(first, 1.0, bio));
        assertEquals(61, hold.getTotalWeightOfCatchInHold(), .001);

        assertEquals(hold.getPercentageFilled(),.61,.001);


    }

    @Test
    public void throwsOverboard() throws Exception
    {

        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);
        Hold hold = new Hold(100, bio);


        hold.load(new Catch(first, 100.0, bio));
        assertEquals(100, hold.getTotalWeightOfCatchInHold(), .001);

        hold.load(new Catch(second, 300.0, bio));
        assertEquals(100, hold.getTotalWeightOfCatchInHold(), .001);

        //has thrown stuff overboard
        assertEquals(25, hold.getWeightOfCatchInHold(first), .001);
        assertEquals(75, hold.getWeightOfCatchInHold(second), .001);



    }

    @Test
    public void unloadsCorrectly()
    {
        Species first = new Species("lame");
        Species second = new Species("second");
        GlobalBiology bio = new GlobalBiology(first,second);

        Hold hold = new Hold(100,bio);


        hold.load(new Catch(first, 25.0, bio));
        hold.load(new Catch(second, 35.0, bio));
        assertEquals(60, hold.getTotalWeightOfCatchInHold(), .001);

        Catch caught = hold.unload();
        //should have emptied the hold
        assertEquals(0, hold.getTotalWeightOfCatchInHold(), .001);
        assertEquals(25.0, caught.getWeightCaught(first), .001);
        //should not fuck up if I fill the hold again
        hold.load(new Catch(first, 15.0, bio));
        assertEquals(25.0, caught.getWeightCaught(first), .001);
        assertEquals(15, hold.getTotalWeightOfCatchInHold(), .001);



    }

    @Test
    public void throwsOverboardAbundance() throws Exception {

        //set up copied from the holdsize test
        StockAssessmentCaliforniaMeristics first = mock(StockAssessmentCaliforniaMeristics.class);
        StockAssessmentCaliforniaMeristics second = mock(StockAssessmentCaliforniaMeristics.class);
        Species firstSpecies = new Species("first",first);
        Species secondSpecies = new Species("second",second);



        GlobalBiology bio = new GlobalBiology(firstSpecies, secondSpecies);



        when(first.getMaxAge()).thenReturn(2);
        when(second.getMaxAge()).thenReturn(1);

        when(first.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        10d,20d,30d
                )
        );
        when(first.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        50d,50d,50d
                )
        );


        when(second.getWeightFemaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );
        when(second.getWeightMaleInKg()).thenReturn(
                ImmutableList.of(
                        100d,100d
                )
        );

        Hold hold = new Hold(1000d,
                             bio);

        hold.load(
                new Catch(
                        new double[]{0,10,0},
                        new double[]{10,0,0},
                        firstSpecies,
                        bio

                )
        );

        assertEquals(hold.getTotalWeightOfCatchInHold(),
                     10*10+10*50d,.001d);
        assertEquals(hold.getWeightOfCatchInHold(firstSpecies),
                     600d,0001d);
        assertEquals(hold.getWeightOfCatchInHold(secondSpecies),
                     0d,0001d);

        assertEquals(hold.getWeightOfBin(firstSpecies,0),100d,.001d);
        assertEquals(hold.getWeightOfBin(firstSpecies,1),500d,.001d);
        assertEquals(hold.getWeightOfBin(firstSpecies,2),0d,.001d);
        //catch the other species, too
        hold.load(
                new Catch(
                        new double[]{0,2},
                        new double[]{0,0},
                        secondSpecies,
                        bio

                )
        );
        assertEquals(hold.getTotalWeightOfCatchInHold(),
                     800,.001d);
        assertEquals(hold.getWeightOfCatchInHold(firstSpecies),
                     600d,0001d);
        assertEquals(hold.getWeightOfCatchInHold(secondSpecies),
                     200d,0001d);

        assertEquals(hold.getWeightOfBin(firstSpecies,0),100d,.001d);
        assertEquals(hold.getWeightOfBin(firstSpecies,1),500d,.001d);
        assertEquals(hold.getWeightOfBin(firstSpecies,2),0d,.001d);

        //so far so good, now we overload and we should throw away 50% of each

        //catch the other species, too
        hold.load(
                new Catch(
                        new double[]{0,12},
                        new double[]{0,0},
                        secondSpecies,
                        bio

                )
        );
        //you had 2000kg, you can only hold 1000kg
        //you had 600-1400 now you ought to have 300-700
        assertEquals(hold.getTotalWeightOfCatchInHold(),
                     1000,.001d);
        assertEquals(hold.getWeightOfCatchInHold(firstSpecies),
                     300d,0001d);
        assertEquals(hold.getWeightOfCatchInHold(secondSpecies),
                     700d,0001d);


        assertEquals(hold.getWeightOfBin(firstSpecies,0),50d,.001d);
        assertEquals(hold.getWeightOfBin(firstSpecies,1),250d,.001d);
        assertEquals(hold.getWeightOfBin(firstSpecies,2),0d,.001d);
        assertEquals(hold.getWeightOfBin(secondSpecies,0),0d,.001d);
        assertEquals(hold.getWeightOfBin(secondSpecies,1),700d,.001d);

    }
}