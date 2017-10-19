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

package uk.ac.ox.oxfish.utility.adaptation.maximization;

import ec.util.MersenneTwisterFast;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 10/6/16.
 */
public class GravitationalSearchAdaptationTest {


    @Test
    public void worksInTheory() throws Exception {

        MersenneTwisterFast random = new MersenneTwisterFast();
        //best spot to be is (10,20,30) can GDA drive people there?
        FishState model = mock(FishState.class);
        when(model.getRandom()).thenReturn(random);

        Map<Fisher,double[]> coordinates = new HashMap<>();

        for(int i=0; i<100; i++)
            coordinates.put(mock(Fisher.class),
                            new double[]{
                                    random.nextDouble()*100,
                                    random.nextDouble()*100,
                                    random.nextDouble()*100
                            });
        ObservableList<Fisher> list = FXCollections.observableList(new LinkedList<>());
        list.addAll(coordinates.keySet());
        when(model.getFishers()).thenReturn(list);



        HashMap<Fisher,GravitationalSearchAdaptation<double[]>> searchers = new HashMap<>();

        for(Fisher fisher : coordinates.keySet())
            searchers.put(
                    fisher,
                    new GravitationalSearchAdaptation<>(
                            (Sensor<Fisher,double[]>) fisher1 -> coordinates.get(fisher1),
                            (fisher12, change, state) -> coordinates.put(fisher12, change),
                            fisher13 -> true,
                            new CoordinateTransformer<double[]>() {
                                @Override
                                public double[] toCoordinates(double[] variable, Fisher fisher, FishState model) {
                                    return variable;
                                }

                                @Override
                                public double[] fromCoordinates(double[] variable, Fisher fisher, FishState model) {
                                    return variable;
                                }
                            },
                            new ObjectiveFunction<Fisher>() {
                                @Override
                                public double computeCurrentFitness(
                                        Fisher observer, Fisher observed) {
                                    double[] coord = coordinates.get(observed);
                                    return -Math.pow(coord[0]-10,2)
                                            -Math.pow(coord[1]-20,2)
                                            -Math.pow(coord[2]-30,2);
                                }

                            },
                            100,
                            10,
                            new FixedDoubleParameter(0),
                            random

                    ));

        for(Map.Entry<Fisher,GravitationalSearchAdaptation<double[]>> ada : searchers.entrySet()) {
            ada.getValue().start(model,ada.getKey());
        }

        System.out.println(Arrays.toString(coordinates.values().iterator().next()));

        //step 1000 times
        for(int i=0; i<2000; i++)
            for(Map.Entry<Fisher,GravitationalSearchAdaptation<double[]>> ada : searchers.entrySet()) {
                ada.getValue().adapt(ada.getKey(),model,random);
            }

        System.out.println(Arrays.toString(coordinates.values().iterator().next()));

        DoubleSummaryStatistics one = new DoubleSummaryStatistics();
        DoubleSummaryStatistics two = new DoubleSummaryStatistics();
        DoubleSummaryStatistics three = new DoubleSummaryStatistics();

        for(double[] coordinate : coordinates.values()) {
            one.accept(coordinate[0]);
            two.accept(coordinate[1]);
            three.accept(coordinate[2]);
        }
        assertEquals(one.getAverage(),10,10);
        assertEquals(two.getAverage(),20,10);
        assertEquals(three.getAverage(),30,10);


    }
}
