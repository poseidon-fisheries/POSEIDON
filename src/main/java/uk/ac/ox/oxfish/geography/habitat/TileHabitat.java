package uk.ac.ox.oxfish.geography.habitat;

/**
 * The habitat information associated with a tile
 * Created by carrknight on 9/28/15.
 */
public class TileHabitat
{


    /**
     * in % how much of the sea tile has a hard substrate
     */
    private final double hardPercentage;



    public TileHabitat(double hardPercentage) {
        this.hardPercentage = hardPercentage;
    }

    public double getHardPercentage() {
        return hardPercentage;
    }
}
