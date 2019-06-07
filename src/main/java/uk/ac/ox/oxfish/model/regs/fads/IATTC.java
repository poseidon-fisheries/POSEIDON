package uk.ac.ox.oxfish.model.regs.fads;

import javax.measure.quantity.Volume;

import static org.apache.sis.measure.Units.CUBIC_METRE;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class IATTC {

    /**
     * Return the number of FADs that can be active at the same time for purse seine vessels
     * according to IATTC resolution C-17-02.8. This is currently hard coded, but we'll most likely
     * want to make this changeable.
     */
    public static int activeFadsLimit(Volume holdVolume) {
        switch (capacityClass(holdVolume)) {
            case 1:
            case 2:
            case 3:
                return 70;
            case 4:
            case 5:
                return 120;
            default:
                return volumeInCubicMetres(holdVolume) < 1200 ? 300 : 450;
        }
    }

    /**
     * I couldn't find the canonical source for this, but it's stated in a few places, notably
     * https://www.iattc.org/Meetings/Meetings2009/AIDCP-21/Docs/_English/MOP-21-07_Vessel%20capacity%20class%20definitions%20related%20to%20the%20requirement%20for%20carrying%20an%20on%20board%20observer.pdf
     * It seems that around 2010, IATTC changed from weight based capacity classes to volume based
     * ones because the latter is more objective.
     */
    public static int capacityClass(Volume holdVolume) {
        final long v = volumeInCubicMetres(holdVolume);
        if (v < 54) return 1;
        else if (v < 108) return 2;
        else if (v < 213) return 3;
        else if (v < 319) return 4;
        else if (v <= 425) return 5;
        else return 6;
    }

    private static long volumeInCubicMetres(Volume holdVolume) {
        // Hold volumes should normally be integers, but we round just in case
        return Math.round(asDouble(holdVolume, CUBIC_METRE));
    }

}
