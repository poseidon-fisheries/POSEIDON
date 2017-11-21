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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.complicated.VariableProportionAging;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FixedBoxcarBertalannfyAging implements AlgorithmFactory<FixedBoxcarAging>{



    private DoubleParameter LInfinity= new FixedDoubleParameter(113);

    private DoubleParameter K = new FixedDoubleParameter(.364);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FixedBoxcarAging apply(FishState fishState) {


        return new FixedBoxcarAging(
                K.apply(fishState.getRandom()),
                LInfinity.apply(fishState.getRandom())
        );

    }


    /**
     * Getter for property 'LInfinity'.
     *
     * @return Value for property 'LInfinity'.
     */
    public DoubleParameter getLInfinity() {
        return LInfinity;
    }

    /**
     * Setter for property 'LInfinity'.
     *
     * @param LInfinity Value to set for property 'LInfinity'.
     */
    public void setLInfinity(DoubleParameter LInfinity) {
        this.LInfinity = LInfinity;
    }

    /**
     * Getter for property 'k'.
     *
     * @return Value for property 'k'.
     */
    public DoubleParameter getK() {
        return K;
    }

    /**
     * Setter for property 'k'.
     *
     * @param k Value to set for property 'k'.
     */
    public void setK(DoubleParameter k) {
        K = k;
    }

}
