package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.*;

/**
 * A location fishermen come and go from
 * Created by carrknight on 4/2/15.
 */
public class Port {

    /**
     * all the fishers anchored here
     */
    private final HashSet<Fisher> fishersHere;

    /**
     * the location of the port
     */
    private final SeaTile location;


    public Port(SeaTile location)
    {
        this.location = location;
        fishersHere = new HashSet<>();
    }

    /**
     * Tell the port the fisher is docking here. To be correct they must be on the same tile and the fisher must not
     * be already here
     * @param fisher the fisher docking to port
     */
    public void dock(Fisher fisher)
    {

        Preconditions.checkArgument(fisher.getLocation().equals(location),
                "A fisher can't dock a port if they aren't on the same tile");

        boolean wasHere = !fishersHere.add(fisher);
        if(wasHere)
            throw new IllegalStateException(fisher +" called dock() but is already here ");
        assert fishersHere.contains(fisher); //should be here now
    }

    /**
     * tell the port the fisher is departing
     * @param fisher the fisher departing
     */
    public void depart(Fisher fisher)
    {
        boolean wasHere =fishersHere.remove(fisher);
        if(!wasHere)
            throw new IllegalStateException(fisher +" called depart() but wasn't in the list of docked fishers ");
        assert !fishersHere.contains(fisher); //shouldn't be here anymore

    }


    /**
     * returns an immutable view of the fishers listed here
     * @return the set of fishers here
     */
    public Set<Fisher> getFishersHere() {
        return Collections.unmodifiableSet(fishersHere);
    }


    public boolean isDocked(Fisher fisher)
    {
        return fishersHere.contains(fisher);
    }
}
