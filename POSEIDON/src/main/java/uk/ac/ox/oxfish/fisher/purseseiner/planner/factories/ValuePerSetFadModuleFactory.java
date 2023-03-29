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

package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.ValuePerSetFadModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class ValuePerSetFadModuleFactory implements AlgorithmFactory<ValuePerSetFadModule> {

    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization = new SquaresMapDiscretizerFactory(6, 3);

    private DoubleParameter dampen = new FixedDoubleParameter(-1d);
    //0 gives no bias to the western waters. Increase this to increase the western bias
    private DoubleParameter maxAllowableShear = new FixedDoubleParameter(0.9);

    public DoubleParameter getMaxAllowableShear() {
        return maxAllowableShear;
    }

    public void setMaxAllowableShear(final DoubleParameter maxAllowableShear) {
        this.maxAllowableShear = maxAllowableShear;
    }

    @Override
    public ValuePerSetFadModule apply(final FishState state) {

        final OwnFadSetDiscretizedActionGenerator optionsGenerator =
            new OwnFadSetDiscretizedActionGenerator(
                new MapDiscretization(discretization.apply(state)),
                0,
                maxAllowableShear.applyAsDouble(state.getRandom())
            );
        return new ValuePerSetFadModule(
            optionsGenerator,
            dampen.applyAsDouble(state.getRandom())
        );
    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public void setDiscretization(
        final AlgorithmFactory<? extends MapDiscretizer> discretization
    ) {
        this.discretization = discretization;
    }

    public DoubleParameter getDampen() {
        return dampen;
    }

    public void setDampen(final DoubleParameter dampen) {
        this.dampen = dampen;
    }

}
