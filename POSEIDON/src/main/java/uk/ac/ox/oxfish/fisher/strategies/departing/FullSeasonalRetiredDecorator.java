/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.departing;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * a departing strategy where every year the fisher has to decide whether to be full-time, seasonal or retired.
 * If full-time, it just uses the delegate.
 * If seasonal, it has a maximum number of hours it can go out (and must also satisfy delegate)
 * If retired, it doesn't quit.
 * <p>
 * If full, time it will switch to season if it is making than minimumIncome.
 * If seasonal, it will switch to full time if making more than targetIncome and retire if making less than minimumIncome
 * If retired, it will switch to seasonal if at least one friend is making more than targetIncome
 */
public class FullSeasonalRetiredDecorator implements DepartingStrategy {


    public static final String SEASONALITY_VARIABLE_NAME = "Seasonality";
    /**
     * how many consecutive times you need to hit/miss your targets before you change your effort level
     */
    private final int inertia;
    /**
     * people will increase effort if they are making/expect to make more than this
     */
    private final double targetIncome;
    /**
     * people will decrease effort if they are making less than this
     */
    private final double minimumIncome;
    private final MaxHoursPerYearDepartingStrategy seasonalDelegate;
    private final String targetVariable;
    private final int neverSwitchBeforeThisYear;
    private final boolean canReturnFromRetirement;
    private EffortStatus status;
    /**
     * how many times we exceeded our targets (and we would like to increase our effort)
     */
    private int timesTargetsExceeded = 0;
    /**
     * how many times we were below our minimum targets (and we would like to decrease our effort)
     */
    private int timesFailureObserved = 0;
    /**
     * what we do when full time
     */
    private DepartingStrategy delegate;
    private Stoppable stoppable;


    public FullSeasonalRetiredDecorator(
        final EffortStatus status,
        final double targetIncome,
        final double minimumIncome,
        final int maxHoursOutWhenSeasonal,
        final DepartingStrategy delegate, final String targetVariable
    ) {

        this(status, targetIncome, minimumIncome, maxHoursOutWhenSeasonal, delegate, targetVariable, -1);
    }


    public FullSeasonalRetiredDecorator(
        final EffortStatus status,
        final double targetIncome,
        final double minimumIncome,
        final int maxHoursOutWhenSeasonal,
        final DepartingStrategy delegate,
        final String targetVariable,
        final int neverSwitchBeforeThisYear
    ) {
        this(status, targetIncome, minimumIncome, maxHoursOutWhenSeasonal, delegate,
            targetVariable, neverSwitchBeforeThisYear, 1, true
        );

    }

