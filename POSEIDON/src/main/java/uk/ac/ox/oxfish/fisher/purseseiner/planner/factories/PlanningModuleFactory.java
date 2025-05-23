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

import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.MinimumSetValues;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.OwnFadSetDiscretizedActionGenerator;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.PlanningModule;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public abstract class PlanningModuleFactory<T extends PlanningModule> implements AlgorithmFactory<T> {
    private AlgorithmFactory<MinimumSetValues> minimumSetValues;
    private IntegerParameter targetYear;
    /**
     * discretizes map so that when it is time to target FADs you just go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> discretization;

    public PlanningModuleFactory() {
    }

    public PlanningModuleFactory(
        final AlgorithmFactory<MinimumSetValues> minimumSetValues,
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends MapDiscretizer> discretization
    ) {
        this.minimumSetValues = minimumSetValues;
        this.targetYear = targetYear;
        this.discretization = discretization;
    }

    @Override
    public T apply(final FishState fishState) {
        final OwnFadSetDiscretizedActionGenerator optionsGenerator =
            new OwnFadSetDiscretizedActionGenerator(
                new MapDiscretization(getDiscretization().apply(fishState)),
                getMinimumSetValues().apply(fishState).getMinimumSetValue(getTargetYear().getValue(), ActionClass.FAD)
            );
        return makePlanningModule(fishState, optionsGenerator);
    }

    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    public AlgorithmFactory<MinimumSetValues> getMinimumSetValues() {
        return minimumSetValues;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    protected abstract T makePlanningModule(
        FishState fishState,
        OwnFadSetDiscretizedActionGenerator optionsGenerator
    );

    public void setMinimumSetValues(final AlgorithmFactory<MinimumSetValues> minimumSetValues) {
        this.minimumSetValues = minimumSetValues;
    }

    public void setDiscretization(
        final AlgorithmFactory<? extends MapDiscretizer> discretization
    ) {
        this.discretization = discretization;
    }
}
