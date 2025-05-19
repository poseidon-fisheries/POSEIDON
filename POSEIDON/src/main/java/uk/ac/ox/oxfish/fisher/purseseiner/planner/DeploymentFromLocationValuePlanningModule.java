/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

/**
 * simply uses DeploymentPlannedActionGenerator to draw a new DPL spot, and returns it as an action
 */
@SuppressWarnings("rawtypes")
public class DeploymentFromLocationValuePlanningModule
    extends LocationValuePlanningModule {

    @SuppressWarnings("unchecked")
    public DeploymentFromLocationValuePlanningModule(
        final Fisher fisher,
        final LocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double delayInHoursAfterADeployment
    ) {
        super(
            locationValues,
            new DeploymentPlannedActionGenerator(
                fisher,
                locationValues,
                map,
                random,
                delayInHoursAfterADeployment
            )
        );
    }

    @Override
    public ActionClass getActionClass() {
        return ActionClass.DPL;
    }

    @Override
    public int numberOfPossibleActions(final Fisher fisher) {
        return getFadManager(fisher).getNumFadsInStock();
    }
}
