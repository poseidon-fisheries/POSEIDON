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

import java.util.ArrayList;

/**
 * Classic UCB1 algorithm. We need to scale down the rewards to a 0-1 range though
 * Created by carrknight on 11/10/16.
 */
public class UCB1BanditAlgorithm implements BanditAlgorithm {

    private final double maxReward;

    private final double minReward;
    private final BanditAverage averages;
    private int numberOfObservations = 0;


    public UCB1BanditAlgorithm(double minReward, double maxReward, BanditAverage average) {
        this.maxReward = maxReward;
        this.minReward = minReward;
        this.averages = average;
    }

    @Override
    public int chooseArm(MersenneTwisterFast random) {

        //if there is an option without a single played game, play that first
        for (int i = 0; i < averages.getNumberOfArms(); i++)
            if (averages.getNumberOfObservations(i) == 0)
                return i;

        assert numberOfObservations >= averages.getNumberOfArms();
        //now pick the one with the highest confidence bound
        double max = upperConfidenceBound(0);
        ArrayList<Integer> maxIndices = new ArrayList<>();
        maxIndices.add(0);
        for (int i = 1; i < averages.getNumberOfArms(); i++) {
            double average = upperConfidenceBound(i);
            if (average > max) {
                max = average;
                maxIndices = new ArrayList<>();
                maxIndices.add(i);
            } else if (average == max)
                maxIndices.add(i);
        }
        assert maxIndices.size() > 0;
        return maxIndices.get(random.nextInt(maxIndices.size()));


    }

    private double upperConfidenceBound(final int arm) {
        return averages.getAverage(arm) +
            Math.sqrt(2 * Math.log(numberOfObservations) / averages.getNumberOfObservations(arm));
    }

    @Override
    public void observeReward(double reward, int armPlayed) {

        //rescale
        reward = Math.min(Math.max(reward, minReward), maxReward);
        reward = (reward - minReward) / (maxReward - minReward);
        averages.observeReward(reward, armPlayed);
        numberOfObservations++;
    }


    public int getNumberOfObservations() {
        return numberOfObservations;
    }

    public int getNumberOfObservations(int arm) {
        return averages.getNumberOfObservations(arm);
    }

    public double getAverage(int arm) {
        return averages.getAverage(arm);
    }

    public int getNumberOfArms() {
        return averages.getNumberOfArms();
    }
}

