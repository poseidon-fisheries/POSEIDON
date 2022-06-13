/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LastMomentAbundanceFad;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

public class LastMomentAbundanceFadInitalizerFactory implements
        AlgorithmFactory<FadInitializer<AbundanceLocalBiology, LastMomentAbundanceFad>>, PluggableSelectivity {

    private DoubleParameter daysItTakeToFillUp = new FixedDoubleParameter(35);

    private  DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(10);

    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();

    private  DoubleParameter dudProbability = new FixedDoubleParameter(0.1);

    private HashMap<String,Double> maxCatchabilitiesPerSpecies = new HashMap<>();

    @Override
    public FadInitializer<AbundanceLocalBiology, LastMomentAbundanceFad> apply(FishState fishState) {
        double[] catchabilities = new double[fishState.getBiology().getSize()];
        //todo
        for (Map.Entry<String, Double> catchability : maxCatchabilitiesPerSpecies.entrySet()) {
            catchabilities[fishState.getSpecies(catchability.getKey()).getIndex()] =
                    catchability.getValue();

        }


        return new LastMomentAbundanceFadInitializer(
                daysItTakeToFillUp.apply(fishState.getRandom()).intValue(),
                daysInWaterBeforeAttraction.apply(fishState.getRandom()).intValue(),
                selectivityFilters,
                dudProbability.apply(fishState.getRandom()),
                catchabilities,
                fishState.getBiology()
        );

    }


    public DoubleParameter getDaysItTakeToFillUp() {
        return daysItTakeToFillUp;
    }

    public void setDaysItTakeToFillUp(DoubleParameter daysItTakeToFillUp) {
        this.daysItTakeToFillUp = daysItTakeToFillUp;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        return selectivityFilters;
    }

    public DoubleParameter getDudProbability() {
        return dudProbability;
    }

    public HashMap<String, Double> getMaxCatchabilitiesPerSpecies() {
        return maxCatchabilitiesPerSpecies;
    }

    public void setMaxCatchabilitiesPerSpecies(HashMap<String, Double> maxCatchabilitiesPerSpecies) {
        this.maxCatchabilitiesPerSpecies = maxCatchabilitiesPerSpecies;
    }

    public void setDaysInWaterBeforeAttraction(DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    @Override
    public void setSelectivityFilters(
            Map<Species, NonMutatingArrayFilter> selectivityFilters) {
        this.selectivityFilters = selectivityFilters;
    }

    public void setDudProbability(DoubleParameter dudProbability) {
        this.dudProbability = dudProbability;
    }
}
