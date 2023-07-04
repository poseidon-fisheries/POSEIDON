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

import uk.ac.ox.oxfish.biology.complicated.RepeatingInitialAbundance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by carrknight on 7/11/17.
 */
public class InitialAbundanceFromStringFactory implements AlgorithmFactory<RepeatingInitialAbundance> {


    private String fishPerBinPerSex = "10000000,1000000,10000";


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RepeatingInitialAbundance apply(FishState state) {

        //turn into weights array
        Function<String, Integer> mapper = s -> Integer.parseInt(s.trim());

        Integer[] fish = Arrays.stream(fishPerBinPerSex.split(",")).map(mapper).toArray(Integer[]::new);


        //set up initial abundance array
        double[] abundance = new double[fish.length];
        //fill it up
        for (int bin = 0; bin < fish.length; bin++) {
            abundance[bin] = fish[bin];
        }
        //return it
        return new RepeatingInitialAbundance(abundance);

    }

    /**
     * Getter for property 'fishPerBinPerSex'.
     *
     * @return Value for property 'fishPerBinPerSex'.
     */
    public String getFishPerBinPerSex() {
        return fishPerBinPerSex;
    }

    /**
     * Setter for property 'fishPerBinPerSex'.
     *
     * @param fishPerBinPerSex Value to set for property 'fishPerBinPerSex'.
     */
    public void setFishPerBinPerSex(String fishPerBinPerSex) {
        this.fishPerBinPerSex = fishPerBinPerSex;
    }
}
