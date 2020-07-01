/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.heatmap.acquisition;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Goes through all the possible seatiles and picks the highest one
 * Created by carrknight on 6/28/16.
 */
public class ExhaustiveAcquisitionFunction  implements AcquisitionFunction
{

    private double proportionSearched;

    private final boolean ignoreProtectedAreas;

    private final boolean ignoreWastelands;



    public ExhaustiveAcquisitionFunction(
            double proportionSearched, boolean ignoreProtectedAreas, boolean ignoreWastelands) {
        this.proportionSearched = proportionSearched;
        this.ignoreProtectedAreas = ignoreProtectedAreas;
        this.ignoreWastelands = ignoreWastelands;
    }

    /**
     * Goes through all the possible seatiles and picks the highest one
     * @param map        the map to pick from
     * @param regression the geographical regression
     * @param state  @return a choice
     * @param fisher
     * @param current
     */
    @Override
    public SeaTile pick(
            NauticalMap map, GeographicalRegression regression,
            FishState state, Fisher fisher, SeaTile current) {

        List<SeaTile> seaTiles = map.getAllSeaTilesExcludingLandAsList();

        MersenneTwisterFast random = state.getRandom();
        Collections.shuffle(seaTiles,new Random(random.nextLong()));



        Pair<SeaTile,Double> best;
        if(current!=null)
            best = new Pair<>(current, regression.predict(current, state.getHoursSinceStart(), fisher,state ));
        else
            best = new Pair<>(null,-Double.MAX_VALUE);
        assert Double.isFinite(best.getSecond());
        for(SeaTile tile : seaTiles)
        {
            if(
                    (!ignoreWastelands || tile.isFishingEvenPossibleHere()) &&
                            (!ignoreProtectedAreas || fisher.isAllowedToFishHere(tile,state)) &&
                            random.nextBoolean(proportionSearched))
            {
                double predicted = regression.predict(tile, state.getHoursSinceStart(), fisher,state );
                if(Double.isFinite(predicted) && predicted > best.getSecond())
                    best=new Pair<>(tile,predicted);
            }
        }



        return best.getFirst();

    }
}
