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

import uk.ac.ox.oxfish.fisher.erotetic.ThresholdAnswer;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SNALSARutilities;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.SimpleEroteticDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 4/11/16.
 */
public class ThresholdEroteticDestinationFactory implements AlgorithmFactory<SimpleEroteticDestinationStrategy> {


    private DoubleParameter minimumObservations = new FixedDoubleParameter(5);

    private DoubleParameter profitThreshold = new FixedDoubleParameter(0);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SimpleEroteticDestinationStrategy apply(FishState state) {
        return new SimpleEroteticDestinationStrategy(
                new ThresholdAnswer<>(
                        minimumObservations.apply(state.getRandom()).intValue(),
                        minimumObservations.apply(state.getRandom()),
                        SNALSARutilities.PROFIT_FEATURE
                ),
                new FavoriteDestinationStrategy(state.getMap(),state.getRandom())

        );
    }

    public DoubleParameter getMinimumObservations() {
        return minimumObservations;
    }

    public void setMinimumObservations(DoubleParameter minimumObservations) {
        this.minimumObservations = minimumObservations;
    }

    public DoubleParameter getProfitThreshold() {
        return profitThreshold;
    }

    public void setProfitThreshold(DoubleParameter profitThreshold) {
        this.profitThreshold = profitThreshold;
    }


}
