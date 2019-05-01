package uk.ac.ox.oxfish.model.regs.fads;

import uk.ac.ox.oxfish.fisher.equipment.Hold;

import static org.apache.sis.measure.Units.CUBIC_METRE;
import static uk.ac.ox.oxfish.model.regs.fads.IATTC.CapacityClass.*;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class IATTC {

    private static long volumeInCubicMetres(Hold hold) {
        return hold.getVolume()
            // Hold volumes should normally be integers, but we round just in case
            .map(v -> Math.round(asDouble(v, CUBIC_METRE)))
            .orElseThrow(() -> new IllegalArgumentException(
                hold + " doesn't have the volume information needed to establish its IATTC class."
            ));
    }

    /**
     * I couldn't find the canonical source for this, but it's stated in a few places, notably
     * https://www.iattc.org/Meetings/Meetings2009/AIDCP-21/Docs/_English/MOP-21-07_Vessel%20capacity%20class%20definitions%20related%20to%20the%20requirement%20for%20carrying%20an%20on%20board%20observer.pdf
     * It seems that around 2010, IATTC changed from weight based capacity classes to volume based
     * ones because the latter is more objective.
     */
    public static CapacityClass capacityClass(Hold hold) {
        final long v = volumeInCubicMetres(hold);
        if (v < 54) return CLASS_1;
        else if (v < 108) return CLASS_2;
        else if (v < 213) return CLASS_3;
        else if (v < 319) return CLASS_4;
        else if (v <= 425) return CLASS_5;
        else return CLASS_6;
    }

    /**
     * Return the number of FADs that can be active at the same time for purse seine vessels
     * according to IATTC resolution C-17-02.8. This is currently hard coded, but we'll most likely
     * want to make this changeable.
     */
    public static int activeFadsLimit(Hold hold) {
        switch (capacityClass(hold)) {
            case CLASS_1:
            case CLASS_2:
            case CLASS_3:
                return 70;
            case CLASS_4:
            case CLASS_5:
                return 120;
            default:
                return volumeInCubicMetres(hold) < 1200 ? 300 : 450;
        }
    }

    public enum CapacityClass {CLASS_1, CLASS_2, CLASS_3, CLASS_4, CLASS_5, CLASS_6}

}
