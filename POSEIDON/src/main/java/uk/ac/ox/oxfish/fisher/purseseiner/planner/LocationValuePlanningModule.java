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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * an abstract class that deals with planning modules that just repackage the behaviour present in a
 * DrawFromLocationValuePlannedActionGenerator but need to take care into starting the location values with the right
 * fisher
 */
public abstract class LocationValuePlanningModule implements PlanningModule {

    final private LocationValues locationValues;
    final private DrawFromLocationValuePlannedActionGenerator<? extends PlannedAction> generator;

    LocationValuePlanningModule(
        final LocationValues locationValues,
        final DrawFromLocationValuePlannedActionGenerator<? extends PlannedAction> generator
    ) {
        this.locationValues = locationValues;
        this.generator = generator;
    }

    @Override
    public PlannedAction chooseNextAction(final Plan currentPlanSoFar) {
        return generator.drawNewPlannedAction();
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {
        // start the location value if needed; else start the generator
        if (locationValues.getValues() == null)
            locationValues.start(model, fisher);
        generator.init();
    }

    /**
     * this is like the start(...) but gets called when we want the module to be aware that a new plan is starting
     */
    @Override
    public void prepareForReplanning(
        final FishState state,
        final Fisher fisher
    ) {
        Preconditions.checkState(locationValues.getValues() != null);
        generator.init();
    }

    @Override
    public void turnOff(final Fisher fisher) {
        locationValues.turnOff(fisher);
    }

    void removeLocation(final SeaTile location) {
        generator.removeLocation(location);
    }

}
