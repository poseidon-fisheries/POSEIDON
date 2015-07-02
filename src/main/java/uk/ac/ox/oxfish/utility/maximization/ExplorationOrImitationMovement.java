package uk.ac.ox.oxfish.utility.maximization;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This algorithm has a fixed percentage  to delegate its action to another iterative movement algorithm (exploration) or
 * just copy the best trip of somebody else's you are connected to
 * Created by carrknight on 7/2/15.
 */
public class ExplorationOrImitationMovement implements IterativeMovement {


    /**
     * the iterative process used during exploration
     */
    private final IterativeMovement delegate;


    private  final double probabilityExploring;


    private final boolean ignoreEdgeDirection;

    private final MersenneTwisterFast random;

    /**
     * function that returns a fitness value for a friend of the fisher. Useful for comparison
     */
    private final Function<Fisher,Double> friendFitnessFunction;

    /**
     * function that returns the objective/seatile any friend is currently trying to achieve, needed for imitation
     */
    private final Function<Fisher,SeaTile> friendCurrentTileFunction;

    public ExplorationOrImitationMovement(
            IterativeMovement delegate, double probabilityExploring,
            boolean ignoreEdgeDirection, MersenneTwisterFast random,
            Function<Fisher,Double> friendFitnessFunction,
            Function<Fisher,SeaTile> friendCurrentTileFunction)
    {
        this.delegate = delegate;
        Preconditions.checkArgument(probabilityExploring >= 0);
        Preconditions.checkArgument(probabilityExploring <= 1.0);
        this.probabilityExploring = probabilityExploring;
        this.ignoreEdgeDirection = ignoreEdgeDirection;
        this.random = random;
        this.friendFitnessFunction = friendFitnessFunction;
        this.friendCurrentTileFunction = friendCurrentTileFunction;
    }

    /**
     * decide a new tile to move to given the current and previous step and their fitness
     *
     * @param fisher          the fisher doing the maximization
     * @param previous        the sea-tile tried before this one. Could be null
     * @param current         the sea-tile just tried
     * @param previousFitness the fitness value associated with the old sea-tile, could be NaN
     * @param newFitness      the fitness value associated with the current tile
     * @return a new sea-tile to try
     */
    @Override
    public SeaTile adapt(
            Fisher fisher, SeaTile previous, SeaTile current, double previousFitness, double newFitness) {

        //if we explore:
        if(random.nextBoolean(probabilityExploring))
            return delegate.adapt(fisher, previous, current, previousFitness, newFitness);
        else
        {
            //we are going to imitate, get all friends
            Collection<Fisher> friends = ignoreEdgeDirection ? fisher.getAllFriends() : fisher.getDirectedFriends();
            //if you have no friends go back to exploring
            if(friends.isEmpty())
                return delegate.adapt(fisher, previous, current, previousFitness, newFitness);

            //otherwise get the one with the best profits
            final Optional<Map.Entry<Fisher, Double>> bestFriend = friends.stream().collect(
                    Collectors.toMap((friend) -> friend, friendFitnessFunction)).entrySet().stream().max(
                    Map.Entry.comparingByValue());

            if(bestFriend.isPresent() && bestFriend.get().getValue() > newFitness) //if the best friend knows what he's doing
                return friendCurrentTileFunction.apply(bestFriend.get().getKey());
            else
                //friends suck, go back to exploration
            return delegate.adapt(fisher,previous,current,previousFitness,newFitness);


        }



    }
}
