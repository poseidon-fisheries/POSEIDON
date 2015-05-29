package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

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
            assert currentAction instanceof Arriving; //this should have been called by "arrived"

            //go back home
            return fisher.getHomePort().getLocation();
        }
        else
        {
            //if we are going to port, keep going
            if(fisher.getDestination().equals(fisher.getHomePort().getLocation()) && !fisher.isAtDestination())
                return fisher.getHomePort().getLocation();

            //otherwise go/keep going to favorite spot
            assert  currentAction instanceof Moving || currentAction instanceof AtPort; //we haven't arrived yet. We are either moving or just left dock
            return favoriteSpot;
        }

    }


    public SeaTile getFavoriteSpot() {
        return favoriteSpot;
    }

    public void setFavoriteSpot(SeaTile favoriteSpot) {
        this.favoriteSpot = favoriteSpot;
    }




    /**
     * takes map and randomizer from the state and simply call the constructor of the strategy
     */
    //this factory doesn't get his own class because it doesn't really have setters and getters
    public static  final StrategyFactory<FavoriteDestinationStrategy> RANDOM_FAVORITE_DESTINATION_FACTORY =
        new StrategyFactory<FavoriteDestinationStrategy>() {
            @Override
            public Class<? super FavoriteDestinationStrategy> getStrategySuperClass()
            {
                return  DestinationStrategy.class;
            }

            @Override
            public FavoriteDestinationStrategy apply(FishState state) {

                MersenneTwisterFast random = state.random;
                NauticalMap map = state.getMap();
                return new FavoriteDestinationStrategy(map,random);

            }
        };


    /**
     * Factory expecting an x and y to generate favorite destination. Not terribly useful at the present state
     */
    public static  final FixedFavoriteDestinationFactory FIXED_FAVORITE_DESTINATION_FACTORY =
            new FixedFavoriteDestinationFactory();

}


/***
 *      ___ _   ___ _____ ___  _____   __
 *     | __/_\ / __|_   _/ _ \| _ \ \ / /
 *     | _/ _ \ (__  | || (_) |   /\ V /
 *     |_/_/ \_\___| |_| \___/|_|_\ |_|
 *
 */

/**
 * this factory gets its own class because it has setters and getters which can be found through reflection
 */
class FixedFavoriteDestinationFactory implements  StrategyFactory<FavoriteDestinationStrategy>
{

    /**
     * x grid of the sea tile
     */
    private int x=0;

    /**
     * y grid of the sea tile
     */
    private int y=0;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public Class<? super FavoriteDestinationStrategy> getStrategySuperClass()
    {
        return  DestinationStrategy.class;
    }

    @Override
    public FavoriteDestinationStrategy apply(FishState state) {

        MersenneTwisterFast random = state.random;
        NauticalMap map = state.getMap();
        return new FavoriteDestinationStrategy(map.getSeaTile(x,y));

    }

}