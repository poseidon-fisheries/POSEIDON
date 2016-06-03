package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.*;

/**
 * Checks if any of your friends have fished in a location somewhere. If so avoid it!
 * Created by carrknight on 5/31/16.
 */
public class NoFriendsHereExtractor
        implements SocialAcceptabilityFeatureExtractor<SeaTile> {


    /**
     * directed friends are fishers who are your friends. If false then also people who befriended you
     * are considered
     */
    private boolean directedFriendsOnly = true;


    public NoFriendsHereExtractor(boolean directedFriendsOnly) {
        this.directedFriendsOnly = directedFriendsOnly;
    }

    public NoFriendsHereExtractor()
    {


    }


    /**
     * Method called to extract the feature from the object toRepresent, given the observer and the overall model
     *  @param toRepresent the list of object from which to extract a feature
     * @param model       the model to represent
     * @param fisher
     */
    @Override
    public Map<SeaTile, Double> extractFeature(
            Collection<SeaTile> toRepresent, FishState model, Fisher fisher) {

        Collection<Fisher> friends =
                directedFriendsOnly ?
                fisher.getDirectedFriends() :
                fisher.getAllFriends();

        //go through all your friends and collect the tiles they've fished into
        Set<SeaTile> friendsTiles =new HashSet<>();
        for(Fisher friend : friends)
        {
            TripRecord lastTrip = friend.getLastFinishedTrip();
            if(lastTrip != null)
            friendsTiles.addAll(lastTrip.getTilesFished());
        }

        HashMap<SeaTile,Double> toReturn =
                new HashMap<>(toRepresent.size());
        for(SeaTile tile : toRepresent)
        {

            if(friendsTiles.contains(tile))
                toReturn.put(tile, -1d); //unsafe
            else
                toReturn.put(tile, 1d); //safe

        }
        return toReturn;
    }


    /**
     * Getter for property 'directedFriendsOnly'.
     *
     * @return Value for property 'directedFriendsOnly'.
     */
    public boolean isDirectedFriendsOnly() {
        return directedFriendsOnly;
    }

    /**
     * Setter for property 'directedFriendsOnly'.
     *
     * @param directedFriendsOnly Value to set for property 'directedFriendsOnly'.
     */
    public void setDirectedFriendsOnly(boolean directedFriendsOnly) {
        this.directedFriendsOnly = directedFriendsOnly;
    }
}
