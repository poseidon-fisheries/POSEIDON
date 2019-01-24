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

package uk.ac.ox.oxfish.utility;

import com.beust.jcommander.internal.Lists;
import ec.util.MersenneTwisterFast;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 9/11/15.
 */
public class FishStateUtilitiesTest {


    @Test
    public void averager() {

        DataColumn column = new DataColumn("alpha");
        column.add(100d);
        column.add(200d);
        column.add(1d);
        column.add(2d);

        assertEquals(1.5,FishStateUtilities.getAverage(column,2),.0001);
    }

    @Test
    public void logistic() throws Exception {


        assertEquals(0.9933071491,FishStateUtilities.logisticProbability(1,10,1,.5),.0001);
        assertEquals(0.119202922,FishStateUtilities.logisticProbability(1,20,.9,1),.0001);

    }

    @Test
    public void utmToLatLong() throws Exception {

        //osmose low-right corner
        Point2D.Double latlong = FishStateUtilities.utmToLatLong("17 N", 584600.702, 2791787.489);
        assertEquals(25.24,latlong.getX(),.01);
        assertEquals(-80.16,latlong.getY(),.01);
        System.out.println(latlong);

        //osmose up-left corner
        latlong = FishStateUtilities.utmToLatLong("17 N", -73291.664, 3445097.299);
        assertEquals(31,latlong.getX(),.01);
        assertEquals(-87,latlong.getY(),.01);

        System.out.println(latlong);

    }





    @Test
    public void  printTablePerPort(){



        Fisher fisher1 = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher fisher2 = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher fisher3 = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher fisher4 = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Port port1 = mock(Port.class); when(port1.getName()).thenReturn("Seattle");
        Port port2 = mock(Port.class); when(port2.getName()).thenReturn("Shanghai");
        when(fisher1.getHomePort()).thenReturn(port1);
        when(fisher2.getHomePort()).thenReturn(port1);
        when(fisher3.getHomePort()).thenReturn(port2);
        when(fisher4.getHomePort()).thenReturn(port2);
        ObservableList<Fisher> fishers = FXCollections.observableArrayList(fisher1, fisher2, fisher3, fisher4);

        DataColumn column1 = new DataColumn("lame");
        when(fisher1.getYearlyData().getColumn("lame")).thenReturn(column1);
        column1.add(100d);
        column1.add(200d);

        DataColumn column2 = new DataColumn("lame");
        when(fisher2.getYearlyData().getColumn("lame")).thenReturn(column2);
        column2.add(200d);
        column2.add(100d);

        DataColumn column3 = new DataColumn("lame");
        when(fisher3.getYearlyData().getColumn("lame")).thenReturn(column3);
        column3.add(2000d);
        column3.add(1000d);

        DataColumn column4 = new DataColumn("lame");
        when(fisher4.getYearlyData().getColumn("lame")).thenReturn(column4);
        column4.add(1000d);
        column4.add(2000d);

        FishState model = mock(FishState.class);
        when(model.getFishers()).thenReturn(fishers);
        when(model.getPorts()).thenReturn(Lists.newArrayList(port1, port2));
        when(model.getYear()).thenReturn(2);

        String table = FishStateUtilities.printTablePerPort(model, "lame", 0);
        System.out.println(table);
        assertTrue(table.equals("Shanghai,Seattle\n" +
                "1500.0,150.0\n" +
                "1500.0,150.0\n") ||
                table.equals(
                        "Seattle,Shanghai\n" +
                                "150.0,1500.0\n" +
                                "150.0,1500.0\n")
        ) ;



    }


    @Test
    public void getValidSeatileFromGroup() throws Exception {

        SeaTile tile1 = mock(SeaTile.class);
        SeaTile tile2 = mock(SeaTile.class);
        SeaTile tile3 = mock(SeaTile.class);
        SeaTile tile4 = mock(SeaTile.class);
        SeaTile tile5 = mock(SeaTile.class);
        when(tile1.isFishingEvenPossibleHere()).thenReturn(true);
        when(tile2.isFishingEvenPossibleHere()).thenReturn(true);
        when(tile3.isFishingEvenPossibleHere()).thenReturn(true);
        when(tile4.isFishingEvenPossibleHere()).thenReturn(true);
        when(tile5.isFishingEvenPossibleHere()).thenReturn(false); //tile 5 should be ignored

        List<SeaTile> tiles = Lists.newArrayList(tile1,tile2,tile3,tile4,tile5);
        Fisher fisher = mock(Fisher.class);
        FishState model = mock(FishState.class);


        //tile 1 to 3 should also be ignored!
        when(fisher.isAllowedToFishHere(tile1,model)).thenReturn(false);
        when(fisher.isAllowedToFishHere(tile2,model)).thenReturn(false);
        when(fisher.isAllowedToFishHere(tile3,model)).thenReturn(false);
        when(fisher.isAllowedToFishHere(tile4,model)).thenReturn(true);
        when(fisher.isAllowedToFishHere(tile5,model)).thenReturn(true);

        for(int i=0; i<100; i++)
        {
            SeaTile tile = FishStateUtilities.getValidSeatileFromGroup(new MersenneTwisterFast(),
                    tiles,
                    true,
                    fisher,
                    model,
                    true,
                    100);
            assertEquals(tile,tile4);
        }
    }


    @Test
    public void distanceToTimeSeries() throws IOException {

        //distance to itself is always zero

        ArrayList<Double> input = com.google.common.collect.Lists.newArrayList(
                13095134.58,
                12382623.44,
                12187480.55,
                12308532.3,
                11996413.38,
                11847586.29,
                11784681.89,
                11683882.83,
                11451689.01,
                11484349.02,
                11129178.25,
                11041537.54,
                10970262.92,
                10759006.93,
                10702457.01

        );
        assertEquals(0,
                FishStateUtilities.timeSeriesDistance(
                        input,
                        input, 1,
                        false),
                0.0001);

        //transform into data column
        DataColumn data = new DataColumn("lame");
        for (Double observation : input) {
            data.add(observation);
        };

        assertEquals(0,
                FishStateUtilities.timeSeriesDistance(
                        data,
                        Paths.get("inputs","tests","landings.csv"), 1
                ),
                0.0001);


        //computed this distance with R
        //169961501
        assertEquals(169961500.94,
                FishStateUtilities.timeSeriesDistance(
                        data,
                        Paths.get("inputs","tests","landings2.csv"), 1
                ),
                0.01);

    }


    @Test
    public void weightedAverage() {

        double[] observations = new double[]{100,200,300};
        double[] weight = new double[]{1,1,10};
        double average = FishStateUtilities.getWeightedAverage(observations, weight);
        assertEquals(275,average,.0001);

    }
}