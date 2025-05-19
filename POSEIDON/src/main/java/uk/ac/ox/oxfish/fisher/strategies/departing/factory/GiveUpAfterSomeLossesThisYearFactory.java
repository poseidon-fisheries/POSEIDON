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

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.GiveUpAfterSomeLossesThisYearDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class GiveUpAfterSomeLossesThisYearFactory implements AlgorithmFactory<GiveUpAfterSomeLossesThisYearDecorator> {


    private DoubleParameter howManyBadTripsBeforeGivingUp = new FixedDoubleParameter(3);

    private DoubleParameter minimumProfitPerTripRequired = new FixedDoubleParameter(0);

    private AlgorithmFactory<? extends DepartingStrategy> delegate = new MaxHoursPerYearDepartingFactory(9999999);


    @Override
    public GiveUpAfterSomeLossesThisYearDecorator apply(final FishState state) {
        return new GiveUpAfterSomeLossesThisYearDecorator(
            (int) howManyBadTripsBeforeGivingUp.applyAsDouble(state.getRandom()),
            (int) minimumProfitPerTripRequired.applyAsDouble(state.getRandom()),
            delegate.apply(state)
        );
    }


    public DoubleParameter getHowManyBadTripsBeforeGivingUp() {
        return howManyBadTripsBeforeGivingUp;
    }

    public void setHowManyBadTripsBeforeGivingUp(final DoubleParameter howManyBadTripsBeforeGivingUp) {
        this.howManyBadTripsBeforeGivingUp = howManyBadTripsBeforeGivingUp;
    }

    public DoubleParameter getMinimumProfitPerTripRequired() {
        return minimumProfitPerTripRequired;
    }

    public void setMinimumProfitPerTripRequired(final DoubleParameter minimumProfitPerTripRequired) {
        this.minimumProfitPerTripRequired = minimumProfitPerTripRequired;
    }

    public AlgorithmFactory<? extends DepartingStrategy> getDelegate() {
        return delegate;
    }

    public void setDelegate(final AlgorithmFactory<? extends DepartingStrategy> delegate) {
        this.delegate = delegate;
    }
}
