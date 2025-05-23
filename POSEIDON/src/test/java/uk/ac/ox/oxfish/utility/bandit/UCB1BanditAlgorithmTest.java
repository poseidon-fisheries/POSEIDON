/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.utility.bandit;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.data.ExponentialMovingAverage;
import uk.ac.ox.oxfish.model.data.IterativeAverage;
import uk.ac.ox.oxfish.model.data.MovingAverage;

/**
 * Created by carrknight on 11/10/16.
 */
public class UCB1BanditAlgorithmTest {


    @Test
    public void tenOptions() throws Exception {

        //option 10 is the best, you should pick it!
        final MersenneTwisterFast random = new MersenneTwisterFast();
        final UCB1BanditAlgorithm bandit = new UCB1BanditAlgorithm(
            -10, 10, new BanditAverage(10, IterativeAverage::new));
        for (int i = 0; i < 10000; i++) { //notice how much slower this method is. It is being extremely cautious
            final int arm = bandit.chooseArm(random);
            final double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        Assertions.assertTrue(bandit.getNumberOfObservations(9) > 5000);

    }


    @Test
    public void tenOptionsEMA() throws Exception {

        //option 10 is the best, you should pick it!
        final MersenneTwisterFast random = new MersenneTwisterFast();
        final UCB1BanditAlgorithm bandit = new UCB1BanditAlgorithm(
            -10, 10, new BanditAverage(10, () -> new ExponentialMovingAverage<>(.8)));

        for (int i = 0; i < 10000; i++) {
            final int arm = bandit.chooseArm(random);
            final double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        Assertions.assertTrue(bandit.getNumberOfObservations(9) > 4000);

    }


    @Test
    public void tenOptionsMA() throws Exception {

        //option 10 is the best, you should pick it!
        final MersenneTwisterFast random = new MersenneTwisterFast();
        final UCB1BanditAlgorithm bandit = new UCB1BanditAlgorithm(
            -10, 10, new BanditAverage(10, () -> new MovingAverage<>(20)));
        for (int i = 0; i < 10000; i++) {
            final int arm = bandit.chooseArm(random);
            final double reward = random.nextGaussian() / 2 + arm;
            bandit.observeReward(reward, arm);
        }

        //now you should be playing most
        System.out.println(bandit.getNumberOfObservations(9));
        Assertions.assertTrue(bandit.getNumberOfObservations(9) > 5000);

    }

}
