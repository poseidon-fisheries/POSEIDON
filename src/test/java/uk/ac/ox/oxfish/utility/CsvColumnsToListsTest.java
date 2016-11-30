package uk.ac.ox.oxfish.utility;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 11/30/16.
 */
public class CsvColumnsToListsTest {


    @Test
    public void csvColumn() throws Exception {

        CsvColumnsToLists tester = new CsvColumnsToLists(
                Paths.get("inputs","tests","weather.csv").toAbsolutePath().toString(),
                ',', "alsowrong,wrong".split(",")
        );

        LinkedList<Double>[] columns = tester.readColumns();
        assertEquals(columns[0].get(0),-1,.0001);
        assertEquals(columns[1].get(0),0,.0001);

    }
}