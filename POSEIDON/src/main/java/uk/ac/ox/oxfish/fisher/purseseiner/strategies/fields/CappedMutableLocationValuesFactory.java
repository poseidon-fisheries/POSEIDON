/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class CappedMutableLocationValuesFactory extends MutableLocationValuesFactory {

    private IntegerParameter maximumNumberOfLocationValues;

    public CappedMutableLocationValuesFactory(
        final InputPath locationValuesFile,
        final DoubleParameter decayRateOfOpportunisticFadSetLocationValues,
        final DoubleParameter decayRateOfNonAssociatedSetLocationValues,
        final DoubleParameter decayRateOfDolphinSetLocationValues,
        final DoubleParameter decayRateOfDeploymentLocationValues,
        final IntegerParameter targetYear,
        final IntegerParameter maximumNumberOfLocationValues
    ) {
        super(
            locationValuesFile,
            decayRateOfOpportunisticFadSetLocationValues,
            decayRateOfNonAssociatedSetLocationValues,
            decayRateOfDolphinSetLocationValues,
            decayRateOfDeploymentLocationValues,
            targetYear
        );
        this.maximumNumberOfLocationValues = maximumNumberOfLocationValues;
    }

    @SuppressWarnings("unused")
    public CappedMutableLocationValuesFactory() {
    }

    @SuppressWarnings("unused")
    public IntegerParameter getMaximumNumberOfLocationValues() {
        return maximumNumberOfLocationValues;
    }

    @SuppressWarnings("unused")
    public void setMaximumNumberOfLocationValues(final IntegerParameter maximumNumberOfLocationValues) {
        this.maximumNumberOfLocationValues = maximumNumberOfLocationValues;
    }

    @Override
    public LocationValueByActionClass apply(final FishState fishState) {
        return new LocationValueByActionClass(
            super
                .apply(fishState)
                .asMap()
                .entrySet()
                .stream()
                .collect(toImmutableMap(
                    Entry::getKey,
                    entry -> {
                        if (entry.getValue() instanceof MutableLocationValues)
                            return new CappedLocationValuesDecorator<>(
                                (MutableLocationValues<?>) entry.getValue(),
                                maximumNumberOfLocationValues.getIntValue()
                            );
                        else return entry.getValue();
                    }
                ))
        );
    }
}
