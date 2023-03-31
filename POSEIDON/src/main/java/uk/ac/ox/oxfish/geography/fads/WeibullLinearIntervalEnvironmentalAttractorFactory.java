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

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.HeterogeneousLinearIntervalAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * like the other Weibull Linear Interval (fixed step increase in catch per day) but this one will skip the daily attraction
 * step if ANY of the environmental thresholds is not met
 */
public class WeibullLinearIntervalEnvironmentalAttractorFactory
    extends WeibullLinearIntervalAttractorFactory {

    private LinkedList<AdditionalMapFactory> environmentalMaps = new LinkedList<>();
    private LinkedList<DoubleParameter> environmentalThresholds = new LinkedList<>();

    {
        final AdditionalMapFactory e = new AdditionalMapFactory();
        environmentalMaps.add(e);
        environmentalThresholds.add(new FixedDoubleParameter(0.15));
    }

    public WeibullLinearIntervalEnvironmentalAttractorFactory() {
    }

    public WeibullLinearIntervalEnvironmentalAttractorFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final DoubleParameter fadDudRate,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final LinkedHashMap<String, Double> carryingCapacityShapeParameters,
        final LinkedHashMap<String, DoubleParameter> carryingCapacityScaleParameters,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter daysItTakesToFillUp,
        final DoubleParameter minAbundanceThreshold,
        final LinkedList<AdditionalMapFactory> environmentalMaps,
        final LinkedList<DoubleParameter> environmentalThresholds
    ) {
        super(
            abundanceFiltersFactory,
            fadDudRate,
            fishReleaseProbabilityInPercent,
            carryingCapacityShapeParameters,
            carryingCapacityScaleParameters,
            daysInWaterBeforeAttraction,
            daysItTakesToFillUp,
            minAbundanceThreshold
        );
        this.environmentalMaps = environmentalMaps;
        this.environmentalThresholds = environmentalThresholds;
    }

    @NotNull
    @Override
    protected HeterogeneousLinearIntervalAttractor generateFishAttractor(final FishState fishState) {

        //this is called within a locker, so it's safe to assume the startables will only be registered once
        Predicate<SeaTile> passesAllFilters = seaTile -> true;
        for (int environmental = 0; environmental < environmentalMaps.size(); environmental++) {

            final AdditionalStartable newMap = environmentalMaps.get(environmental).apply(fishState);
            fishState.registerStartable(newMap);
            final String mapName = environmentalMaps.get(environmental).mapVariableName;
            final double threshold = environmentalThresholds.get(environmental).applyAsDouble(fishState.getRandom());

            final Predicate<SeaTile> passes = seaTile -> fishState.getMap().getAdditionalMaps().get(
                mapName).get().get(
                seaTile.getGridX(),
                seaTile.getGridY()
            ) >= threshold;

            passesAllFilters = passesAllFilters.and(passes);


        }


        final HeterogeneousLinearIntervalAttractor attractor = super.generateFishAttractor(
            fishState);
        attractor.setAdditionalAttractionHurdle(passesAllFilters);
        return attractor;
    }

    public LinkedList<AdditionalMapFactory> getEnvironmentalMaps() {
        return environmentalMaps;
    }

    public void setEnvironmentalMaps(final LinkedList<AdditionalMapFactory> environmentalMaps) {
        this.environmentalMaps = environmentalMaps;
    }

    public LinkedList<DoubleParameter> getEnvironmentalThresholds() {
        return environmentalThresholds;
    }

    public void setEnvironmentalThresholds(
        final LinkedList<DoubleParameter> environmentalThresholds
    ) {
        this.environmentalThresholds = environmentalThresholds;
    }
}
