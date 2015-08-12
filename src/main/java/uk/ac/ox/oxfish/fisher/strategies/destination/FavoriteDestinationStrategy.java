package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.actions.Moving;
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
    public void start(FishState model) {

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }

    /**
     * decides where to go.
     *
     * @param fisher        the agent that needs to choose
     * @param random        the randomizer. It probably comes from the fisher but I make explicit it might be needed
     * @param model         the model link
     * @param currentAction what action is the fisher currently taking that prompted to check for destination   @return the destination
     */
    @Override
    public SeaTile chooseDestination(
            Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction) {

        //if we have arrived
        // or
        //we were going to port already
        if(fisher.getLocation().equals(favoriteSpot))
        {
            //go back home
            return fisher.getHomePort().getLocation();
        }
        else
        {
            //if we are going to port, keep going
            if(!fisher.isAtDestination() && fisher.isGoingToPort() )
                return fisher.getHomePort().getLocation();

            //otherwise go/keep going to favorite spot
//            assert  currentAction instanceof Moving || currentAction instanceof AtPort; //we haven't arrived yet. We are either moving or just left dock
            return favoriteSpot;
        }

    }


    public SeaTile getFavoriteSpot() {
        return favoriteSpot;
    }

    public void setFavoriteSpot(SeaTile favoriteSpot) {
        this.favoriteSpot = favoriteSpot;
    }







}


