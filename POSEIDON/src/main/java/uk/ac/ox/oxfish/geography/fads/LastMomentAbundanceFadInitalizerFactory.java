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
    private DoubleParameter rangeInSeaTiles = new FixedDoubleParameter(0);

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

    @Override
    public FadInitializer<AbundanceLocalBiology, ? extends LastMomentFad> apply(final FishState fishState) {
        final double[] catchabilities = new double[fishState.getBiology().getSize()];
        //todo
        for (final Map.Entry<String, Double> catchability : maxCatchabilitiesPerSpecies.entrySet()) {
            catchabilities[fishState.getSpecies(catchability.getKey()).getIndex()] =
                catchability.getValue();

        }

        final double range = rangeInSeaTiles.applyAsDouble(fishState.getRandom());
        final Map<Species, NonMutatingArrayFilter> selectivityFilters =
            abundanceFiltersFactory.apply(fishState)
                .get(FadSetAction.class);
        if (Double.isNaN(range) || (int) range <= 0) {
            return new LastMomentAbundanceFadInitializer(
                (int) daysItTakeToFillUp.applyAsDouble(fishState.getRandom()),
                (int) daysInWaterBeforeAttraction.applyAsDouble(fishState.getRandom()),
                selectivityFilters,
                dudProbability.applyAsDouble(fishState.getRandom()),
                catchabilities,
                fishState.getBiology()
            );
        } else {

            assert (int) range >= 1;
            return new LastMomentAbundanceFadWithRangeInitializer<>(
                (int) daysItTakeToFillUp.applyAsDouble(fishState.getRandom()),
                (int) daysInWaterBeforeAttraction.applyAsDouble(fishState.getRandom()),
                selectivityFilters,
                dudProbability.applyAsDouble(fishState.getRandom()),
                catchabilities,
                fishState.getBiology(),
                (int) range
            );
        }

    }


    public DoubleParameter getDaysItTakeToFillUp() {
        return daysItTakeToFillUp;
    }

    public void setDaysItTakeToFillUp(final DoubleParameter daysItTakeToFillUp) {
        this.daysItTakeToFillUp = daysItTakeToFillUp;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getDudProbability() {
        return dudProbability;
    }

    public void setDudProbability(final DoubleParameter dudProbability) {
        this.dudProbability = dudProbability;
    }

    public HashMap<String, Double> getMaxCatchabilitiesPerSpecies() {
        return maxCatchabilitiesPerSpecies;
    }

    public void setMaxCatchabilitiesPerSpecies(final HashMap<String, Double> maxCatchabilitiesPerSpecies) {
        this.maxCatchabilitiesPerSpecies = maxCatchabilitiesPerSpecies;
    }

    public DoubleParameter getRangeInSeaTiles() {
        return rangeInSeaTiles;
    }

    public void setRangeInSeaTiles(final DoubleParameter rangeInSeaTiles) {
        this.rangeInSeaTiles = rangeInSeaTiles;
    }
}
