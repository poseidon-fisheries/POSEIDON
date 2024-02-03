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

package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.TimeScalarFunction;
import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory.InverseTimeScalarFactory;
import uk.ac.ox.oxfish.fisher.strategies.destination.GeneralizedCognitiveStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * Factory for the Generalized Cognitive Strategy
 * Created by Brian Powers 4/4/2019
 */
public class GeneralizedCognitiveStrategyFactory implements AlgorithmFactory<GeneralizedCognitiveStrategy> {

    private DoubleParameter minAbsoluteSatisfactoryProfit = new FixedDoubleParameter(1);
    private DoubleParameter minRelativeSatisfactoryProfit = new FixedDoubleParameter(0);
    private DoubleParameter weightProfit = new FixedDoubleParameter(1);
    private DoubleParameter weightLegal = new FixedDoubleParameter(1);
    private DoubleParameter weightCommunal = new FixedDoubleParameter(1);
    private DoubleParameter weightReputation = new FixedDoubleParameter(1);
    private AlgorithmFactory<? extends TimeScalarFunction> timeScalarFunction =
        new InverseTimeScalarFactory();
    private DoubleParameter kExploration = new FixedDoubleParameter(.1);

    @Override
    public GeneralizedCognitiveStrategy apply(final FishState state) {
        final MersenneTwisterFast random = state.random;
        return new GeneralizedCognitiveStrategy(
            minAbsoluteSatisfactoryProfit.applyAsDouble(random),
            minRelativeSatisfactoryProfit.applyAsDouble(random),
            weightProfit.applyAsDouble(random),
            weightLegal.applyAsDouble(random),
            weightCommunal.applyAsDouble(random),
            weightReputation.applyAsDouble(random),
            timeScalarFunction.apply(state),
            kExploration.applyAsDouble(random)/*,//kExplore
				numberOfTerritorySites.apply(random)*/
        );
    }
	
/*    private DoubleParameter numberOfTerritorySites = new FixedDoubleParameter(5);
    public DoubleParameter getNumberOfTerritorySites(){
    	return numberOfTerritorySites;
    }
    public void setNumberOfTerritorySites(DoubleParameter nSites){
    	numberOfTerritorySites = nSites;
    }*/

    public DoubleParameter getMinAbsolute() {
        return minAbsoluteSatisfactoryProfit;
    }

    public void setMinAbsolute(final DoubleParameter minAbsolute) {
        minAbsoluteSatisfactoryProfit = minAbsolute;
    }

    public DoubleParameter getMinRelative() {
        return minRelativeSatisfactoryProfit;
    }

    public void setMinRelative(final DoubleParameter minRelative) {
        minRelativeSatisfactoryProfit = minRelative;
    }


    public DoubleParameter getWeightProfit() {
        return weightProfit;
    }

    public void setWeightProfit(final DoubleParameter weightProfit) {
        this.weightProfit = weightProfit;
    }

    public DoubleParameter getWeightLegal() {
        return weightLegal;
    }

    public void setWeightLegal(final DoubleParameter weightLegal) {
        this.weightLegal = weightLegal;
    }

    public DoubleParameter getWeightCommunal() {
        return weightCommunal;
    }

    public void setWeightCommunal(final DoubleParameter weightCommunal) {
        this.weightCommunal = weightCommunal;
    }

    public DoubleParameter getWeightReputation() {
        return weightReputation;
    }

    public void setWeightReputation(final DoubleParameter weightReputation) {
        this.weightReputation = weightReputation;
    }

    public DoubleParameter getKExploration() {
        return kExploration;
    }

    public void setKExploration(final DoubleParameter kExploration) {
        this.kExploration = kExploration;
    }

    /**
     * Getter for property 'timeScalarFunction'.
     *
     * @return Value for property 'timeScalarFunction'.
     */
    public AlgorithmFactory<? extends TimeScalarFunction> getTimeScalarFunction() {
        return timeScalarFunction;
    }

    /**
     * Setter for property 'timeScalarFunction'.
     *
     * @param timeScalarFunction Value to set for property 'timeScalarFunction'.
     */
    public void setTimeScalarFunction(
        final AlgorithmFactory<? extends TimeScalarFunction> timeScalarFunction
    ) {
        this.timeScalarFunction = timeScalarFunction;
    }

}
