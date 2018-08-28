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
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * The fisher has one spot they really like and they always go there.
 * Created by carrknight on 4/22/15.
 */
public class FavoriteDestinationStrategy implements DestinationStrategy {

    private SeaTile favoriteSpot;

    /**
     * create the strategy with given destination
     * @param favoriteSpot where the fisher wants to go
     */
    public FavoriteDestinationStrategy(SeaTile favoriteSpot) {
        this.favoriteSpot = favoriteSpot;
    }

    public FavoriteDestinationStrategy(NauticalMap map, MersenneTwisterFast random) {
        this.favoriteSpot = map.getRandomBelowWaterLineSeaTile(random);
    }


    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * tell the startable to turnoff,
     * @param fisher
     */
    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * decides where to go.
     *
     * @param fisher
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random,
            FishState model,
            Action currentAction) {

        //if we have arrived
        if(fisher.getLocation().equals(favoriteSpot))
        {
            //and we are able to fish here, fish here
            if(fisher.canAndWantToFishHere())
                return fisher.getLocation();
            //otherwise go back home
            return fisher.getHomePort().getLocation();
        }
        else
            //we haven't arrived
        {
            //if we are going to port, keep going
            if(!fisher.isAtDestination() && fisher.isGoingToPort() )
                return fisher.getHomePort().getLocation();

            //otherwise go/keep going to favorite spot
            return favoriteSpot;
        }

    }


    public SeaTile getFavoriteSpot() {
        return favoriteSpot;
    }

    public void setFavoriteSpot(SeaTile favoriteSpot) {
        this.favoriteSpot = favoriteSpot;

        Preconditions.checkArgument(this.favoriteSpot.getAltitude()<0);
    }







}


