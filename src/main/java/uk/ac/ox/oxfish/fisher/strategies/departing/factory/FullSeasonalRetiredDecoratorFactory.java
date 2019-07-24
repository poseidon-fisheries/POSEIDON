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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.EffortStatus;
import uk.ac.ox.oxfish.model.plugins.FullSeasonalRetiredDataCollectors;
import uk.ac.ox.oxfish.fisher.strategies.departing.FullSeasonalRetiredDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Supplier;

public class FullSeasonalRetiredDecoratorFactory implements AlgorithmFactory<FullSeasonalRetiredDecorator> {


    /**
     * otherwise you start seasonal
     */
    private DoubleParameter probabilityStartingFullTime = new FixedDoubleParameter(1);


    /**
     * level of the variable above which you would move from seasonal to full-time (or from retired to seasonal)
     */
    private DoubleParameter targetVariable = new FixedDoubleParameter(100);


    /**
     * level of the variable below which you would go from full-time to seasonal (and from seasonal to retired)
     */
    private DoubleParameter minimumVariable = new FixedDoubleParameter(0);


    private AlgorithmFactory<? extends DepartingStrategy> decorated = new MaxHoursPerYearDepartingFactory();


    private DoubleParameter maxHoursOutWhenSeasonal = new FixedDoubleParameter(100);

    private String variableName = "TRIP_PROFITS_PER_HOUR";


    private DoubleParameter firstYearYouCanSwitch = new FixedDoubleParameter(-1);



    /**
     * makes sure we create only one instance for each data gatherer
     */
    private final Locker<FishState, Startable> dataGatherer = new Locker<>();

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FullSeasonalRetiredDecorator apply(FishState state) {




        double fulltimeProbability = probabilityStartingFullTime.apply(state.getRandom());
        return new FullSeasonalRetiredDecorator(
                state.random.nextDouble()< fulltimeProbability ? EffortStatus.FULLTIME : EffortStatus.SEASONAL,
                targetVariable.apply(state.getRandom()),
                minimumVariable.apply(state.getRandom()),
                maxHoursOutWhenSeasonal.apply(state.getRandom()).intValue(),
                decorated.apply(state),
                variableName,
                firstYearYouCanSwitch.apply(state.getRandom()).intValue()



        );
    }




    /**
     * Getter for property 'probabilityStartingFullTime'.
     *
     * @return Value for property 'probabilityStartingFullTime'.
     */
    public DoubleParameter getProbabilityStartingFullTime() {
        return probabilityStartingFullTime;
    }

    /**
     * Setter for property 'probabilityStartingFullTime'.
     *
     * @param probabilityStartingFullTime Value to set for property 'probabilityStartingFullTime'.
     */
    public void setProbabilityStartingFullTime(DoubleParameter probabilityStartingFullTime) {
        this.probabilityStartingFullTime = probabilityStartingFullTime;
    }

    /**
     * Getter for property 'targetVariable'.
     *
     * @return Value for property 'targetVariable'.
     */
    public DoubleParameter getTargetVariable() {
        return targetVariable;
    }

    /**
     * Setter for property 'targetVariable'.
     *
     * @param targetVariable Value to set for property 'targetVariable'.
     */
    public void setTargetVariable(DoubleParameter targetVariable) {
        this.targetVariable = targetVariable;
    }

    /**
     * Getter for property 'minimumVariable'.
     *
     * @return Value for property 'minimumVariable'.
     */
    public DoubleParameter getMinimumVariable() {
        return minimumVariable;
    }

    /**
     * Setter for property 'minimumVariable'.
     *
     * @param minimumVariable Value to set for property 'minimumVariable'.
     */
    public void setMinimumVariable(DoubleParameter minimumVariable) {
        this.minimumVariable = minimumVariable;
    }

    /**
     * Getter for property 'delegate'.
     *
     * @return Value for property 'delegate'.
     */
    public AlgorithmFactory<? extends DepartingStrategy> getDecorated() {
        return decorated;
    }

    /**
     * Setter for property 'delegate'.
     *
     * @param decorated Value to set for property 'delegate'.
     */
    public void setDecorated(
            AlgorithmFactory<? extends DepartingStrategy> decorated) {
        this.decorated = decorated;
    }

    /**
     * Getter for property 'maxHoursOutWhenSeasonal'.
     *
     * @return Value for property 'maxHoursOutWhenSeasonal'.
     */
    public DoubleParameter getMaxHoursOutWhenSeasonal() {
        return maxHoursOutWhenSeasonal;
    }

    /**
     * Setter for property 'maxHoursOutWhenSeasonal'.
     *
     * @param maxHoursOutWhenSeasonal Value to set for property 'maxHoursOutWhenSeasonal'.
     */
    public void setMaxHoursOutWhenSeasonal(DoubleParameter maxHoursOutWhenSeasonal) {
        this.maxHoursOutWhenSeasonal = maxHoursOutWhenSeasonal;
    }

    /**
     * Getter for property 'variableName'.
     *
     * @return Value for property 'variableName'.
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Setter for property 'variableName'.
     *
     * @param variableName Value to set for property 'variableName'.
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }


    /**
     * Getter for property 'firstYearYouCanSwitch'.
     *
     * @return Value for property 'firstYearYouCanSwitch'.
     */
    public DoubleParameter getFirstYearYouCanSwitch() {
        return firstYearYouCanSwitch;
    }

    /**
     * Setter for property 'firstYearYouCanSwitch'.
     *
     * @param firstYearYouCanSwitch Value to set for property 'firstYearYouCanSwitch'.
     */
    public void setFirstYearYouCanSwitch(DoubleParameter firstYearYouCanSwitch) {
        this.firstYearYouCanSwitch = firstYearYouCanSwitch;
    }
}
