/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.InterceptExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.SeasonExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Season;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * The logistic decision to go out or not parametrized for Longliners by Steve Saul
 * Created by carrknight on 12/2/16.
 */
public class LonglineFloridaLogisticDepartingFactory implements AlgorithmFactory<DailyLogisticDepartingStrategy> {


    private DoubleParameter intercept = new FixedDoubleParameter(-3.626);

    private DoubleParameter summer = new FixedDoubleParameter(0.439);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @SuppressWarnings("unchecked")
    @Override
    public DailyLogisticDepartingStrategy apply(final FishState state) {

        return new DailyLogisticDepartingStrategy(
            new LogisticClassifier(
                //intercept:
                entry(
                    new InterceptExtractor()
                    , intercept.applyAsDouble(state.getRandom())),
                //summer?:
                entry(
                    new SeasonExtractor(Season.SUMMER)
                    , summer.applyAsDouble(state.getRandom()))
            ));


    }


    /**
     * Getter for property 'intercept'.
     *
     * @return Value for property 'intercept'.
     */
    public DoubleParameter getIntercept() {
        return intercept;
    }

    public void setIntercept(final DoubleParameter intercept) {
        this.intercept = intercept;
    }

    /**
     * Getter for property 'summer'.
     *
     * @return Value for property 'summer'.
     */
    public DoubleParameter getSummer() {
        return summer;
    }

    public void setSummer(final DoubleParameter summer) {
        this.summer = summer;
    }
}
