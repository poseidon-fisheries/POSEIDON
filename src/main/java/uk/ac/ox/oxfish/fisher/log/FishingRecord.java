package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * A record describing the achieved catch for a specific instance of fishing
 * Created by carrknight on 6/30/15.
 */
public class FishingRecord
{


    /**
     * hours spent fishing.
     */
    final private int hoursSpentFishing;


    /**
     * where did fishing occur
     */
    final private SeaTile tileFished;

    /**
     * the catch
     */
    final private Catch fishCaught;




    public FishingRecord(
            int hoursSpentFishing, SeaTile tileFished, Catch fishCaught) {
        this.hoursSpentFishing = hoursSpentFishing;
        this.tileFished = tileFished;
        this.fishCaught = fishCaught;
    }

    public int getHoursSpentFishing() {
        return hoursSpentFishing;
    }


    public SeaTile getTileFished() {
        return tileFished;
    }

    public Catch getFishCaught() {
        return fishCaught;
    }


}
