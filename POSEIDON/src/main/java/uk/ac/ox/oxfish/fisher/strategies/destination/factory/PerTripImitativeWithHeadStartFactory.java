/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.strategies.destination.SelfDestructingIterativeDestinationStarter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class PerTripImitativeWithHeadStartFactory implements
    AlgorithmFactory<SelfDestructingIterativeDestinationStarter> {


    private final PerTripImitativeDestinationFactory delegate =
        new PerTripImitativeDestinationFactory();

    private DoubleParameter maxHoursOut = new FixedDoubleParameter(24 * 5);

    private DoubleParameter fractionOfTilesExploredInHeadStart = new FixedDoubleParameter(.25);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SelfDestructingIterativeDestinationStarter apply(final FishState state) {
        return
            new SelfDestructingIterativeDestinationStarter(

                delegate.apply(state),
                maxHoursOut.applyAsDouble(state.getRandom()),
                fractionOfTilesExploredInHeadStart.applyAsDouble(state.getRandom())

            );
    }

    public DoubleParameter getStepSize() {
        return delegate.getStepSize();
    }

    public void setStepSize(final DoubleParameter stepSize) {
        delegate.setStepSize(stepSize);
    }

    public boolean isIgnoreEdgeDirection() {
        return delegate.isIgnoreEdgeDirection();
    }

    public void setIgnoreEdgeDirection(final boolean ignoreEdgeDirection) {
        delegate.setIgnoreEdgeDirection(ignoreEdgeDirection);
    }

    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return delegate.getProbability();
    }

    public void setProbability(
        final AlgorithmFactory<? extends AdaptationProbability> probability
    ) {
        delegate.setProbability(probability);
    }

    public DoubleParameter getDropInUtilityNeededForUnfriend() {
        return delegate.getDropInUtilityNeededForUnfriend();
    }

    public void setDropInUtilityNeededForUnfriend(
        final DoubleParameter dropInUtilityNeededForUnfriend
    ) {
        delegate.setDropInUtilityNeededForUnfriend(dropInUtilityNeededForUnfriend);
    }

    public boolean isAlwaysCopyBest() {
        return delegate.isAlwaysCopyBest();
    }

    public void setAlwaysCopyBest(final boolean alwaysCopyBest) {
        delegate.setAlwaysCopyBest(alwaysCopyBest);
    }

    /**
     * Getter for property 'objectiveFunction'.
     *
     * @return Value for property 'objectiveFunction'.
     */
    public AlgorithmFactory<? extends ObjectiveFunction<Fisher>> getObjectiveFunction() {
        return delegate.getObjectiveFunction();
    }

    /**
     * Setter for property 'objectiveFunction'.
     *
     * @param objectiveFunction Value to set for property 'objectiveFunction'.
     */
    public void setObjectiveFunction(
        final AlgorithmFactory<? extends ObjectiveFunction<Fisher>> objectiveFunction
    ) {
        delegate.setObjectiveFunction(objectiveFunction);
    }

    /**
     * Getter for property 'backtracksOnBadExploration'.
     *
     * @return Value for property 'backtracksOnBadExploration'.
     */
    public boolean isBacktracksOnBadExploration() {
        return delegate.isBacktracksOnBadExploration();
    }

    /**
     * Setter for property 'backtracksOnBadExploration'.
     *
     * @param backtracksOnBadExploration Value to set for property 'backtracksOnBadExploration'.
     */
    public void setBacktracksOnBadExploration(final boolean backtracksOnBadExploration) {
        delegate.setBacktracksOnBadExploration(backtracksOnBadExploration);
    }

    /**
     * Getter for property 'automaticallyIgnoreMPAs'.
     *
     * @return Value for property 'automaticallyIgnoreMPAs'.
     */
    public boolean isAutomaticallyIgnoreMPAs() {
        return delegate.isAutomaticallyIgnoreMPAs();
    }

    /**
     * Setter for property 'automaticallyIgnoreMPAs'.
     *
     * @param automaticallyIgnoreMPAs Value to set for property 'automaticallyIgnoreMPAs'.
     */
    public void setAutomaticallyIgnoreMPAs(final boolean automaticallyIgnoreMPAs) {
        delegate.setAutomaticallyIgnoreMPAs(automaticallyIgnoreMPAs);
    }

    /**
     * Getter for property 'automaticallyIgnoreAreasWhereFishNeverGrows'.
     *
     * @return Value for property 'automaticallyIgnoreAreasWhereFishNeverGrows'.
     */
    public boolean isAutomaticallyIgnoreAreasWhereFishNeverGrows() {
        return delegate.isAutomaticallyIgnoreAreasWhereFishNeverGrows();
    }

    /**
     * Setter for property 'automaticallyIgnoreAreasWhereFishNeverGrows'.
     *
     * @param automaticallyIgnoreAreasWhereFishNeverGrows Value to set for property
     *                                                    'automaticallyIgnoreAreasWhereFishNeverGrows'.
     */
    public void setAutomaticallyIgnoreAreasWhereFishNeverGrows(final boolean automaticallyIgnoreAreasWhereFishNeverGrows) {
        delegate.setAutomaticallyIgnoreAreasWhereFishNeverGrows(automaticallyIgnoreAreasWhereFishNeverGrows);
    }

    /**
     * Getter for property 'ignoreFailedTrips'.
     *
     * @return Value for property 'ignoreFailedTrips'.
     */
    public boolean isIgnoreFailedTrips() {
        return delegate.isIgnoreFailedTrips();
    }

    /**
     * Setter for property 'ignoreFailedTrips'.
     *
     * @param ignoreFailedTrips Value to set for property 'ignoreFailedTrips'.
     */
    public void setIgnoreFailedTrips(final boolean ignoreFailedTrips) {
        delegate.setIgnoreFailedTrips(ignoreFailedTrips);
    }

    /**
     * Getter for property 'maxInitialDistance'.
     *
     * @return Value for property 'maxInitialDistance'.
     */
    public double getMaxInitialDistance() {
        return delegate.getMaxInitialDistance();
    }

    /**
     * Setter for property 'maxInitialDistance'.
     *
     * @param maxInitialDistance Value to set for property 'maxInitialDistance'.
     */
    public void setMaxInitialDistance(final double maxInitialDistance) {
        delegate.setMaxInitialDistance(maxInitialDistance);
    }

    /**
     * Getter for property 'maxHoursOut'.
     *
     * @return Value for property 'maxHoursOut'.
     */
    public DoubleParameter getMaxHoursOut() {
        return maxHoursOut;
    }

    /**
     * Setter for property 'maxHoursOut'.
     *
     * @param maxHoursOut Value to set for property 'maxHoursOut'.
     */
    public void setMaxHoursOut(final DoubleParameter maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }

    /**
     * Getter for property 'fractionOfTilesExploredInHeadStart'.
     *
     * @return Value for property 'fractionOfTilesExploredInHeadStart'.
     */
    public DoubleParameter getFractionOfTilesExploredInHeadStart() {
        return fractionOfTilesExploredInHeadStart;
    }

    /**
     * Setter for property 'fractionOfTilesExploredInHeadStart'.
     *
     * @param fractionOfTilesExploredInHeadStart Value to set for property 'fractionOfTilesExploredInHeadStart'.
     */
    public void setFractionOfTilesExploredInHeadStart(
        final DoubleParameter fractionOfTilesExploredInHeadStart
    ) {
        this.fractionOfTilesExploredInHeadStart = fractionOfTilesExploredInHeadStart;
    }
}
