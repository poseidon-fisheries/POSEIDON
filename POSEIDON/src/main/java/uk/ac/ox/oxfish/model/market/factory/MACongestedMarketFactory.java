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

package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MACongestedMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Create Congested Market with Moving Average congestion
 * Created by carrknight on 1/6/16.
 */
public class MACongestedMarketFactory implements AlgorithmFactory<MACongestedMarket> {


    /**
     * demand intercept
     */
    private DoubleParameter demandIntercept = new FixedDoubleParameter(10d);

    /**
     * demand slope
     */
    private DoubleParameter demandSlope = new FixedDoubleParameter(0.001);

    /**
     * moving average size
     */
    private DoubleParameter observationWindow = new FixedDoubleParameter(30);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MACongestedMarket apply(FishState fishState) {
        return new MACongestedMarket(
            demandIntercept.applyAsDouble(fishState.getRandom()),
            demandSlope.applyAsDouble(fishState.getRandom()),
            (int) observationWindow.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getDemandIntercept() {
        return demandIntercept;
    }

    public void setDemandIntercept(DoubleParameter demandIntercept) {
        this.demandIntercept = demandIntercept;
    }

    public DoubleParameter getDemandSlope() {
        return demandSlope;
    }

    public void setDemandSlope(DoubleParameter demandSlope) {
        this.demandSlope = demandSlope;
    }

    public DoubleParameter getObservationWindow() {
        return observationWindow;
    }

    public void setObservationWindow(DoubleParameter observationWindow) {
        this.observationWindow = observationWindow;
    }
}
