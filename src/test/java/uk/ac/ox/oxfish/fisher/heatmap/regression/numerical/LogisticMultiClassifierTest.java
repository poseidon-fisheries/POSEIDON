package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 12/5/16.
 */
public class LogisticMultiClassifierTest {

    @Test
    public void logistic() throws Exception
    {

        double[][] beta = new double[2][2];
        beta[0][0] = 1;
        beta[0][1] = 0;
        beta[1][0] = 0;
        beta[1][1] = 0;
        //all intercept
        ObservationExtractor[][] extractor = new ObservationExtractor[2][2];
        extractor[0][0] = (tile, timeOfObservation, agent, model) -> 1;
        extractor[1][0] = extractor[0][0];
        extractor[1][1] = extractor[0][0];
        extractor[0][1] = extractor[0][0];


        LogisticMultiClassifier logit = new LogisticMultiClassifier(beta);
        LogisticInputMaker input = new LogisticInputMaker(extractor);


        assertEquals(0.731058,logit.getProbability(0,
                                                   input.getRegressionInput(mock(Fisher.class),mock(FishState.class))),.001);
        assertEquals(1d-0.731058,logit.getProbability(
                1,input.getRegressionInput(mock(Fisher.class),mock(FishState.class))),.001);


        beta[1][0] = 1;
        assertEquals(0.5,logit.getProbability(
                0,input.getRegressionInput(mock(Fisher.class),mock(FishState.class))),.001);
        assertEquals(0.5,logit.getProbability(
                1,input.getRegressionInput(mock(Fisher.class),mock(FishState.class))),.001);

        MersenneTwisterFast random = new MersenneTwisterFast();
        int chosen1 = 0;
        for(int i=0; i<1000; i++) {
            if(logit.choose(
                    input.getRegressionInput(mock(Fisher.class),mock(FishState.class)), random)==1)
                chosen1++;
        }

        System.out.println(chosen1);
        assertTrue(chosen1>200);
        assertTrue(chosen1<800);

    }
}