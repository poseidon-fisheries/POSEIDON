package uk.ac.ox.oxfish.model.network;

import com.google.common.base.Preconditions;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builds network where everyone has the same out-degree of edges
 * Created by carrknight on 7/1/15.
 */
public class EquidegreeBuilder implements AlgorithmFactory<DirectedGraph<Fisher,FriendshipEdge>>{

    private int degree = 2;


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
        Preconditions.checkArgument(
                populationSize > degree, " Cannot achieve desired degree of " + degree + " with only " + populationSize + " fishers");
        for(Fisher fisher : fishers)
        {
            Set<Fisher> friends = new HashSet<>(degree);
            while(friends.size() < degree)
            {
                final Fisher candidate = fishers.get(state.getRandom().nextInt(populationSize));
                if(candidate != fisher)
                    friends.add(candidate);
            }
            //now make them your friends!
            for(Fisher friend : friends)
                toReturn.addEdge(new FriendshipEdge(),fisher,friend, EdgeType.DIRECTED);


        }

        return toReturn;

    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }
}
