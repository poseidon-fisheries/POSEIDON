package uk.ac.ox.oxfish.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Just a collector of all the utilities function i need
 * Created by carrknight on 6/19/15.
 */
public class FishStateUtilities {

    public static final double EPSILON = .01;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


}
