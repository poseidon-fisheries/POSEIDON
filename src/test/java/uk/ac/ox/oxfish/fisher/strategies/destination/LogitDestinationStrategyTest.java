package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.MovingTest;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.GridXExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.InterceptExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The logit regression has come to town.
 * Created by carrknight on 12/6/16.
 */
public class LogitDestinationStrategyTest
{


    @Test
    public void logitDestinationStrategy() throws Exception {

        Log.info("logit regression  is tasked to split  and decide over a 4x4 map");

        MersenneTwisterFast random = new MersenneTwisterFast();

        FishState state = MovingTest.generateSimple4x4Map();
        MapDiscretization discretization = new MapDiscretization(new SquaresMapDiscretizer(0,1));
        discretization.discretize(state.getMap());
        //make sure the discretization is correct
        assertTrue(discretization.isValid(0));
        assertTrue(discretization.isValid(1));
        assertEquals(2,discretization.getNumberOfGroups());
        for(int i=0; i<100; i++) {
            SeaTile tile = discretization.getGroup(0).get(random.nextInt(discretization.getGroup(0).size()));
            assertTrue(tile.getGridX()<=1);
        }

        //create the logistic
        //give it more betas than necessary, it will be okay
        //0 has about 75% of being selected  compared to 1
        double[][] beta = new double[3][];
        beta[0]=new double[]{1};
        beta[1]=new double[]{0};
        beta[2]=new double[]{-1}; //should get ignored

        ObservationExtractor[][] extractors = new ObservationExtractor[3][];
        extractors[0]= new ObservationExtractor[]{
                new InterceptExtractor(1d)
        };
        extractors[1]=extractors[0];
        extractors[2]= null;

        LogitDestinationStrategy strategy = new LogitDestinationStrategy(
                beta, extractors,
                Lists.newArrayList(new Integer(0),new Integer(1),new Integer(2)),
                discretization,
                new FavoriteDestinationStrategy(state.getMap().getSeaTile(3,3)),
                random,
                false, false);

        int counter = 0;
        for(int i=0; i<1000; i++)
        {
            strategy.adapt(state,random,mock(Fisher.class));
            SeaTile tile = strategy.getCurrentTarget();
            if(tile.getGridX()<=1)
                counter++;
        }

        System.out.println(counter);
        assertTrue(counter>600);
        assertTrue(counter<900);



    }

    @Test
    public void flipOrderOfSites() throws Exception {


        Log.info("same logistic problem, this time the order of columns is different");

        MersenneTwisterFast random = new MersenneTwisterFast();

        FishState state = MovingTest.generateSimple4x4Map();
        MapDiscretization discretization = new MapDiscretization(new SquaresMapDiscretizer(0, 1));
        discretization.discretize(state.getMap());
        //make sure the discretization is correct
        assertTrue(discretization.isValid(0));
        assertTrue(discretization.isValid(1));
        assertEquals(2, discretization.getNumberOfGroups());
        for (int i = 0; i < 100; i++) {
            SeaTile tile = discretization.getGroup(0).get(random.nextInt(discretization.getGroup(0).size()));
            assertTrue(tile.getGridX() <= 1);
        }

        //create the logistic
        //give it more betas than necessary, it will be okay
        //0 has about 75% of being selected  compared to 1
        double[][] beta = new double[3][];
        beta[0] = new double[]{1};
        beta[1] = new double[]{0};
        beta[2] = new double[]{-1}; //should get ignored

        ObservationExtractor[][] extractors = new ObservationExtractor[3][];
        extractors[0] = new ObservationExtractor[]{
                new InterceptExtractor(1d)
        };
        extractors[1] = extractors[0];
        extractors[2] = null;

        LogitDestinationStrategy strategy = new LogitDestinationStrategy(
                beta, extractors,
                Lists.newArrayList(1, 0, 2),
                discretization,
                new FavoriteDestinationStrategy(state.getMap().getSeaTile(3, 3)),
                random,
                false, false);

        int counter = 0;
        for (int i = 0; i < 10000; i++) {
            strategy.adapt(state, random, mock(Fisher.class));
            SeaTile tile = strategy.getCurrentTarget();
            if (tile.getGridX() <= 1)
                counter++;
        }

        System.out.println(counter);
        assertTrue(counter > 1000);
        assertTrue(counter < 3000);

    }

    @Test
    public void tileExtractorWorks() throws Exception {
        Log.info("logit regression extractor is a function of the X of the seatile, making the second group more likely");

        MersenneTwisterFast random = new MersenneTwisterFast();

        FishState state = MovingTest.generateSimple4x4Map();
        MapDiscretization discretization = new MapDiscretization(new SquaresMapDiscretizer(0,1));
        discretization.discretize(state.getMap());
        //make sure the discretization is correct
        assertTrue(discretization.isValid(0));
        assertTrue(discretization.isValid(1));
        assertEquals(2,discretization.getNumberOfGroups());
        for(int i=0; i<100; i++) {
            SeaTile tile = discretization.getGroup(0).get(random.nextInt(discretization.getGroup(0).size()));
            assertTrue(tile.getGridX()<=1);
        }

        //create the logistic
        //give it more betas than necessary, it will be okay
        //0 is unlikely to be chosen
        double[][] beta = new double[3][];
        beta[0]=new double[]{1};
        beta[1]=new double[]{1};
        beta[2]=new double[]{-1}; //should get ignored

        ObservationExtractor[][] extractors = new ObservationExtractor[3][];
        extractors[0]= new ObservationExtractor[]{
                new GridXExtractor()
        };
        extractors[1]=extractors[0];
        extractors[2]= null;

        int counter = 0;

        LogitDestinationStrategy strategy = new LogitDestinationStrategy(
                beta, extractors,
                Lists.newArrayList(1,0,2),
                discretization,
                new FavoriteDestinationStrategy(state.getMap().getSeaTile(3,3)),
                random,
                false, false);

        for(int i=0; i<1000; i++)
        {
            strategy.adapt(state,random,mock(Fisher.class));
            SeaTile tile = strategy.getCurrentTarget();
            if(tile.getGridX()<=1)
                counter++;
        }

        System.out.println(counter);
        assertTrue(counter>50);
        assertTrue(counter<200);
    }
}