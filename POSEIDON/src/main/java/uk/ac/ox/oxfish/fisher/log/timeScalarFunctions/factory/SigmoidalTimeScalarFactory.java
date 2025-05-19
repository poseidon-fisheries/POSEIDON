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

package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.factory;

import uk.ac.ox.oxfish.fisher.log.timeScalarFunctions.SigmoidalTimeScalar;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

/**
 * A factory to create the sigmoidal time scalar function for use in the Geralized Cognitive Model
 * to scale the impact of old memories of fishing trips
 *
 * @author Brian Powers on 5/3/2019
 */
public class SigmoidalTimeScalarFactory implements AlgorithmFactory<SigmoidalTimeScalar> {
    private DoubleParameter a = new FixedDoubleParameter(1.0);
    private DoubleParameter b = new FixedDoubleParameter(1.0);

    @Override
    public SigmoidalTimeScalar apply(final FishState state) {
        return new SigmoidalTimeScalar(a.applyAsDouble(state.random), b.applyAsDouble(state.random));
    }

    public DoubleParameter getA() {
        return a;
    }

    public void setA(final DoubleParameter a) {
        this.a = a;
    }

    public DoubleParameter getB() {
        return b;
    }

    public void setB(final DoubleParameter b) {
        this.b = b;
    }

}
