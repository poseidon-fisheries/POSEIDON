package uk.ac.ox.oxfish.utility;

import com.google.common.collect.Sets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 9/11/15.
 */
public class FishStateUtilitiesTest {


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
        when(model.getPorts()).thenReturn(Sets.newHashSet(port1,port2));
        when(model.getYear()).thenReturn(2);

        String table = FishStateUtilities.printTablePerPort(model, "lame");
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
}