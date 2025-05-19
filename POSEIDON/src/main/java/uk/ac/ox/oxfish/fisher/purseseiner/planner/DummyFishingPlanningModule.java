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
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * a dummy planning module that just picks a randomly non-empty seatile to trawl in for an hour (plus delay)
 */
public class DummyFishingPlanningModule implements PlanningModule {

    private final double delayAfterFishingInHours;

    private final int maximumNumberOfFishingActionsInTrip;

    private final int MAXIMUM_NUMBER_OF_TRIES = 50; // don't keep sampling if you are not allowed anywhere!

    private NauticalMap map;

    private MersenneTwisterFast random;

    private Fisher fisher;

    private FishState model;

    public DummyFishingPlanningModule(
        final double delayAfterFishingInHours,
        final int maximumNumberOfFishingActionsInTrip
    ) {
        this.delayAfterFishingInHours = delayAfterFishingInHours;
        this.maximumNumberOfFishingActionsInTrip = maximumNumberOfFishingActionsInTrip;
    }

    @Override
    public PlannedAction chooseNextAction(final Plan currentPlanSoFar) {

        for (int trial = 0; trial < MAXIMUM_NUMBER_OF_TRIES; trial++) {
            final List<SeaTile> options = map.getAllSeaTilesExcludingLandAsList();
            final SeaTile seaTile = options.get(random.nextInt(options.size()));
            if (seaTile.isFishingEvenPossibleHere() && fisher.isAllowedToFishHere(seaTile, model))
                return new PlannedAction.Fishing(seaTile, delayAfterFishingInHours);
        }
        return null;
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
        assert map != null;
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        map = model.getMap();
        random = model.getRandom();
        this.fisher = fisher;
        this.model = model;
    }

    @Override
    public void turnOff(final Fisher fisher) {
        map = null;
    }

    @Override
    public ActionClass getActionClass() {
        return null;
    }
}
