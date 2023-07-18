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

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridYExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.KernelTilePredictor;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Created by carrknight on 7/8/16.
 */
@SuppressWarnings("unchecked")
public class KernelTilePredictorTest {


    @Test
    public void kernelSpace() throws Exception {

        final FishState state = MovingTest.generateSimple50x50Map();
        final NauticalMap map = state.getMap();

        final KernelTilePredictor predictor = new KernelTilePredictor(
            1d,
            map.getSeaTile(25, 25),
            entry(
                new GridXExtractor(),
                1d
            ),
            entry(
                new GridYExtractor(),
                1d
            )
        );


        predictor.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(20, 20),
                0d,
                80d
            ),
            null, mock(FishState.class)
        );
        predictor.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(22, 22),
                0d,
                90d
            ),
            null, mock(FishState.class)
        );
        predictor.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(28, 28),
                0d,
                110d
            ),
            null, mock(FishState.class)
        );
        predictor.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(30, 30),
                0d,
                130d
            ),
            null, mock(FishState.class)
        );
        Assert.assertEquals(predictor.getCurrentPrediction(), 100, 5);
    }

    @Test
    public void forgettingVsNotForgetting() throws Exception {

        final FishState state = MovingTest.generateSimple50x50Map();
        final NauticalMap map = state.getMap();

        final KernelTilePredictor forgetting = new KernelTilePredictor(
            .8d,
            map.getSeaTile(25, 25),
            entry(
                new GridXExtractor(),
                1d
            ),
            entry(
                new GridYExtractor(),
                1d
            )
        );
        final KernelTilePredictor notForgetting = new KernelTilePredictor(
            1d,
            map.getSeaTile(25, 25),
            entry(
                new GridXExtractor(),
                1d
            ),
            entry(
                new GridYExtractor(),
                1d
            )
        );

        forgetting.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(25, 25),
                0d,
                80d
            ),
            null, mock(FishState.class)
        );
        notForgetting.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(25, 25),
                0d,
                80d
            ),
            null, mock(FishState.class)
        );

        forgetting.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(25, 25),
                0d,
                100d
            ),
            null, mock(FishState.class)
        );
        notForgetting.addObservation(
            new GeographicalObservation<>(
                map.getSeaTile(25, 25),
                0d,
                100d
            ),
            null, mock(FishState.class)
        );

        System.out.println(forgetting.getCurrentPrediction());
        System.out.println(notForgetting.getCurrentPrediction());
        Assert.assertTrue(forgetting.getCurrentPrediction() > notForgetting.getCurrentPrediction());
    }
}