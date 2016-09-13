package uk.ac.ox.oxfish.fisher.heatmap.regression;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class PersonalTuningRegressionTest {


    @Test
    public void personalTuning() throws Exception {

        //the right parameters are -2 and 10. Can the tuner find it?

        GeographicalRegression<Double> fake = new GeographicalRegression<Double>() {

            double[] parameters =  new double[2];

            @Override
            public double predict(SeaTile tile, double time, Fisher fisher) {
                return Math.pow(parameters[0]+2,2) + Math.pow(parameters[1]-10,2);
            }

            @Override
            public void addObservation(
                    GeographicalObservation<Double> observation, Fisher fisher) {
                //ignored
            }

            @Override
            public double extractNumericalYFromObservation(
                    GeographicalObservation<Double> observation, Fisher fisher) {
                return 0;
            }

            @Override
            public double[] getParametersAsArray() {
                return parameters;
            }

            @Override
            public void setParameters(double[] parameterArray) {
                parameters = parameterArray;
            }

            @Override
            public void start(FishState model, Fisher fisher) {

            }

            @Override
            public void turnOff(Fisher fisher) {

            }
        };


        PersonalTuningRegression regression = new PersonalTuningRegression(
                    fake,
                    .005,.001,
                    2

        );


        for(int i=0; i<1000; i++)
        {
            regression.addObservation(mock(GeographicalObservation.class),mock(Fisher.class));
            System.out.println(Arrays.toString(regression.getParametersAsArray()));
        }
        //the approach gets really slow so you might not have reached it
        assertEquals(regression.getParametersAsArray()[0],-2,.1);
        assertEquals(regression.getParametersAsArray()[1],10,.5);

    }
}