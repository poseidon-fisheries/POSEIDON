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

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public class MutableLocationValuesFactory extends LocationValuesFactory {
    private DoubleParameter decayRateOfOpportunisticFadSetLocationValues;
    private DoubleParameter decayRateOfNonAssociatedSetLocationValues;
    private DoubleParameter decayRateOfDolphinSetLocationValues;
    private DoubleParameter decayRateOfDeploymentLocationValues;

    @SuppressWarnings("WeakerAccess")
    public MutableLocationValuesFactory(
        final InputPath locationValuesFile,
        final DoubleParameter decayRateOfOpportunisticFadSetLocationValues,
        final DoubleParameter decayRateOfNonAssociatedSetLocationValues,
        final DoubleParameter decayRateOfDolphinSetLocationValues,
        final DoubleParameter decayRateOfDeploymentLocationValues,
        final IntegerParameter targetYear
    ) {
        super(locationValuesFile, targetYear);
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public MutableLocationValuesFactory() {
    }

    @Override
    public LocationValueByActionClass apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new LocationValueByActionClass(
            ImmutableMap.<Class<? extends PurseSeinerAction>, LocationValues>builder()
                .put(
                    FadSetAction.class,
                    new FadLocationValues()
                )
                .put(
                    OpportunisticFadSetAction.class,
                    new OpportunisticFadSetLocationValues(
                        fisher -> loadLocationValues(fisher, OpportunisticFadSetAction.class),
                        getDecayRateOfOpportunisticFadSetLocationValues().applyAsDouble(rng)
                    )
                )
                .put(
                    NonAssociatedSetAction.class,
                    new NonAssociatedSetLocationValues(
                        fisher -> loadLocationValues(fisher, NonAssociatedSetAction.class),
                        getDecayRateOfNonAssociatedSetLocationValues().applyAsDouble(rng)
                    )
                ).put(
                    DolphinSetAction.class,
                    new DolphinSetLocationValues(
                        fisher -> loadLocationValues(fisher, DolphinSetAction.class),
                        getDecayRateOfDolphinSetLocationValues().applyAsDouble(rng)
                    )
                )
                .put(
                    FadDeploymentAction.class,
                    new DeploymentLocationValues(
                        fisher -> loadLocationValues(fisher, FadDeploymentAction.class),
                        getDecayRateOfDeploymentLocationValues().applyAsDouble(rng)
                    )
                )
                .build()
        );
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfOpportunisticFadSetLocationValues() {
        return decayRateOfOpportunisticFadSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfOpportunisticFadSetLocationValues(final DoubleParameter decayRateOfOpportunisticFadSetLocationValues) {
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfNonAssociatedSetLocationValues() {
        return decayRateOfNonAssociatedSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfNonAssociatedSetLocationValues(final DoubleParameter decayRateOfNonAssociatedSetLocationValues) {
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfDolphinSetLocationValues() {
        return decayRateOfDolphinSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfDolphinSetLocationValues(final DoubleParameter decayRateOfDolphinSetLocationValues) {
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public DoubleParameter getDecayRateOfDeploymentLocationValues() {
        return decayRateOfDeploymentLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfDeploymentLocationValues(final DoubleParameter decayRateOfDeploymentLocationValues) {
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

}
