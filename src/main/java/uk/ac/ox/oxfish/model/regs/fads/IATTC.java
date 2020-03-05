package uk.ac.ox.oxfish.model.regs.fads;

import javax.measure.Quantity;
import javax.measure.quantity.Volume;

import static tech.units.indriya.unit.Units.CUBIC_METRE;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class IATTC {

    /**
     * I couldn't find the canonical source for this, but it's stated in a few places, notably
     * https://www.iattc.org/Meetings/Meetings2009/AIDCP-21/Docs/_English/MOP-21-07_Vessel%20capacity%20class%20definitions%20related%20to%20the%20requirement%20for%20carrying%20an%20on%20board%20observer.pdf
     * It seems that around 2010, IATTC changed from weight based capacity classes to volume based
     * ones because the latter is more objective.
     */
    public static int capacityClass(Quantity<Volume> holdVolume) {
        final long v = volumeInCubicMetres(holdVolume);
        if (v < 54) return 1;
        else if (v <= 107) return 2;
        else if (v <= 212) return 3;
        else if (v <= 318) return 4;
        else if (v <= 425) return 5;
        else return 6;
    }

    private static long volumeInCubicMetres(Quantity<Volume> holdVolume) {
        // Hold volumes should normally be integers, but we round just in case
        return Math.round(asDouble(holdVolume, CUBIC_METRE));
    }

    /**
     * Not used for now, but it might be what we need in the end
     * (See: https://github.com/poseidon-fisheries/tuna/issues/117)
     */
    public static int capacityClassByMaximumLoad(double maximumLoadInKg) {
        final int t = (int) (maximumLoadInKg / 1000);
        if (t < 46) return 1;
        else if (t <= 91) return 2;
        else if (t <= 181) return 3;
        else if (t <= 272) return 4;
        else if (t <= 363) return 5;
        else return 6;
    }

}
