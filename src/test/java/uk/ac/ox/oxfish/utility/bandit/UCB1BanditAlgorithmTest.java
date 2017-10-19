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
        assertTrue(bandit.getNumberOfObservations(9)>4000);

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