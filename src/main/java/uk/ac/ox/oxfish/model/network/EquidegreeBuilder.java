package uk.ac.ox.oxfish.model.network;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import javafx.collections.ObservableList;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.*;

/**
 * Builds network where everyone has the same out-degree of edges
 * Created by carrknight on 7/1/15.
 */
public class EquidegreeBuilder implements NetworkBuilder{

    private DoubleParameter degree = new FixedDoubleParameter(2d);

    /**
     * list of additional conditions to pass before allowing friendship
     */
    private final LinkedList<NetworkPredicate> predicates = new LinkedList<>();


    /**
     * when this is false then do not allow both A->B and B->A connections
     */
    private boolean allowMutualFriendships = true;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DirectedGraph<Fisher, FriendshipEdge> apply(FishState state) {

        DirectedGraph<Fisher,FriendshipEdge> toReturn = new DirectedSparseGraph<>();

        //get all the fishers
        final List<Fisher> fishers = state.getFishers();
        final int populationSize = fishers.size();
        if(populationSize <= 1)
            Preconditions.checkArgument(
                    false, "Cannot create social network with no fishers to connect");

        Log.trace("random before populating " + state.getRandom().nextDouble());
        for(Fisher fisher : fishers)
        {
            int degree = computeDegree(state.getRandom());
            if( populationSize <= degree) {

                degree = populationSize-1;
                Log.warn("The social network had to reduce the desired degree level to " + degree + " because the population size is too small");

            }
            List<Fisher> friends = new LinkedList<>();

            List<Fisher> candidates = new LinkedList<>();
            for(Fisher candidate : fishers)
            {
                boolean allowed = candidate!=fisher;
                boolean mutualAllowed = allowMutualFriendships || (toReturn.findEdge(candidate,fisher) == null);
                for (NetworkPredicate predicate : predicates)
                    allowed = allowed && predicate.test(fisher, candidate);
                if(allowed  && mutualAllowed)
                    candidates.add(candidate);
            }


            Collections.sort(candidates, Comparator.comparingInt(Fisher::getID));

            while(friends.size() < degree && friends.size() < candidates.size())
            {
                int randomConnection = state.getRandom().nextInt(candidates.size());
                final Fisher candidate = candidates.get(randomConnection);
                assert (candidate != fisher);

                if(!friends.contains(candidate))
                    friends.add(candidate);


            }

            if(friends.size()<degree && Log.DEBUG)
            {
                assert friends.size()==candidates.size();
                Log.debug(fisher + " couldn't have " + degree + "friends because the total number of valid candidates" +
                                  " were " + candidates.size() +
                                  ", and the total number of friends the fisher actually has is " + friends.size());
            }



            //now make them your friends!

            if(friends.size() > 0)
                for(Fisher friend : friends)
                    toReturn.addEdge(new FriendshipEdge(),fisher,friend, EdgeType.DIRECTED);
            else //if you have no friends add yourself as an unconnected person
                toReturn.addVertex(fisher);

        }

        return toReturn;

    }


    private int computeDegree(MersenneTwisterFast random){
        return degree.apply(random).intValue();
    }

    public DoubleParameter getDegree() {
        return degree;
    }

    public void setDegree(DoubleParameter degree) {
        this.degree = degree;
    }

    /**
     * this is supposed to be called not so much when initializing the network but later on if any agent is created
     * while the model is running
     *
     * @param fisher
     * @param currentNetwork
     * @param state
     */
    @Override
    public void addFisher(
            Fisher fisher, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {
        Preconditions.checkArgument(!currentNetwork.containsVertex(fisher));

        currentNetwork.addVertex(fisher);
        ObservableList<Fisher> fishers = state.getFishers();
        int populationSize = fishers.size();

        int degree = computeDegree(state.getRandom());
        Set<Fisher> friends = new HashSet<>(degree);
        while(friends.size() < Math.min(degree,populationSize))
        {
            final Fisher candidate = fishers.get(state.getRandom().nextInt(populationSize));
            if(candidate != fisher)
            {
                boolean allowed = true;
                for(NetworkPredicate predicate : predicates)
                    allowed = allowed && predicate.test(fisher,candidate);
                if(allowed)
                    friends.add(candidate);
            }
        }
        //now make them your friends!
        for(Fisher friend : friends)
            currentNetwork.addEdge(new FriendshipEdge(),fisher,friend, EdgeType.DIRECTED);

    }

    /**
     * remove fisher from network. This is to be used while the model is running to clear any ties
     *
     * @param toRemove       fisher to remove
     * @param currentNetwork network to modify
     * @param state
     */
    @Override
    public void removeFisher(
            Fisher toRemove, DirectedGraph<Fisher, FriendshipEdge> currentNetwork, FishState state) {
        currentNetwork.removeVertex(toRemove);
    }

    /**
     * adds a condition that needs to be true for two fishers to be friends.
     * @param predicate the condition to add
     */
    public void addPredicate(NetworkPredicate predicate){
        predicates.add(predicate);
    }

    /**
     * Getter for property 'allowMutualFriendships'.
     *
     * @return Value for property 'allowMutualFriendships'.
     */
    public boolean isAllowMutualFriendships() {
        return allowMutualFriendships;
    }

    /**
     * Setter for property 'allowMutualFriendships'.
     *
     * @param allowMutualFriendships Value to set for property 'allowMutualFriendships'.
     */
    public void setAllowMutualFriendships(boolean allowMutualFriendships) {
        this.allowMutualFriendships = allowMutualFriendships;
    }
}
