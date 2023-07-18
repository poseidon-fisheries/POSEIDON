package uk.ac.ox.oxfish.biology.complicated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;

public class HockeyStickRecruitmentTest {


    @Test
    public void nohinge() {


        //SSB0 --> 500; hinge at 20%; R0 is 1000
        HockeyStickRecruitment recruitment = new HockeyStickRecruitment(
            true,
            .20,
            1000,
            10,
            500
        );

        //all fish weighs 1, but only bin 1 and 2 are mature
        Species species = new Species(
            "lame",
            new GrowthBinByList(1, new double[]{5, 20, 100}, new double[]{1, 1, 1})
        );

        //250 SSB now
        StructuredAbundance abundance = new StructuredAbundance(new double[]{10000, 100, 150});

        //full recruitment!
        Assertions.assertEquals(
            recruitment.computeYearlyRecruitment(species, species.getMeristics(), abundance),
            1000,
            .0001
        );


    }

    @Test
    public void hinge() {


        //SSB0 --> 500; hinge at 20%; R0 is 1000
        HockeyStickRecruitment recruitment = new HockeyStickRecruitment(
            true,
            .20,
            1000,
            10,
            500
        );

        //all fish weighs 1, but only bin 1 and 2 are mature
        Species species = new Species(
            "lame",
            new GrowthBinByList(1, new double[]{5, 20, 100}, new double[]{1, 1, 1})
        );

        //50 SSB now
        StructuredAbundance abundance = new StructuredAbundance(new double[]{10000, 25, 25});

        //half recruitment! (10% depletion)
        Assertions.assertEquals(
            recruitment.computeYearlyRecruitment(species, species.getMeristics(), abundance),
            500,
            .0001
        );


    }
}