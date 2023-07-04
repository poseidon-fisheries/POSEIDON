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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

/**
 * Created by carrknight on 7/7/17.
 */
public class ListMeristicFactory implements AlgorithmFactory<FromListMeristics> {

    /**
     * gets turned into a list of doubles
     */
    private String weightsPerBin = ".1,1,5";


    private DoubleParameter mortalityRate = new FixedDoubleParameter(0.08);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FromListMeristics apply(FishState fishState) {


        //turn into weights array
        ToDoubleFunction<String> mapper = s -> Double.parseDouble(s.trim());

        double[] weights = Arrays.stream(weightsPerBin.split(",")).mapToDouble(mapper).toArray();

        //turn into maturity array

        //create a meristic!
        return new FromListMeristics(
            weights, 2);


    }

    /**
     * Getter for property 'weightsPerBin'.
     *
     * @return Value for property 'weightsPerBin'.
     */
    public String getWeightsPerBin() {
        return weightsPerBin;
    }

    /**
     * Setter for property 'weightsPerBin'.
     *
     * @param weightsPerBin Value to set for property 'weightsPerBin'.
     */
    public void setWeightsPerBin(String weightsPerBin) {
        this.weightsPerBin = weightsPerBin;
    }

    /**
     * Getter for property 'mortalityRate'.
     *
     * @return Value for property 'mortalityRate'.
     */
    public DoubleParameter getMortalityRate() {
        return mortalityRate;
    }

    /**
     * Setter for property 'mortalityRate'.
     *
     * @param mortalityRate Value to set for property 'mortalityRate'.
     */
    public void setMortalityRate(DoubleParameter mortalityRate) {
        this.mortalityRate = mortalityRate;
    }

}
