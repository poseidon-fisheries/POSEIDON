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

import com.beust.jcommander.internal.Lists;
import uk.ac.ox.oxfish.biology.complicated.InitialAbundance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.List;

/**
 * Given a list of integers, assume each represents the bin total for each male and female;
 * so that, for example. 100,200,300 implies 100 male and 100 female of age[0] in the world
 * Created by carrknight on 7/8/17.
 */
public class InitialAbundanceFromListFactory implements AlgorithmFactory<InitialAbundance>
{



    private List<Integer> fishPerBinPerSex = Lists.newArrayList(10000000, 1000000, 10000);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public InitialAbundance apply(FishState state) {

        //set up initial abundance array
        int[][] abundance = new int[2][];
        abundance[0]=new int[fishPerBinPerSex.size()];
        abundance[1]=new int[fishPerBinPerSex.size()];
        //fill it up
        for(int bin=0; bin<fishPerBinPerSex.size(); bin++)
        {
            abundance[0][bin] = fishPerBinPerSex.get(bin);
            abundance[1][bin] = fishPerBinPerSex.get(bin);
        }
        //return it
        return new InitialAbundance(abundance);

    }

    /**
     * Getter for property 'fishPerBinPerSex'.
     *
     * @return Value for property 'fishPerBinPerSex'.
     */
    public List<Integer> getFishPerBinPerSex() {
        return fishPerBinPerSex;
    }

    /**
     * Setter for property 'fishPerBinPerSex'.
     *
     * @param fishPerBinPerSex Value to set for property 'fishPerBinPerSex'.
     */
    public void setFishPerBinPerSex(List<Integer> fishPerBinPerSex) {
        this.fishPerBinPerSex = fishPerBinPerSex;
    }
}
