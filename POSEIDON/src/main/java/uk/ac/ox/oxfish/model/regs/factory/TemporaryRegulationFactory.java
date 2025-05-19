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

package uk.ac.ox.oxfish.model.regs.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.DecoratedObjectFactory;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class TemporaryRegulationFactory
    extends DecoratedObjectFactory<AlgorithmFactory<? extends Regulation>>
    implements AlgorithmFactory<TemporaryRegulation> {

    private DoubleParameter startDay;
    private DoubleParameter endDay;

    @SuppressWarnings("unused")
    public TemporaryRegulationFactory() {
        super();
    }

    public TemporaryRegulationFactory(
        final AlgorithmFactory<? extends Regulation> delegate,
        final int startDay,
        final int endDay
    ) {
        super(delegate);
        this.startDay = new FixedDoubleParameter(startDay);
        this.endDay = new FixedDoubleParameter(endDay);
    }

    @SuppressWarnings("unused")
    public DoubleParameter getStartDay() {
        return startDay;
    }

    @SuppressWarnings("unused")
    public void setStartDay(final DoubleParameter startDay) {
        this.startDay = startDay;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getEndDay() {
        return endDay;
    }

    @SuppressWarnings("unused")
    public void setEndDay(final DoubleParameter endDay) {
        this.endDay = endDay;
    }

    @Override
    public TemporaryRegulation apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return new TemporaryRegulation(
            getDelegate().apply(fishState),
            (int) startDay.applyAsDouble(rng),
            (int) endDay.applyAsDouble(rng)
        );
    }
}
