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
 * The classic epsilon greedy bandit
 * Created by carrknight on 11/9/16.
 */
public class EpsilonGreedyBanditAlgorithm implements BanditAlgorithm {


    /**
     * keeps tracks of averages (and number of arms)
     */
    private final BanditAverage averages;

    private double explorationProbability;

    public EpsilonGreedyBanditAlgorithm(BanditAverage averages, double explorationProbability) {
        this.averages = averages;
        this.explorationProbability = explorationProbability;
    }

    @Override
    public int chooseArm(MersenneTwisterFast random) {

        if (random.nextDouble() < explorationProbability)
            return random.nextInt(averages.getNumberOfArms());


        double max = averages.getAverage(0);
        max = Double.isFinite(max) ? max : 0; //if it's NaN turn it into a 0
        ArrayList<Integer> maxIndices = new ArrayList<>();
        maxIndices.add(0);
        for (int i = 1; i < averages.getNumberOfArms(); i++) {
            double average = averages.getAverage(i);
            average = Double.isFinite(average) ? average : 0; //if it's NaN turn it into a 0
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

    @Override
    public void observeReward(double reward, int armPlayed) {

        averages.observeReward(reward, armPlayed);
    }

    /**
     * Getter for property 'explorationProbability'.
     *
     * @return Value for property 'explorationProbability'.
     */
    public double getExplorationProbability() {
        return explorationProbability;
    }

    /**
     * Setter for property 'explorationProbability'.
     *
     * @param explorationProbability Value to set for property 'explorationProbability'.
     */
    public void setExplorationProbability(double explorationProbability) {
        this.explorationProbability = explorationProbability;
    }
}
