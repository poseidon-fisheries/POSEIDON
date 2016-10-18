package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 8/17/16.
 */
public class LowessTileTest
{


    @Test
    public void regression() throws Exception {

        List<String> data = Files.readAllLines(Paths.get("inputs", "tests", "regression.csv"));
        assertEquals(data.size(),100);

        LeastSquareFilter tile = new LeastSquareFilter(2, 1000d, new double[]{0,0}, 1d);

        for(String line : data)
        {
            String[] split = line.split(",");
            assertEquals(split.length,2);
            double x =  Double.parseDouble(split[0]);
            double y =  Double.parseDouble(split[1]);
            tile.addObservation(new double[]{1,x},y,9);
        }
        System.out.println(Arrays.toString(tile.getBeta()));
        assertEquals(1.97711,tile.getBeta()[0],.01);
        assertEquals(4.85097,tile.getBeta()[1],.01);
    }
}