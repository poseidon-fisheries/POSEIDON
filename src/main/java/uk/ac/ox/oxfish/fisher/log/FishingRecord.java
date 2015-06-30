package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Gear;
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
    final private double hoursSpentFishing;

    /**
     * the gear used
     */
    final private Gear gearUsed;


    /**
     * where did fishing occur
     */
    final private SeaTile tileFished;

    /**
     * the catch
     */
    final private Catch fishCaught;

    /**
     * the time-step when the fishing occurred
     */
    final private int step;


    public FishingRecord(
            double hoursSpentFishing, Gear gearUsed, SeaTile tileFished, Catch fishCaught, int step) {
        this.hoursSpentFishing = hoursSpentFishing;
        this.gearUsed = gearUsed;
        this.tileFished = tileFished;
        this.fishCaught = fishCaught;
        this.step = step;
    }


    public double getHoursSpentFishing() {
        return hoursSpentFishing;
    }

    public Gear getGearUsed() {
        return gearUsed;
    }

    public SeaTile getTileFished() {
        return tileFished;
    }

    public Catch getFishCaught() {
        return fishCaught;
    }

    public int getStep() {
        return step;
    }
}
