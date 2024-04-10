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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.OFS;

public class FadStealingFromLocationValuePlanningModule<B extends LocalBiology>
    extends LocationValuePlanningModule {

    FadStealingFromLocationValuePlanningModule(
        final Fisher fisher,
        final LocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double hoursItTakesToSet,
        final double hoursWastedIfNoFadAround,
        final double minimumFadValueToSteal,
        final double probabilityOfFindingOtherFads
    ) {
        this(
            locationValues,
            new FadStealingPlannedActionGenerator(
                fisher,
                locationValues,
                map,
                random,
                hoursItTakesToSet,
                hoursWastedIfNoFadAround,
                minimumFadValueToSteal,
                probabilityOfFindingOtherFads
            )
        );
    }

    private FadStealingFromLocationValuePlanningModule(
        final LocationValues locationValues,
        final FadStealingPlannedActionGenerator generator
    ) {
        super(locationValues, generator);
    }

    @Override
    public ActionClass getActionClass() {
        return OFS;
    }
}
