/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Goes through all the possible seatiles and picks the highest one
 * Created by carrknight on 6/28/16.
 */
public class ExhaustiveAcquisitionFunction implements AcquisitionFunction {

    private final boolean ignoreProtectedAreas;
    private final boolean ignoreWastelands;
    private final double proportionSearched;


    public ExhaustiveAcquisitionFunction(
        final double proportionSearched, final boolean ignoreProtectedAreas, final boolean ignoreWastelands
    ) {
        this.proportionSearched = proportionSearched;
        this.ignoreProtectedAreas = ignoreProtectedAreas;
        this.ignoreWastelands = ignoreWastelands;
    }

    /**
     * Goes through all the possible seatiles and picks the highest one
     *
     * @param map        the map to pick from
     * @param regression the geographical regression
     * @param state      @return a choice
     * @param fisher
     * @param current
     */
    @Override
    public SeaTile pick(
        final NauticalMap map,
        final GeographicalRegression<?> regression,
        final FishState state,
        final Fisher fisher,
        final SeaTile current
    ) {

        final List<SeaTile> seaTiles = map.getAllSeaTilesExcludingLandAsList();

        final MersenneTwisterFast random = state.getRandom();
        Collections.shuffle(seaTiles, new Random(random.nextLong()));


        Map.Entry<SeaTile, Double> best;
        if (current != null)
            best = entry(current, regression.predict(current, state.getHoursSinceStart(), fisher, state));
        else
            best = entry(null, -Double.MAX_VALUE);
        assert Double.isFinite(best.getValue());
        for (final SeaTile tile : seaTiles) {
            if (
                (!ignoreWastelands || tile.isFishingEvenPossibleHere()) &&
                    (!ignoreProtectedAreas || fisher.isAllowedToFishHere(tile, state)) &&
                    random.nextBoolean(proportionSearched)) {
                final double predicted = regression.predict(tile, state.getHoursSinceStart(), fisher, state);
                if (Double.isFinite(predicted) && predicted > best.getValue())
                    best = entry(tile, predicted);
            }
        }


        return best.getKey();

    }
}
