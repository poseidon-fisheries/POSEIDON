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
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

/**
 * simply uses DeploymentPlannedActionGenerator to draw a new DPL spot, and returns it as an action
 */
public class DeploymentFromLocationValuePlanningModule
        extends LocationValuePlanningModule {

    public DeploymentFromLocationValuePlanningModule(
            DeploymentLocationValues locationValues,
            NauticalMap map,
            MersenneTwisterFast random) {

        this(locationValues,map,random,0);

    }

    public DeploymentFromLocationValuePlanningModule(
            DeploymentLocationValues locationValues,
            NauticalMap map,
            MersenneTwisterFast random,
            double delayInHoursAfterADeployment) {
        super(locationValues,new DeploymentPlannedActionGenerator(
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
     * @param state
     * @param fisher
     * @return
     */
    @Override
    public int maximumActionsInAPlan(FishState state, Fisher fisher) {
        //you are limited by
        // (1) the amount of fads in your boat
        // (2) the amount of deploy actions you are still allowed to make this year
        // (3) the number of active FAD sets this year
        FadManager<? extends LocalBiology, ? extends AbstractFad<? extends LocalBiology,? extends AbstractFad<?,?>>> fadManager = FadManager.getFadManager(fisher);
        return Math.min(Math.min(
                fadManager.getNumFadsInStock(),
                fadManager.getHowManyActiveFadsCanWeStillDeploy()
                ),
                fadManager.getNumberOfRemainingYearlyActions(FadDeploymentAction.class));
    }
}
