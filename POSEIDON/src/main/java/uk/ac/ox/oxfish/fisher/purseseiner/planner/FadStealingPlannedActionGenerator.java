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
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

public class FadStealingPlannedActionGenerator extends
    DrawFromLocationValuePlannedActionGenerator<PlannedAction.OpportunisticFadSet> {

    /**
     * the time it takes to set if something is found
     */
    private final double hoursItTakesToSet;

    /**
     * time spent looking fruitlessly for a FAD when there is none around!
     */
    private final double hoursWastedIfNoFadAround;

    private final double minimumFadValueToSteal;

    private final double probabilityOfFindingOtherFads;

    FadStealingPlannedActionGenerator(
        final Fisher fisher,
        final LocationValues originalLocationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double hoursItTakesToSet,
        final double hoursWastedIfNoFadAround,
        final double minimumFadValueToSteal,
        final double probabilityOfFindingOtherFads
    ) {
        super(fisher, originalLocationValues, map, random);
        this.hoursItTakesToSet = hoursItTakesToSet;
        this.hoursWastedIfNoFadAround = hoursWastedIfNoFadAround;
        this.minimumFadValueToSteal = minimumFadValueToSteal;
        this.probabilityOfFindingOtherFads = probabilityOfFindingOtherFads;
    }

    @Override
    protected PlannedAction.OpportunisticFadSet locationToPlannedAction(final SeaTile location) {
        return new PlannedAction.OpportunisticFadSet(
            drawNewLocation(),
            hoursItTakesToSet,
            hoursWastedIfNoFadAround,
            minimumFadValueToSteal,
            probabilityOfFindingOtherFads
        );
    }
}
