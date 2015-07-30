package uk.ac.ox.oxfish.model.network;

import com.google.common.base.Preconditions;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.*;

/**
 * Builds a network using Barabasi-Albert algorithm from Jung
 * Created by carrknight on 7/1/15.
 */
public class BarabasiAlbertBuilder implements AlgorithmFactory<DirectedGraph<Fisher,FriendshipEdge>>
{


    private boolean parallelFriendshipsAllowed = true;

    private int edgesPerVertex = 2;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DirectedGraph<Fisher, FriendshipEdge> apply(FishState state) {

        final List<Fisher> fishers = state.getFishers();
        final Iterator<Fisher> fisherIterator = fishers.iterator();

        Preconditions.checkArgument(edgesPerVertex < fishers.size());


        BarabasiAlbertGenerator<Fisher,FriendshipEdge> generator =
                new BarabasiAlbertGenerator<>(DirectedSparseGraph::new,
                                              fisherIterator::next,
                                              FriendshipEdge::new,
                                              edgesPerVertex,
                                              edgesPerVertex,
                                              state.getRandom().nextInt(),
                                              new HashSet<>());


        final DirectedGraph<Fisher, FriendshipEdge> toReturn = (DirectedGraph<Fisher, FriendshipEdge>) generator.create();
        generator.evolveGraph(fishers.size()-edgesPerVertex);

        assert toReturn.getVertices().size() == fishers.size();


        return toReturn;

    }

    public boolean isParallelFriendshipsAllowed() {
        return parallelFriendshipsAllowed;
    }

    public void setParallelFriendshipsAllowed(boolean parallelFriendshipsAllowed) {
        this.parallelFriendshipsAllowed = parallelFriendshipsAllowed;
    }

    public int getEdgesPerVertex() {
        return edgesPerVertex;
    }

    public void setEdgesPerVertex(int edgesPerVertex) {
        this.edgesPerVertex = edgesPerVertex;
    }
}
