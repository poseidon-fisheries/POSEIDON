/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.function.ToDoubleFunction;

/**
 * a departing strategy where every year the fisher has to decide whether to be full-time, seasonal or retired.
 * If full-time, it just uses the delegate.
 * If seasonal, it has a maximum number of hours it can go out (and must also satisfy delegate)
 * If retired, it doesn't quit.
 *
 * If full, time it will switch to season if it is making than minimumIncome.
 * If seasonal, it will switch to full time if making more than targetIncome and retire if making less than minimumIncome
 * If retired, it will switch to seasonal if at least one friend is making more than targetIncome
 */
public class FullSeasonalRetiredDecorator implements DepartingStrategy{


    public static final String SEASONALITY_VARIABLE_NAME = "Seasonality";

    private EffortStatus status;


    /**
     * people will increase effort if they are making/expect to make more than this
     */
    private final  double targetIncome;

    /**
     * people will decrease effort if they are making less than this
     */
    private final double minimumIncome;


    /**
     * what we do when full time
     */
    private DepartingStrategy delegate;

    private final MaxHoursPerYearDepartingStrategy seasonalDelegate;


    private Stoppable stoppable;

    private final String targetVariable ;


    public FullSeasonalRetiredDecorator(
            EffortStatus status,
            double targetIncome,
            double minimumIncome,
            int maxHoursOutWhenSeasonal,
            DepartingStrategy delegate, String targetVariable) {
        this.targetVariable = targetVariable;
        Preconditions.checkState(maxHoursOutWhenSeasonal>=0);
        this.status = status;
        this.targetIncome = targetIncome;
        this.minimumIncome = minimumIncome;
        this.delegate = delegate;
        seasonalDelegate = new MaxHoursPerYearDepartingStrategy(maxHoursOutWhenSeasonal);
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
    public boolean shouldFisherLeavePort(Fisher fisher, FishState model, MersenneTwisterFast random) {

        switch (status){
            case FULLTIME:
                return delegate.shouldFisherLeavePort(fisher, model, random);
            case SEASONAL:
                return seasonalDelegate.shouldFisherLeavePort(fisher, model, random) && delegate.shouldFisherLeavePort(fisher, model, random);
            default:
                assert status.equals(EffortStatus.RETIRED);
            case RETIRED:
                return false;
        }


    }

    @Override
    public void start(FishState model, Fisher fisher) {
        Preconditions.checkState(stoppable == null, "Already started!!");
        stoppable = model.scheduleEveryYear(new Steppable() {
            @Override
            public void step(SimState simState) {
                updateEffortLevel(fisher, model);
            }
        }, StepOrder.AFTER_DATA);
        fisher.getAdditionalVariables().put(SEASONALITY_VARIABLE_NAME, status);

    }

    @Override
    public void turnOff(Fisher fisher) {
        stoppable.stop();
    }

    /**
     *  * If full, time it will switch to season if it is making than minimumIncome.
     *  * If seasonal, it will switch to full time if making more than targetIncome and retire if making less than
     *  minimumIncome
     *  * If retired, it will switch to seasonal if at least one friend is making more than targetIncome
     *
     *  Probably no need to call it directly: start will schedule itself
     * @param fisher
     * @param state
     */
    @VisibleForTesting
    public void updateEffortLevel(Fisher fisher, FishState state)
    {

        //don't bother the first year
        if(state.getDay()<365)
            return;


        double currentIncome = fisher.getLatestYearlyObservation(targetVariable);

        switch (status){
            case FULLTIME:
                if(currentIncome<minimumIncome)
                    status = EffortStatus.SEASONAL;
                break;
            case SEASONAL:
                if(currentIncome<minimumIncome)
                    status = EffortStatus.RETIRED;
                else if(currentIncome>targetIncome)
                    status = EffortStatus.FULLTIME;
                break;
            case RETIRED:

                double maxFriendsProfits = fisher.getDirectedFriends().stream().mapToDouble(new ToDoubleFunction<Fisher>() {
                    @Override
                    public double applyAsDouble(Fisher value) {
                        return value.getLatestYearlyObservation(targetVariable);
                    }
                }).max().orElse(Double.NaN);

                if(Double.isFinite(maxFriendsProfits) & maxFriendsProfits>targetIncome)
                    status = EffortStatus.SEASONAL;

                break;
        }

        fisher.getAdditionalVariables().put(SEASONALITY_VARIABLE_NAME,status);


    }


    public double getTargetIncome() {
        return targetIncome;
    }

    public double getMinimumIncome() {
        return minimumIncome;
    }

    public DepartingStrategy getDelegate() {
        return delegate;
    }

    public void setDelegate(DepartingStrategy delegate) {
        this.delegate = delegate;
    }

    public MaxHoursPerYearDepartingStrategy getSeasonalDelegate() {
        return seasonalDelegate;
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public EffortStatus getStatus() {
        return status;
    }
}



