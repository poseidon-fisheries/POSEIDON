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
public class SoftmaxBanditAlgorithmTest {


    @Test
    public void tenOptions() throws Exception
    {

            //option 10 is the best, you should pick it!
            MersenneTwisterFast random = new MersenneTwisterFast();
            SoftmaxBanditAlgorithm bandit = new SoftmaxBanditAlgorithm(
                    new BanditAverage(10, IterativeAverage::new), 20, .98);
            for (int i = 0; i < 2000; i++) {
                int arm = bandit.chooseArm(random);
                double reward = random.nextGaussian() / 2 + arm;
                bandit.observeReward(reward, arm);
            }

            //now you should be playing most
            System.out.println(bandit.getNumberOfObservations(9));
            //sometimes it gets stuck at 8 rather than 9
            assertTrue(bandit.getNumberOfObservations(9) + bandit.getNumberOfObservations(8)> 1000);

    }



    @Test
    public void tenOptionsEMA() throws Exception
    {


        //option 10 is the best, you should pick it!
        MersenneTwisterFast random = new MersenneTwisterFast();
        SoftmaxBanditAlgorithm bandit = new SoftmaxBanditAlgorithm(
                new BanditAverage(10, () -> new ExponentialMovingAverage<>(.8)), 50, .975);
        for (int i = 0; i < 1000; i++) {
            int arm = bandit.chooseArm(random);
            double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        assertTrue(bandit.getNumberOfObservations(9) > 500);


    }


    @Test
    public void tenOptionsMA() throws Exception
    {

        //About 1 out of 5000 trials fail

        //option 10 is the best, you should pick it!
        MersenneTwisterFast random = new MersenneTwisterFast();
        SoftmaxBanditAlgorithm bandit = new SoftmaxBanditAlgorithm(
                new BanditAverage(10, () -> new MovingAverage<>(20)), 50, .975);
        for (int i = 0; i < 1000; i++) {
            int arm = bandit.chooseArm(random);
            double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        assertTrue(bandit.getNumberOfObservations(9) > 500);


    }

}