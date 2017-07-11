package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 7/5/17.
 */
public class LogisticRecruitmentProcessTest {




    private final static Meristics meristics = new FromListMeristics(.99,new Double[]{1d,1d,1d},
                                                                     1d, 2d, 3d);


    @Test
    public void logisticGrowth() throws Exception {

        LogisticRecruitmentProcess process = new LogisticRecruitmentProcess(
                400d,.5
        );
        int recruit = process.recruit(mock(Species.class), meristics,
                                      new int[]{0, 0, 0}, new int[]{12, 4, 60});

        //recruits ought to be weighin a total of 50kg, so that there ought to be 50 of them
        Assert.assertEquals(recruit,50);




    }

    @Test
    public void noRecruitsAboveCarryingCapacity() throws Exception {

        LogisticRecruitmentProcess process = new LogisticRecruitmentProcess(
                201,100
        );
        int recruit = process.recruit(mock(Species.class), meristics,
                                      new int[]{0, 0, 0}, new int[]{12, 4, 60});

        //recruits ought to be weighin a total of 1kg, so that there ought to be 1 of them
        Assert.assertEquals(recruit,1);




    }


    @Test
    public void noGrowthFromDepletion() throws Exception {

        LogisticRecruitmentProcess process = new LogisticRecruitmentProcess(
                400d,.5
        );
        int recruit = process.recruit(mock(Species.class), meristics,
                                      new int[]{0, 0, 0}, new int[]{0,0, 0});

        Assert.assertEquals(recruit,0);


    }
}