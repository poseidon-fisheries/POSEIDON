package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

public class WeibullDistributionTest {

    //We have extreme parameters for the Bigeye carrying capacity distribution
    //This is simple code to just check to see what the effect is on the carrying capacity.
    @Test
    public void testWeibull() {
        final WeibullDoubleParameter weibullDoubleParameter = new WeibullDoubleParameter(.0001, 4286);
        final MersenneTwisterFast mersenneTwisterFast = new MersenneTwisterFast();
        int nInf = 0;
        int nZero = 0;
        int nFinite = 0;
        for (int i = 0; i < 100000; i++) {
            final double rNum = weibullDoubleParameter.applyAsDouble(mersenneTwisterFast);
            if (rNum < 1) nZero++;
            if (rNum > 10000000) nInf++;
            if (rNum >= 1 && rNum <= 10000000) {
                nFinite++;
                System.out.println(rNum);
            }
        }
        System.out.println(nInf);
        System.out.println(nZero);
        System.out.println(nFinite);


    }
}
