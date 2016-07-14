package uk.ac.ox.oxfish.fisher.heatmap.regression;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.SpaceRegressionDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 7/8/16.
 */
public class KernelTilePredictorTest {


    @Test
    public void kernelSpace() throws Exception {

        FishState state = MovingTest.generateSimple50x50Map();
        NauticalMap map = state.getMap();

        KernelTilePredictor predictor = new KernelTilePredictor(
                1d,
                map.getSeaTile(25,25),
                new SpaceRegressionDistance(1d)
        );
        predictor.addObservation(
                new GeographicalObservation<>(
                        map.getSeaTile(20,20),
                        0d,
                        80d
                ),
                null
        );
        predictor.addObservation(
                new GeographicalObservation<>(
                        map.getSeaTile(22,22),
                        0d,
                        90d
                ),
                null
        );
        predictor.addObservation(
                new GeographicalObservation<>(
                        map.getSeaTile(28,28),
                        0d,
                        110d
                ),
                null
        );
        predictor.addObservation(
                new GeographicalObservation<>(
                        map.getSeaTile(30,30),
                        0d,
                        130d
                ),
                null
        );
        Assert.assertEquals(predictor.getCurrentPrediction(),100,5);
    }

    @Test
    public void forgettingVsNotForgetting() throws Exception {

        FishState state = MovingTest.generateSimple50x50Map();
        NauticalMap map = state.getMap();

        KernelTilePredictor forgetting = new KernelTilePredictor(
                .8d,
                map.getSeaTile(25,25),
                new SpaceRegressionDistance(1d)
        );
        KernelTilePredictor notForgetting = new KernelTilePredictor(
                1d,
                map.getSeaTile(25,25),
                new SpaceRegressionDistance(1d)
        );
        forgetting.addObservation(
                 new GeographicalObservation<>(
                        map.getSeaTile(25,25),
                        0d,
                        80d
                ),
                null
        );
        notForgetting.addObservation(
                 new GeographicalObservation<>(
                        map.getSeaTile(25,25),
                        0d,
                        80d
                ),
                null
        );

        forgetting.addObservation(
                new GeographicalObservation<>(
                        map.getSeaTile(25,25),
                        0d,
                        100d
                ),
                null
        );
        notForgetting.addObservation(
                new GeographicalObservation<>(
                        map.getSeaTile(25,25),
                        0d,
                        100d
                ),
                null
        );

        System.out.println(forgetting.getCurrentPrediction());
        System.out.println(notForgetting.getCurrentPrediction());
        Assert.assertTrue(forgetting.getCurrentPrediction()>notForgetting.getCurrentPrediction());
    }
}