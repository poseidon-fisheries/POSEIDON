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
package uk.ac.ox.poseidon.epo.policies;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.ActionType;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.PlannedStrategy;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.PlannedStrategyProxy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class FadDeploymentActionOverrideFactory implements AlgorithmFactory<Startable> {

    private int activationStep;

    @SuppressWarnings("unused")
    public FadDeploymentActionOverrideFactory() {
    }

    @SuppressWarnings("WeakerAccess")
    public FadDeploymentActionOverrideFactory(final int activationStep) {
        this.activationStep = activationStep;
    }

    @SuppressWarnings("unused")
    public int getActivationStep() {
        return activationStep;
    }

    @SuppressWarnings("unused")
    public void setActivationStep(final int activationStep) {
        this.activationStep = activationStep;
    }

    @Override
    public Startable apply(final FishState __) {
        return new FadDeploymentActionOverride(activationStep);
    }

    private static class FadDeploymentActionOverride implements Startable {

        private final int activationStep;

        private FadDeploymentActionOverride(final int activationStep) {
            this.activationStep = activationStep;
        }

        @Override
        public void start(final FishState fishState) {
            fishState.scheduleOnceInXDays(
                simState -> {
                    final ImmutableList<ActionType> overrides =
                        ImmutableList.of(ActionType.DeploymentAction);
                    ((FishState) simState)
                        .getFishers()
                        .stream()
                        .map(Fisher::getDestinationStrategy)
                        .filter(PlannedStrategyProxy.class::isInstance)
                        .map(PlannedStrategyProxy.class::cast)
                        .map(PlannedStrategyProxy::getDelegate)
                        .map(PlannedStrategy::getPlanner)
                        // some vessels (mostly dolphin-setters) have zero empirical deployments
                        // and thus no "plan module" with deployment location preferences, so we
                        // exclude those from the "deploy as much as possible" variation
                        .filter(planner -> planner
                            .getPlanningModules()
                            .containsKey(ActionType.DeploymentAction)
                        )
                        .forEach(planner -> planner.setActionPreferenceOverrides(overrides));
                },
                StepOrder.DAWN,
                activationStep
            );
        }
    }
}
