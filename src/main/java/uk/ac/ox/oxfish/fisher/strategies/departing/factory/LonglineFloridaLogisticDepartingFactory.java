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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.InterceptExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.SeasonExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.Season;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The logistic decision to go out or not parametrized for Longliners by Steve Saul
 * Created by carrknight on 12/2/16.
 */
public class LonglineFloridaLogisticDepartingFactory implements AlgorithmFactory<DailyLogisticDepartingStrategy>
{


    private DoubleParameter intercept = new FixedDoubleParameter(-3.626);

    private DoubleParameter summer = new FixedDoubleParameter(0.439);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DailyLogisticDepartingStrategy apply(FishState state) {

        return new DailyLogisticDepartingStrategy(
                new LogisticClassifier(
                        //intercept:
                        new Pair<>(
                                new InterceptExtractor()
                        ,intercept.apply(state.getRandom())),
                        //summer?:
                        new Pair<>(
                                new SeasonExtractor(Season.SUMMER)
                                ,summer.apply(state.getRandom()))
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

    /**
     * Getter for property 'summer'.
     *
     * @return Value for property 'summer'.
     */
    public DoubleParameter getSummer() {
        return summer;
    }

    public void setIntercept(DoubleParameter intercept) {
        this.intercept = intercept;
    }

    public void setSummer(DoubleParameter summer) {
        this.summer = summer;
    }
}
