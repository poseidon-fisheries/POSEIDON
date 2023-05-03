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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.List;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.getValidSeatileFromGroup;

/**
 * A destination strategy that is given a fixed propensity to visit each map location and picks one in proportion
 * by softmax (up to a distance limit!)
 * Created by carrknight on 8/8/17.
 */
public class ClampedDestinationStrategy implements DestinationStrategy, TripListener
{

    private final MapDiscretization discretization;

    private final double distanceMaximum;

    private final double[] propensities;

    private final FavoriteDestinationStrategy delegate;

    private Fisher fisher;

    private FishState state;

    private final boolean respectMPA;

    private final boolean avoidWastelands;


    public ClampedDestinationStrategy(
            FavoriteDestinationStrategy delegate,
            MapDiscretization discretization, double distanceMaximum, double[] propensities
    ) {
        this(delegate,discretization,distanceMaximum,propensities,true,true);
    }

    public ClampedDestinationStrategy(
            FavoriteDestinationStrategy delegate,
            MapDiscretization discretization, double distanceMaximum, double[] propensities,
            boolean respectMPA,
            boolean avoidWastelands
    ){
        Preconditions.checkArgument(propensities.length==discretization.getNumberOfGroups());
        this.discretization = discretization;
        this.distanceMaximum = distanceMaximum;
        this.propensities = propensities;
        this.delegate = delegate;
        this.avoidWastelands = avoidWastelands;
        this.respectMPA = respectMPA;
    }


    /**
     * ignored
     * @param model
     * @param fisher
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
        this.fisher = fisher;
        this.state = model;
        fisher.addTripListener(this);
    }

    /**
     * tell the startable to turnoff,
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {
        if(state != null) {
            delegate.turnOff(fisher);
            fisher.removeTripListener(this);

        }
    }

    /**
     * decides where to go.
     *  @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random, FishState model,
            Action currentAction) {
        return delegate.chooseDestination(fisher, random, model, currentAction);
    }

    @Override
    public void reactToFinishedTrip(TripRecord record, Fisher fisher) {
        double sum = 0;
        MersenneTwisterFast random = state.getRandom();
        NauticalMap map = state.getMap();
        //grab a random seatile for each group
        SeaTile[] candidates = new SeaTile[discretization.getNumberOfGroups()];
        for(int group = 0; group<discretization.getNumberOfGroups(); group++) {
            List<SeaTile> tileGroup = discretization.getGroup(group);
            if(tileGroup.size() > 0)
                candidates[group] = getValidSeatileFromGroup(
                        random,
                        tileGroup,
                        respectMPA,
                        this.fisher,
                        state,
                        avoidWastelands,
                        100
                );
        }
        assert candidates.length ==  propensities.length;
        double[] currentPropensities = Arrays.copyOf(propensities,candidates.length);


        //set propensity to 0 for all tiles further than the max distance

        sum = 0;
        for (int group = 0; group < discretization.getNumberOfGroups(); group++) {
            if (candidates[group] == null ||
                    map.distance(candidates[group], this.fisher.getHomePort().getLocation()) > distanceMaximum)
                currentPropensities[group] = 0;
            else
                sum += currentPropensities[group];
        }

        //turn it into a cumulative distribution
        double[] cdf = new double[currentPropensities.length];
        cdf[0] = currentPropensities[0] / sum;
        for (int i = 1; i < currentPropensities.length; i++)
            cdf[i] = cdf[i - 1] + currentPropensities[i] / sum;


        if(sum>0) {
            int index = Arrays.binarySearch(cdf, random.nextDouble());
            index = (index >= 0) ? index : (-index - 1);
            SeaTile candidate = candidates[index];
            delegate.setFavoriteSpot(candidate);
            return;
        }


    }
}
