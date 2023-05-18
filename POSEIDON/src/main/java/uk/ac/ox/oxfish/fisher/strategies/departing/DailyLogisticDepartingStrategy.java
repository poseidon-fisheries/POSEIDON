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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Decides to go out based on a given logistic classifier.
 * If the classifier says no, wait 24 hours before continuing
 * Created by carrknight on 12/2/16.
 */
public class DailyLogisticDepartingStrategy implements DepartingStrategy {


    /**
     * the classifier/logit object that does all the work
     */
    private final LogisticClassifier classifier;


    /**
     * whenever you decide not to go out, increment this by one
     */
    private int daysToWait = 0;


    public DailyLogisticDepartingStrategy(LogisticClassifier classifier) {
        this.classifier = classifier;
    }

    /**
     * Getter for property 'daysToWait'.
     *
     * @return Value for property 'daysToWait'.
     */
    public int getDaysToWait() {
        return daysToWait;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        //ignored
    }

    @Override
    public void turnOff(Fisher fisher) {
        //ignored
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher
     * @param model
     * @param random
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
        Fisher fisher, FishState model, MersenneTwisterFast random
    ) {

        //if you are waiting because you failed a check before, keep waiting
        if (fisher.getHoursAtPort() < daysToWait * 24)
            return false;
        //time for a check!
        boolean check = classifier.test(fisher, model, fisher.getHomePort().getLocation(), random);
        if (check)
            daysToWait = 0;
        else
            daysToWait++;
        return check;
    }

    /**
     * Getter for property 'classifier'.
     *
     * @return Value for property 'classifier'.
     */
    public LogisticClassifier getClassifier() {
        return classifier;
    }
}
