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

package uk.ac.ox.oxfish.utility.bandit.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.SoftmaxBanditAlgorithm;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 11/11/16.
 */
public class SoftmaxBanditFactory implements
    AlgorithmFactory<BanditSupplier> {


    private DoubleParameter initialTemperature = new FixedDoubleParameter(5d);

    private DoubleParameter temperatureDecay = new FixedDoubleParameter(.98d);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BanditSupplier apply(FishState state) {
        return new BanditSupplier() {
            @Override
            public SoftmaxBanditAlgorithm apply(BanditAverage banditAverage) {
                return new SoftmaxBanditAlgorithm(
                    banditAverage,
                    initialTemperature.applyAsDouble(state.getRandom()),
                    temperatureDecay.applyAsDouble(state.getRandom())
                );
            }
        };
    }

    /**
     * Getter for property 'initialTemperature'.
     *
     * @return Value for property 'initialTemperature'.
     */
    public DoubleParameter getInitialTemperature() {
        return initialTemperature;
    }

    /**
     * Setter for property 'initialTemperature'.
     *
     * @param initialTemperature Value to set for property 'initialTemperature'.
     */
    public void setInitialTemperature(DoubleParameter initialTemperature) {
        this.initialTemperature = initialTemperature;
    }

    /**
     * Getter for property 'temperatureDecay'.
     *
     * @return Value for property 'temperatureDecay'.
     */
    public DoubleParameter getTemperatureDecay() {
        return temperatureDecay;
    }

    /**
     * Setter for property 'temperatureDecay'.
     *
     * @param temperatureDecay Value to set for property 'temperatureDecay'.
     */
    public void setTemperatureDecay(DoubleParameter temperatureDecay) {
        this.temperatureDecay = temperatureDecay;
    }
}
