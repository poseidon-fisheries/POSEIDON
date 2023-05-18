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

package uk.ac.ox.oxfish.utility.adaptation.probability;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * Probability of exploring decreases by fixed value every day, up to a threshold. Imitation is untouched
 * Created by carrknight on 8/28/15.
 */
public class DailyDecreasingProbability implements AdaptationProbability, Steppable {


    private final double dailyDecreaseMultiplier;


    private final FixedProbability probability;

    private final double explorationThreshold;


    public DailyDecreasingProbability(
        double explorationProbability, double imitationProbability,
        double dailyDecreaseMultiplier, double explorationThreshold
    ) {
        this.probability = new FixedProbability(explorationProbability, imitationProbability);
        this.dailyDecreaseMultiplier = dailyDecreaseMultiplier;
        this.explorationThreshold = explorationThreshold;
    }

    @Override
    public double getExplorationProbability() {
        return probability.getExplorationProbability();
    }

    public void setExplorationProbability(double explorationProbability) {
        probability.setExplorationProbability(explorationProbability);
    }

    @Override
    public double getImitationProbability() {
        return probability.getImitationProbability();
    }

    public void setImitationProbability(double imitationProbability) {
        probability.setImitationProbability(imitationProbability);
    }

    @Override
    public void judgeExploration(double previousFitness, double currentFitness) {
        probability.judgeExploration(previousFitness, currentFitness);
    }

    /**
     * schedule yourself to lower exploration rates after a bit.
     *
     * @param model the model
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        model.scheduleEveryDay(
            this,
            StepOrder.DAWN
        );

    }

    @Override
    public void step(SimState simState) {
        probability.
            setExplorationProbability(
                Math.max(
                    probability.getExplorationProbability() * dailyDecreaseMultiplier,
                    explorationThreshold
                ));
    }

    /**
     * tell the startable to turnoff,
     *
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {

    }

    public double getDailyDecreaseMultiplier() {
        return dailyDecreaseMultiplier;
    }

    public double getExplorationThreshold() {
        return explorationThreshold;
    }
}
