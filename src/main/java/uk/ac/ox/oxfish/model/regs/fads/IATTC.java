package uk.ac.ox.oxfish.model.regs.fads;

import uk.ac.ox.oxfish.fisher.equipment.Hold;

public class IATTC {

    private static int volumeInCubicMetres(Hold hold) {
        return hold.getVolumeInCubicMetres().orElseThrow(() -> new IllegalArgumentException(
            hold + " doesn't have the volume information needed to establish its IATTC class."
        ));
    }

    public static int capacityClass(Hold hold) { return capacityClass(volumeInCubicMetres(hold)); }

    /**
     * I couldn't find the canonical source for this, but it's stated in a few places, notably
     * https://www.iattc.org/Meetings/Meetings2009/AIDCP-21/Docs/_English/MOP-21-07_Vessel%20capacity%20class%20definitions%20related%20to%20the%20requirement%20for%20carrying%20an%20on%20board%20observer.pdf
     * It seems that around 2010, IATTC changed from weight based capacity classes to volume based
     * ones because the latter is more objective.
     */
    public static int capacityClass(int holdVolumeInCubicMetres) {
        final int v = holdVolumeInCubicMetres;
        if (v < 54) return 1;
        else if (v < 108) return 2;
        else if (v < 213) return 3;
        else if (v < 319) return 4;
        else if (v <= 425) return 2;
        else return 6;
    }

    /**
     * Return the number of FADs that can be active at the same time for purse seine vessels
     * according to IATTC resolution C-17-02.8.
     * This is currently hard coded, but we'll most likely want to make this changeable.
     */
    public static int activeFadsLimit(Hold hold) {
        final int c = capacityClass(hold);
        if (c <= 3) return 70;
        else if (c <= 5) return 120;
        else if (volumeInCubicMetres(hold) < 1200) return 300;
        else return 450;
    }

}
