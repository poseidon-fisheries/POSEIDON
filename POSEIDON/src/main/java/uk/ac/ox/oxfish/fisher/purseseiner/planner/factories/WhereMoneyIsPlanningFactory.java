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

package uk.ac.ox.oxfish.fisher.purseseiner.planner.factories;

import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.WhereMoneyIsPlanningModule;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

public class WhereMoneyIsPlanningFactory extends PlanningModuleFactory<WhereMoneyIsPlanningModule> {

    private DoubleParameter valueWeight = new CalibratedParameter(1);

    @Override
    protected WhereMoneyIsPlanningModule makePlanningModule(
        final FishState fishState,
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        return new WhereMoneyIsPlanningModule(
            optionsGenerator,
            valueWeight.applyAsDouble(fishState.getRandom())
        );
    }

    public DoubleParameter getValueWeight() {
        return valueWeight;
    }

    public void setValueWeight(final DoubleParameter valueWeight) {
        this.valueWeight = valueWeight;
    }

}
