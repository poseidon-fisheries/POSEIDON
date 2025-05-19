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

package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.YamlConstructor;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

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

    public RandomTrawlStringFactory(final String catchabilityMap) {
        this.catchabilityMap = catchabilityMap;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public RandomCatchabilityTrawl apply(final FishState state) {

        final int species = state.getSpecies().size();
        final double[] means = new double[species];
        final double[] std = new double[species];

        final Map<String, String> catchabilities = Splitter.on(",")
            .withKeyValueSeparator(":")
            .split(catchabilityMap.trim());
        Preconditions.checkArgument(catchabilities.size() > 0, "no catchability!");
        for (final Map.Entry<String, String> catchability : catchabilities.entrySet()) {
            means[Integer.parseInt(catchability.getKey().trim())] = YamlConstructor.parseDoubleParameter(
                catchability.getValue().trim()
            ).applyAsDouble(state.getRandom());
        }

        if (standardDeviationMap.contains(":")) {
            final Map<String, String> deviations = Splitter.on(",")
                .withKeyValueSeparator(":")
                .split(standardDeviationMap);
            assert !deviations.isEmpty();
            for (final Map.Entry<String, String> deviation : deviations.entrySet()) {
                std[Integer.parseInt(deviation.getKey().trim())] = YamlConstructor.parseDoubleParameter(
                    deviation.getValue().trim()
                ).applyAsDouble(state.getRandom());
            }


        }
        return new RandomCatchabilityTrawl(means, std, trawlSpeed.applyAsDouble(state.getRandom()));

    }

    public String getCatchabilityMap() {
        return catchabilityMap;
    }

    public void setCatchabilityMap(final String catchabilityMap) {
        this.catchabilityMap = catchabilityMap;
    }

    public String getStandardDeviationMap() {
        return standardDeviationMap;
    }

    public void setStandardDeviationMap(final String standardDeviationMap) {
        this.standardDeviationMap = standardDeviationMap;
    }

    public DoubleParameter getTrawlSpeed() {
        return trawlSpeed;
    }

    public void setTrawlSpeed(final DoubleParameter trawlSpeed) {
        this.trawlSpeed = trawlSpeed;
    }
}
