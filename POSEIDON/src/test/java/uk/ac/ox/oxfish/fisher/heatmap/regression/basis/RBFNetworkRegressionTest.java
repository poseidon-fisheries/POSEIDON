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

package uk.ac.ox.oxfish.fisher.heatmap.regression.basis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 3/7/17.
 */
public class RBFNetworkRegressionTest {


    @Test
    public void createsTheCorrectNetwork() throws Exception {


        RBFNetworkRegression regression = new RBFNetworkRegression(
            new ObservationExtractor[]{
                mock(ObservationExtractor.class),
                mock(ObservationExtractor.class),
                mock(ObservationExtractor.class)
            },
            3,
            new double[]{1, 2, 3},
            new double[]{2, 3, 4},
            .1,
            100
        );

        Assertions.assertEquals(regression.getNetwork().size(), 27);

    }


    @Test
    public void getThePicture() throws Exception {

        //if you keep pushing for it, eventually it should learn to predict correctly
        RBFNetworkRegression regression = new RBFNetworkRegression(
            new ObservationExtractor[]{
                new GridXExtractor(),
                new GridYExtractor()
            },
            5,
            new double[]{0, 0},
            new double[]{50, 50},
            100,
            1000
        );


        //let's just assume the reading is always "54"
        SeaTile tile = new SeaTile(10, 10, -100, null);
        double initialPrediction = regression.predict(tile, 0, mock(Fisher.class), mock(FishState.class));
        double initialError = 54 - initialPrediction;

        System.out.println(initialPrediction);
        System.out.println(initialError);

        for (int i = 0; i < 100; i++) {
            regression.addObservation(new GeographicalObservation<>(tile, 0, 54d), mock(Fisher.class),
                mock(FishState.class)
            );
            System.out.println(regression.predict(tile, 0, mock(Fisher.class), mock(FishState.class)));
        }

        Assertions.assertEquals(54, regression.predict(tile, 0, mock(Fisher.class), mock(FishState.class)), .01);

    }
}