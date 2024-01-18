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

import uk.ac.ox.oxfish.fisher.purseseiner.planner.MinimumSetValues;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.ValuePerSetFadModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

public class ValuePerSetPlanningModuleFactory extends PlanningModuleFactory<ValuePerSetFadModule> {

    private DoubleParameter dampen;

    @SuppressWarnings("unused")
    public ValuePerSetPlanningModuleFactory() {
    }

    public ValuePerSetPlanningModuleFactory(
        final AlgorithmFactory<MinimumSetValues> minimumSetValues,
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends MapDiscretizer> discretization,
        final DoubleParameter dampen
    ) {
        super(minimumSetValues, targetYear, discretization);
        this.dampen = dampen;
    }

    @Override
    protected ValuePerSetFadModule makePlanningModule(
        final FishState fishState,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        return new ValuePerSetFadModule(
            optionsGenerator,
            dampen.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getDampen() {
        return dampen;
    }

    public void setDampen(final DoubleParameter dampen) {
        this.dampen = dampen;
    }

}
