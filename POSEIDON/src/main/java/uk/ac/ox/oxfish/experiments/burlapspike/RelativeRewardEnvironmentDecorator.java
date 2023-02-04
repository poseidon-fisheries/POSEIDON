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

package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.valuefunction.QProvider;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

/**
 * A decorator that returns as reward the delegate reward - the qvalue of a baseline action and state.
 * This is a hack to use existing algorithms
 * Created by carrknight on 1/7/17.
 */
public class RelativeRewardEnvironmentDecorator implements Environment {

    private final QProvider provider;

    private final Environment delegate;

    private final State baselineState;

    private final Action baselineAction;


    public RelativeRewardEnvironmentDecorator(
            QProvider provider, Environment delegate,
            State baselineState, Action baselineAction) {
        this.provider = provider;
        this.delegate = delegate;
        this.baselineState = baselineState;
        this.baselineAction = baselineAction;
    }

    /**
     * Returns the current observation of the environment as a {@link State}.
     * @return the current observation of the environment as a {@link State}.
     */
    public State currentObservation() {
        return delegate.currentObservation();
    }

    /**
     * Executes the specified action in this environment
     * @param a the Action that is to be performed in this environment.
     * @return the resulting observation and reward transition from applying the given GroundedAction in this environment.
     */
    public EnvironmentOutcome executeAction(Action a) {
        EnvironmentOutcome environmentOutcome = delegate.executeAction(a);
        environmentOutcome.r = environmentOutcome.r - provider.qValue(baselineState,baselineAction);
        return environmentOutcome;
    }

    /**
     * Returns the last reward returned by the environment
     * @return the last reward returned by the environment
     */
    public double lastReward() {
        return delegate.lastReward() - provider.qValue(baselineState,baselineAction);
    }

    /**
     * Returns whether the environment is in a terminal state that prevents further action by the agent.
     * @return true if the current environment is in a terminal state; false otherwise.
     */
    public boolean isInTerminalState() {
        return delegate.isInTerminalState();
    }

    /**
     * Resets this environment to some initial state, if the functionality exists.
     */
    public void resetEnvironment() {
        delegate.resetEnvironment();
    }

    /**
     * Getter for property 'provider'.
     *
     * @return Value for property 'provider'.
     */
    public QProvider getProvider() {
        return provider;
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public Environment getDelegate() {
        return delegate;
    }

    /**
     * Getter for property 'baselineState'.
     *
     * @return Value for property 'baselineState'.
     */
    public State getBaselineState() {
        return baselineState;
    }

    /**
     * Getter for property 'baselineAction'.
     *
     * @return Value for property 'baselineAction'.
     */
    public Action getBaselineAction() {
        return baselineAction;
    }
}
