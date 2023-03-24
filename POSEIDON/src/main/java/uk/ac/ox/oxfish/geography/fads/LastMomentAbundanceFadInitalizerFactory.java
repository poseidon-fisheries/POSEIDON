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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.LastMomentFad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

public class LastMomentAbundanceFadInitalizerFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, ? extends LastMomentFad>> {

    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(10);
    private AbundanceFiltersFactory abundanceFiltersFactory;

    private DoubleParameter daysItTakeToFillUp = new FixedDoubleParameter(35);
    private DoubleParameter dudProbability = new FixedDoubleParameter(0.1);
    private HashMap<String, Double> maxCatchabilitiesPerSpecies = new HashMap<>();
    public LastMomentAbundanceFadInitalizerFactory() {

    }
    public LastMomentAbundanceFadInitalizerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    private DoubleParameter rangeInSeaTiles = new FixedDoubleParameter(0);

    @Override
    public FadInitializer<AbundanceLocalBiology, ? extends LastMomentFad> apply(FishState fishState) {
        double[] catchabilities = new double[fishState.getBiology().getSize()];
        //todo
        for (Map.Entry<String, Double> catchability : maxCatchabilitiesPerSpecies.entrySet()) {
            catchabilities[fishState.getSpecies(catchability.getKey()).getIndex()] =
                catchability.getValue();

        }

        Double range = rangeInSeaTiles.apply(fishState.getRandom());
        final Map<Species, NonMutatingArrayFilter> selectivityFilters =
            abundanceFiltersFactory.apply(fishState)
                .get(FadSetAction.class);
        if (range == null || range.isNaN() || range.intValue() <= 0) {
            return new LastMomentAbundanceFadInitializer(
                daysItTakeToFillUp.apply(fishState.getRandom()).intValue(),
                daysInWaterBeforeAttraction.apply(fishState.getRandom()).intValue(),
                selectivityFilters,
                dudProbability.apply(fishState.getRandom()),
                catchabilities,
                fishState.getBiology()
            );
        } else {

            assert range.intValue()>=1;
            return new LastMomentAbundanceFadWithRangeInitializer<>( daysItTakeToFillUp.apply(fishState.getRandom()).intValue(),
                                                        daysInWaterBeforeAttraction.apply(fishState.getRandom()).intValue(),
                                                        selectivityFilters,
                                                        dudProbability.apply(fishState.getRandom()),
                                                        catchabilities,
                                                        fishState.getBiology(),
                                                                     range.intValue());
        }

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

    public void setDudProbability(DoubleParameter dudProbability) {
        this.dudProbability = dudProbability;
    }

    public DoubleParameter getRangeInSeaTiles() {
        return rangeInSeaTiles;
    }

    public void setRangeInSeaTiles(DoubleParameter rangeInSeaTiles) {
        this.rangeInSeaTiles = rangeInSeaTiles;
    }
}
