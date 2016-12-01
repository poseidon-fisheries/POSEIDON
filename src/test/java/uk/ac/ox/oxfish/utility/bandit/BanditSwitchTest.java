package uk.ac.ox.oxfish.utility.bandit;

import org.junit.Test;

import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 12/1/16.
 */
public class BanditSwitchTest {


    @Test
    public void simpleSwitch() throws Exception {

        BanditSwitch banditSwitch = new BanditSwitch(5,
                                                     integer -> integer % 2 == 0);

        assertEquals(banditSwitch.getGroup(0),0);
        assertEquals(banditSwitch.getGroup(1),2);
        assertEquals(banditSwitch.getGroup(2),4);
        assertEquals((int)banditSwitch.getArm(0),0);
        assertEquals((int)banditSwitch.getArm(2),1);
        assertEquals((int)banditSwitch.getArm(4),2);

        assertNull(banditSwitch.getArm(1));

    }
}