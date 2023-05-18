package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

public class RecruitmentFixedMultiplierByJackKnifeMaturityTest {

    @Test
    public void ssbToRecruitsWorkCorrectly() {


        Map<Integer, DoubleParameter> rps = new HashMap<>();
        rps.put(10, new FixedDoubleParameter(2));
        rps.put(30, new FixedDoubleParameter(4));

        RecruitmentFixedMultiplierByJackKnifeMaturity recruitment =
            new RecruitmentFixedMultiplierByJackKnifeMaturity(
                15, rps, new MersenneTwisterFast()
            );

        Species species = new Species(
            "Test",
            new FromListMeristics(
                new double[]{1, 2, 3},
                new double[]{10d, 20d, 30d},
                1
            )
        );

        //no recruitment until day 10!
        for (int day = 0; day < 10; day++) {
            final double recruit = recruitment.recruit(
                species,
                species.getMeristics(),
                new StructuredAbundance(new double[]{100, 100, 100}),
                day,
                365
            );
            Assert.assertEquals(0, recruit, .001d);
        }
        //on day 10 there should a be a recruitment pulse;
        // we are talking about 100 + 100 mature fish, 200kg+300kg ssb
        // rps 2
        Assert.assertEquals(1000,
            recruitment.recruit(
                species,
                species.getMeristics(),
                new StructuredAbundance(new double[]{100, 100, 100}),
                10,
                365
            ), .001d
        );


    }
}