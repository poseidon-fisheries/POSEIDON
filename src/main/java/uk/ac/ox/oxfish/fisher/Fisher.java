package uk.ac.ox.oxfish.fisher;

import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * Still unclear if it represents a boat or a fleet but in general the person whose task it is to go out and fish the
 * fish.
 * Created by carrknight on 4/2/15.
 */
public class Fisher {

    /**
     * the location of the port
     */
    private SeaTile location;

    DepartingStrategy departingStrategy;


    public SeaTile getLocation()
    {
        return location;
    }
}
