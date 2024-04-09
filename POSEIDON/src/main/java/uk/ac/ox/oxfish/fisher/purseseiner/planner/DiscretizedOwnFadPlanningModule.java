/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.FAD;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public abstract class DiscretizedOwnFadPlanningModule implements PlanningModule {

    private static final int MAX_OWN_FAD_SETS = 1000;
    final protected OwnFadSetDiscretizedActionGenerator optionsGenerator;
    protected double speedInKmPerHours;
    private NauticalMap map;
    private Fisher fisher;

    private FishState fishState;

    public DiscretizedOwnFadPlanningModule(
        final MapDiscretization discretization,
        final double minimumValueOfFadBeforeBeingPickedUp
    ) {
        this(
            new OwnFadSetDiscretizedActionGenerator(
                discretization,
                minimumValueOfFadBeforeBeingPickedUp
            )
        );
    }

    public DiscretizedOwnFadPlanningModule(
        final OwnFadSetDiscretizedActionGenerator optionsGenerator
    ) {
        this.optionsGenerator = optionsGenerator;
    }

    @Override
    public PlannedAction chooseNextAction(final Plan currentPlanSoFar) {

        return chooseFadSet(
            currentPlanSoFar,
            fisher,
            fishState,
            map,
            optionsGenerator
        );

    }

    protected abstract PlannedAction chooseFadSet(
        Plan currentPlanSoFar,
        Fisher fisher,
        FishState model,
        NauticalMap map,
        OwnFadSetDiscretizedActionGenerator optionsGenerator
    );

    @Override
    public void turnOff(final Fisher fisher) {
        map = null;
        this.fisher = null;
        this.fishState = null;

    }

    @Override
    public boolean isStarted() {
        return this.map != null;
    }

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     *
     * @param state
     * @param fisher
     */
    @Override
    public void prepareForReplanning(
        final FishState state,
        final Fisher fisher
    ) {
        start(state, fisher);
        speedInKmPerHours = fisher.getBoat().getSpeedInKph();
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        optionsGenerator.startOrReset(
            getFadManager(fisher),
            model.getRandom(),
            model.getMap()
        );
        map = model.getMap();
        speedInKmPerHours = fisher.getBoat().getSpeedInKph();
        this.fisher = fisher;
        this.fishState = model;

    }

    @Override
    public int numberOfPossibleActions(final Fisher fisher) {
        return getFadManager(fisher).getNumberOfActiveFads();
    }

    @Override
    public ActionClass getActionClass() {
        return FAD;
    }
}