    public FullSeasonalRetiredDecorator(
        final EffortStatus status,
        final double targetIncome,
        final double minimumIncome,
        final int maxHoursOutWhenSeasonal,
        final DepartingStrategy delegate,
        final String targetVariable,
        final int neverSwitchBeforeThisYear,
        final int inertia,
        final boolean canReturnFromRetirement
    ) {
        this.targetVariable = targetVariable;
        this.canReturnFromRetirement = canReturnFromRetirement;
        Preconditions.checkState(maxHoursOutWhenSeasonal >= 0);
        this.status = status;
        this.targetIncome = targetIncome;
        this.minimumIncome = minimumIncome;
        this.delegate = delegate;
        seasonalDelegate = new MaxHoursPerYearDepartingStrategy(maxHoursOutWhenSeasonal);
        this.neverSwitchBeforeThisYear = neverSwitchBeforeThisYear;
        this.inertia = inertia;

        Preconditions.checkState(inertia > 0);
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
    @SuppressWarnings("fallthrough")
    public boolean shouldFisherLeavePort(final Fisher fisher, final FishState model, final MersenneTwisterFast random) {

        switch (status) {
            case FULLTIME:
                return delegate.shouldFisherLeavePort(fisher, model, random);
            case SEASONAL:
                return seasonalDelegate.shouldFisherLeavePort(fisher, model, random) && delegate.shouldFisherLeavePort(
                    fisher,
                    model,
                    random
                );
            default:
                assert status.equals(EffortStatus.RETIRED);
            case RETIRED:
                return false;
        }


    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        Preconditions.checkState(stoppable == null, "Already started!!");
        stoppable = model.scheduleEveryYear(
            (Steppable) simState -> updateEffortLevel(fisher, model),
            StepOrder.AFTER_DATA
        );
        fisher.getAdditionalVariables().put(SEASONALITY_VARIABLE_NAME, status);

    }

    /**
     * * If full, time it will switch to season if it is making than minimumIncome.
     * * If seasonal, it will switch to full time if making more than targetIncome and retire if making less than
     * minimumIncome
     * * If retired, it will switch to seasonal if at least one friend is making more than targetIncome
     * <p>
     * Probably no need to call it directly: start will schedule itself
     *
     * @param fisher
     * @param state
     */
    @VisibleForTesting
    public void updateEffortLevel(final Fisher fisher, final FishState state) {

        //don't bother the first year
        if (state.getDay() < 364)
            return;
        if (state.getYear() < neverSwitchBeforeThisYear)
            return;


        final double currentIncome = fisher.getLatestYearlyObservation(targetVariable);

        //if you couldn't make an observation, you can't adapt!
        if (!Double.isFinite(currentIncome))
            return;
        //update phase (did we meet our targets?)
        switch (status) {
            case FULLTIME:
                if (currentIncome < minimumIncome) {
                    timesFailureObserved++;
                    timesTargetsExceeded = 0;
                } else {
                    timesTargetsExceeded = 0;
                    timesFailureObserved = 0;

                }
                break;
            case SEASONAL:
                if (currentIncome < minimumIncome) {
                    timesFailureObserved++;
                    timesTargetsExceeded = 0;
                } else if (currentIncome > targetIncome) {
                    timesTargetsExceeded++;
                    timesFailureObserved = 0;

                } else {
                    timesTargetsExceeded = 0;
                    timesFailureObserved = 0;
                }
                break;
            case RETIRED:
                break;
        }


        //if you are convinced, convert!

        switch (status) {
            case FULLTIME:
                if (timesFailureObserved >= inertia) {
                    assert timesTargetsExceeded == 0;


                    status = EffortStatus.SEASONAL;
                    timesTargetsExceeded = 0;
                    timesFailureObserved = 0;
                }
                break;
            case SEASONAL:
                if (timesFailureObserved >= inertia) {
                    assert timesTargetsExceeded == 0;

                    status = EffortStatus.RETIRED;
                    timesTargetsExceeded = 0;
                    timesFailureObserved = 0;
                } else if (timesTargetsExceeded >= inertia) {
                    assert timesFailureObserved == 0;
                    status = EffortStatus.FULLTIME;
                    timesTargetsExceeded = 0;
                    timesFailureObserved = 0;
                }
                break;
            case RETIRED:
                if (!canReturnFromRetirement)
                    break;
                final double maxFriendsProfits = fisher.getDirectedFriends()
                    .stream()
                    .mapToDouble(value -> {
                        if (value.getYearlyData().getColumn(targetVariable) == null)
                            throw new RuntimeException("Could not observe" + targetVariable + " for fisher " + value);
                        return value.getLatestYearlyObservation(targetVariable);
                    })
                    .max()
                    .orElse(Double.NaN);

                if (Double.isFinite(maxFriendsProfits) & maxFriendsProfits > targetIncome)
                    status = EffortStatus.SEASONAL;

                break;
        }

        fisher.getAdditionalVariables().put(SEASONALITY_VARIABLE_NAME, status);


    }

    @Override
    public void turnOff(final Fisher fisher) {
        stoppable.stop();
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

    public void setDelegate(final DepartingStrategy delegate) {
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

    public boolean isCanReturnFromRetirement() {
        return canReturnFromRetirement;
    }

    @SuppressWarnings("fallthrough")
    @Override
    public int predictedDaysLeftFishingThisYear(final Fisher fisher, final FishState model, final MersenneTwisterFast random) {

        switch (status) {
            case FULLTIME:
                return delegate.predictedDaysLeftFishingThisYear(fisher, model, random);
            case SEASONAL:
                return seasonalDelegate.predictedDaysLeftFishingThisYear(fisher, model, random);
            default:
                assert false;
            case RETIRED:
                return 0;
        }
    }
}



