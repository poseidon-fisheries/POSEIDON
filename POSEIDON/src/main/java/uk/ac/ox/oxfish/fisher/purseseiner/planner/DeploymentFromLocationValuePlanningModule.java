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

package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.DPL;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

/**
 * simply uses DeploymentPlannedActionGenerator to draw a new DPL spot, and returns it as an action
 */
@SuppressWarnings("rawtypes")
public class DeploymentFromLocationValuePlanningModule
    extends LocationValuePlanningModule {

    public DeploymentFromLocationValuePlanningModule(
        final DeploymentLocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random
    ) {

        this(locationValues, map, random, 0);

    }

    @SuppressWarnings("unchecked")
    public DeploymentFromLocationValuePlanningModule(
        final DeploymentLocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double delayInHoursAfterADeployment
    ) {
        super(locationValues, new DeploymentPlannedActionGenerator(
            locationValues,
            map,
            random,
            delayInHoursAfterADeployment

        ));
    }

    /**
     * returns the minimum between
     * (i) number of FADs in stock
     * (ii) number of active fads we can still deploy
     * (iii) number of allowed deploys this year
     *
     * @param state
     * @param fisher
     * @return
     */
    @Override
    public int maximumActionsInAPlan(final FishState state, final Fisher fisher) {
        final FadManager fadManager = getFadManager(fisher);
        return fadManager.numberOfPermissibleActions(
            DPL,
            fadManager.getNumFadsInStock(),
            state.getRegulations()
        );
    }
}
