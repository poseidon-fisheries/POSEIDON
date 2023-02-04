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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Map;

/**
 * A more generic factory for the random catchability trawl. If you want specie 0 to have catchability 0.01
 * specie 2 to have catchability 10 and everything else 0 then catchabilityMap will look like this: "0:0.01,2:10"
 * Created by carrknight on 11/6/15.
 */
public class RandomTrawlStringFactory implements AlgorithmFactory<RandomCatchabilityTrawl> {


    private String catchabilityMap = "0:0.01"; //by default only specie 0 is given any catchability

    private String standardDeviationMap = ""; //by default no standard deviation

    //this is the wrong name for it: it's actually liters per hour fished!
    private DoubleParameter trawlSpeed = new FixedDoubleParameter(5);


    public RandomTrawlStringFactory() {
    }

    public RandomTrawlStringFactory(String catchabilityMap)
    {
        this.catchabilityMap = catchabilityMap;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RandomCatchabilityTrawl apply(FishState state) {

        int species = state.getSpecies().size();
        double[] means = new double[species];
        double[] std = new double[species];

        Map<String, String> catchabilities = Splitter.on(",").withKeyValueSeparator(":").split(catchabilityMap.trim());
        Preconditions.checkArgument(catchabilities.size() > 0, "no catchability!");
        for(Map.Entry<String,String> catchability : catchabilities.entrySet())
        {
            means[Integer.parseInt(catchability.getKey().trim())] = DoubleParameter.parseDoubleParameter(
                    catchability.getValue().trim()
            ).apply(state.getRandom());
        }

        if(standardDeviationMap.contains(":")) {
            Map<String, String> deviations = Splitter.on(",").withKeyValueSeparator(":").split(standardDeviationMap);
            assert !deviations.isEmpty();
                for (Map.Entry<String, String> deviation : deviations.entrySet()) {
                    std[Integer.parseInt(deviation.getKey().trim())] = DoubleParameter.parseDoubleParameter(
                            deviation.getValue().trim()
                    ).apply(state.getRandom());
                }



        }
        return new RandomCatchabilityTrawl(means, std, trawlSpeed.apply(state.getRandom()));

    }

    public String getCatchabilityMap() {
        return catchabilityMap;
    }

    public void setCatchabilityMap(String catchabilityMap) {
        this.catchabilityMap = catchabilityMap;
    }

    public String getStandardDeviationMap() {
        return standardDeviationMap;
    }

    public void setStandardDeviationMap(String standardDeviationMap) {
        this.standardDeviationMap = standardDeviationMap;
    }

    public DoubleParameter getTrawlSpeed() {
        return trawlSpeed;
    }

    public void setTrawlSpeed(DoubleParameter trawlSpeed) {
        this.trawlSpeed = trawlSpeed;
    }
}
