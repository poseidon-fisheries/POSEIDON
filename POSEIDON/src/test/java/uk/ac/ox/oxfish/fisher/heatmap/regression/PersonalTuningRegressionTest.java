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

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


@SuppressWarnings("unchecked")
public class PersonalTuningRegressionTest {


    @Test
    public void personalTuning() throws Exception {

        //the right parameters are -2 and 10. Can the tuner find it?

        final GeographicalRegression<Double> fake = new GeographicalRegression<Double>() {

            double[] parameters = new double[2];

            @Override
            public double predict(final SeaTile tile, final double time, final Fisher fisher, final FishState model) {
                return Math.pow(parameters[0] + 2, 2) + Math.pow(parameters[1] - 10, 2);
            }

            @Override
            public void addObservation(
                final GeographicalObservation<Double> observation, final Fisher fisher, final FishState model
            ) {
                //ignored
            }

            @Override
            public double extractNumericalYFromObservation(
                final GeographicalObservation<Double> observation, final Fisher fisher
            ) {
                return 0;
            }

            @Override
            public double[] getParametersAsArray() {
                return parameters;
            }

            @Override
            public void setParameters(final double[] parameterArray) {
                parameters = parameterArray;
            }

            @Override
            public void start(final FishState model, final Fisher fisher) {

            }

            @Override
            public void turnOff(final Fisher fisher) {

            }
        };


        final PersonalTuningRegression regression = new PersonalTuningRegression(
            fake,
            .005, .001,
            2

        );


        for (int i = 0; i < 1000; i++) {
            regression.addObservation(mock(GeographicalObservation.class), mock(Fisher.class), mock(FishState.class));
            System.out.println(Arrays.toString(regression.getParametersAsArray()));
        }
        //the approach gets really slow so you might not have reached it
        assertEquals(regression.getParametersAsArray()[0], -2, .1);
        assertEquals(regression.getParametersAsArray()[1], 10, .5);

    }
}