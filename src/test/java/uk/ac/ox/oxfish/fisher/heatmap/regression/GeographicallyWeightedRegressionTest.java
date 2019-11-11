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

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/18/16.
 */
public class GeographicallyWeightedRegressionTest {


    @Test
    public void weightedRegression() throws Exception {

        NauticalMap map = mock(NauticalMap.class);
        SeaTile tile = mock(SeaTile.class);
        when(map.getAllSeaTilesExcludingLandAsList()).thenReturn(Lists.newArrayList(tile));
        Distance distance = mock(Distance.class);

        GeographicallyWeightedRegression regression = new GeographicallyWeightedRegression(
                map,1d,distance,10,
                new ObservationExtractor[]{
                    //this will actually be rerouted to read from the file
                    new ObservationExtractor() {
                        @Override
                        public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
                            return timeOfObservation;
                        }
                    }
                },
                0,10,
                10000,
                new MersenneTwisterFast()
        );

        List<String> data = Files.readAllLines(Paths.get("inputs", "tests", "w_regression.csv"));
        assertEquals(data.size(),100);

        for(String line : data)
        {
            String[] split = line.split(",");
            assertEquals(split.length,3);
            double x =  Double.parseDouble(split[0]);
            double y =  Double.parseDouble(split[1]);
            when(distance.distance(any(),any(),any())).thenReturn(
                    Double.parseDouble(split[2])
            );
            regression.addObservation(new GeographicalObservation<>(mock(SeaTile.class),x,y),
                                      mock(Fisher.class), mock(FishState.class));
        }
        System.out.println(Arrays.toString(regression.getBeta(tile)));
        assertEquals(1.423,regression.getBeta(tile)[0],.1); //some imprecision here, but more or less correct
        assertEquals(9.996,regression.getBeta(tile)[1],.01);


    }

    @Test
    public void setParameters() throws Exception {

        NauticalMap map = mock(NauticalMap.class);
        SeaTile tile = mock(SeaTile.class);
        when(map.getAllSeaTilesExcludingLandAsList()).thenReturn(Lists.newArrayList(tile));
        Distance distance = mock(Distance.class);

        GeographicallyWeightedRegression regression = new GeographicallyWeightedRegression(
                map,.23d,distance,10,
                new ObservationExtractor[]{
                        //this will actually be rerouted to read from the file
                        new ObservationExtractor() {
                            @Override
                            public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
                                return timeOfObservation;
                            }
                        }
                },
                0,10,
                10000,
                new MersenneTwisterFast()
        );

        assertArrayEquals(regression.getParametersAsArray(),new double[]{.23,10},.001);
        regression.setParameters(new double[]{.56,5});
        assertArrayEquals(regression.getParametersAsArray(),new double[]{.56,5},.001);




    }
}