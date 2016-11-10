package uk.ac.ox.oxfish.utility.bandit;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.model.data.ExponentialMovingAverage;
import uk.ac.ox.oxfish.model.data.IterativeAverage;
import uk.ac.ox.oxfish.model.data.MovingAverage;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 11/10/16.
 */
public class UCB1BanditAlgorithmTest {



    @Test
    public void tenOptions() throws Exception
    {

        //option 10 is the best, you should pick it!
        MersenneTwisterFast random = new MersenneTwisterFast();
        UCB1BanditAlgorithm bandit = new UCB1BanditAlgorithm(
                -10,10,new BanditAverage(10, IterativeAverage::new)) ;
        for (int i = 0; i < 10000; i++) { //notice how much slower this method is. It is being extremely cautious
            int arm = bandit.chooseArm(random);
            double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        assertTrue(bandit.getNumberOfObservations(9)>5000);

    }



    @Test
    public void tenOptionsEMA() throws Exception
    {

        //option 10 is the best, you should pick it!
        MersenneTwisterFast random = new MersenneTwisterFast();
        UCB1BanditAlgorithm bandit = new UCB1BanditAlgorithm(
                -10,10,new BanditAverage(10, () -> new ExponentialMovingAverage<>(.8))) ;

        for (int i = 0; i < 10000; i++) {
            int arm = bandit.chooseArm(random);
            double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        assertTrue(bandit.getNumberOfObservations(9)>5000);

    }


    @Test
    public void tenOptionsMA() throws Exception
    {

        //option 10 is the best, you should pick it!
        MersenneTwisterFast random = new MersenneTwisterFast();
        UCB1BanditAlgorithm bandit = new UCB1BanditAlgorithm(
                -10,10,new BanditAverage(10, () -> new MovingAverage<>(20))) ;
        for (int i = 0; i < 10000; i++) {
            int arm = bandit.chooseArm(random);
            double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        assertTrue(bandit.getNumberOfObservations(9)>5000);

    }

}