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

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

/**
 * simply uses DeploymentPlannedActionGenerator to draw a new DPL spot, and returns it as an action
 */
public class DeploymentFromLocationValuePlanningModule
        implements PlanningModule {

    final private DeploymentLocationValues locationValues;

    final private DeploymentPlannedActionGenerator generator;



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
        this.locationValues = locationValues;
        this.generator = new DeploymentPlannedActionGenerator(
                locationValues,
                map,
                random,
                delayInHoursAfterADeployment

        );
    }

    @Override
    public PlannedAction chooseNextAction(Plan currentPlanSoFar) {


        return generator.drawNewDeployment();
    }

    @Override
    public boolean isStarted() {
        return generator.isReady();
    }

    @Override
    public void start(FishState model, Fisher fisher) {

        //start the location value if needed; else start the generator
        if(!locationValues.hasStarted())
            locationValues.start(model,fisher);
        generator.start();




    }

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     *
     * @param state
     * @param fisher
     */
    @Override
    public void prepareForReplanning(FishState state, Fisher fisher) {
        Preconditions.checkArgument(locationValues.hasStarted());
        generator.start();
    }

    @Override
    public void turnOff(Fisher fisher) {


    }

    /**
     * returns the number of FADs in stock!
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
        FadManager<? extends LocalBiology, ? extends Fad<?, ?>> fadManager = FadManager.getFadManager(fisher);
        return Math.min(Math.min(
                fadManager.getNumFadsInStock(),
                fadManager.getHowManyActiveFadsCanWeStillDeploy()
                ),
                fadManager.getNumberOfRemainingYearlyActions(FadDeploymentAction.class));
    }
}
