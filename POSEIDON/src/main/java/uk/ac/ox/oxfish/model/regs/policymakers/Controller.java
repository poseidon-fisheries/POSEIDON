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

package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * A "generic" controller. I am thinking of something like a PID controller for targeting quotas, but I suppose
 * anything really
 * Created by carrknight on 10/11/16.
 */
public abstract class Controller implements Steppable, Startable {


    private static final long serialVersionUID = -5683828673774977268L;
    /**
     * returns the current value of the variable we are trying to manipulate
     */
    private final Sensor<FishState, Double> observed;

    /**
     * returns the current value we would like the variable to be at
     */
    private final Sensor<FishState, Double> target;


    /**
     * act on policy value
     */
    private final Actuator<FishState, Double> actuator;

    /**
     * how often do we act? (in days)
     */
    private final int intervalInDays;

    /**
     * the last number fed to the actuator
     */
    private double policy = Double.NaN;

    private Stoppable receipt;


    public Controller(
        final Sensor<FishState, Double> observed,
        final Sensor<FishState, Double> target,
        final Actuator<FishState, Double> actuator,
        final int intervalInDays
    ) {
        this.observed = observed;
        this.target = target;
        this.actuator = actuator;
        this.intervalInDays = intervalInDays;
    }

    @Override
    public void step(final SimState simState) {

        final FishState model = (FishState) simState;
        final double currentVariable = observed.scan(model);
        final double currentTarget = target.scan(model);

        policy = computePolicy(currentVariable, currentTarget, model, policy);

        actuator.apply(model, policy, model);


    }

    public abstract double computePolicy(
        double currentVariable, double target,
        FishState model, double oldPolicy
    );


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        Preconditions.checkArgument(receipt == null);
        receipt = model.scheduleEveryXDay(this, StepOrder.POLICY_UPDATE, intervalInDays);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if (receipt != null)
            receipt.stop();
    }

    public double getPolicy() {
        return policy;
    }

    /**
     * Getter for property 'observed'.
     *
     * @return Value for property 'observed'.
     */
    public Sensor<FishState, Double> getObserved() {
        return observed;
    }

    /**
     * Getter for property 'target'.
     *
     * @return Value for property 'target'.
     */
    public Sensor<FishState, Double> getTarget() {
        return target;
    }

    /**
     * Getter for property 'actuator'.
     *
     * @return Value for property 'actuator'.
     */
    public Actuator<FishState, Double> getActuator() {
        return actuator;
    }


    /**
     * Getter for property 'interval'.
     *
     * @return Value for property 'interval'.
     */
    public int getIntervalInDays() {
        return intervalInDays;
    }
}
