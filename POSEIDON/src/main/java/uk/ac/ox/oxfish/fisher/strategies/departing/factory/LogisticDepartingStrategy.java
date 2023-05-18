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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * The base on which logistic strategies are instantiated. It works daily, so for a full day it will always say either
 * true or false according to a logistic probability
 * Created by carrknight on 9/11/15.
 */
public abstract class LogisticDepartingStrategy implements DepartingStrategy, Steppable {


    private final double l;

    private final double k;

    private final double x0;


    private Boolean decisionTaken = null;

    private FishState model;


    public LogisticDepartingStrategy(double l, double k, double x0) {
        this.l = l;
        this.k = k;
        this.x0 = x0;
    }

    public double getL() {
        return l;
    }

    public double getK() {
        return k;
    }

    public double getX0() {
        return x0;
    }


    /**
     * logistic
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

        //you have no countdown activated
        if (decisionTaken == null) {


            double x = computeX(fisher, model);
            x = Math.max(x, 0);

            double dailyProbability = FishStateUtilities.logisticProbability(l, k, x0, x);
            assert dailyProbability >= 0;
            assert dailyProbability <= 1;


            decisionTaken = model.getRandom().nextBoolean(dailyProbability);


            return decisionTaken;

        } else
            return decisionTaken;

    }

    /**
     * abstract method, returns whatever we need to plug in the logistic function
     */
    abstract public double computeX(Fisher fisher, FishState model);


    @Override
    public void start(FishState model, Fisher fisher) {

        this.model = model;
        model.scheduleEveryDay(this, StepOrder.DAWN);
    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    @Override
    public void step(SimState simState) {
        decisionTaken = null;
    }
}

