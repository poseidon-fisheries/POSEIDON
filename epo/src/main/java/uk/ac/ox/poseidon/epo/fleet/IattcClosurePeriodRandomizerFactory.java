/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.epo.fleet;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class IattcClosurePeriodRandomizerFactory implements AlgorithmFactory<IattcClosurePeriodRandomizer> {

    DoubleParameter proportionOfBoatsInClosureA = new FixedDoubleParameter(0.5);

    public DoubleParameter getProportionOfBoatsInClosureA() {
        return proportionOfBoatsInClosureA;
    }

    @SuppressWarnings("unused")
    public void setProportionOfBoatsInClosureA(final DoubleParameter proportionOfBoatsInClosureA) {
        this.proportionOfBoatsInClosureA = proportionOfBoatsInClosureA;
    }

    @Override
    public IattcClosurePeriodRandomizer apply(final FishState fishState) {
        return new IattcClosurePeriodRandomizer(proportionOfBoatsInClosureA.applyAsDouble(fishState.getRandom()));
    }
}
