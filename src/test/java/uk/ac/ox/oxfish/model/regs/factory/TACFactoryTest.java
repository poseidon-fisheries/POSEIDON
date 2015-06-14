package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class TACFactoryTest {


    @Test
    public void testTAC() throws Exception {

        TACMonoFactory factory = new TACMonoFactory();
        FishState state = new FishState(System.currentTimeMillis());

        factory.setQuota(new FixedDoubleParameter(200));
        final MonoQuotaRegulation tac1 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(),200,.0001);
        //you can create another one, no problem
        final MonoQuotaRegulation tac2 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(),200,.0001);
        assertEquals(tac2.getYearlyQuota(),200,.0001);
        assertEquals(tac2.getQuotaRemaining(),200,.0001);

        //consume a bit of the second, it will affect the first
        tac2.reactToSale(mock(Specie.class),100,1234);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(),100,.0001);
        assertEquals(tac2.getYearlyQuota(),200,.0001);
        assertEquals(tac2.getQuotaRemaining(),100,.0001);

        //if I create a third tac, it won't replenish quota remaining
        final MonoQuotaRegulation tac3 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),200,.0001);
        assertEquals(tac1.getQuotaRemaining(),100,.0001);
        assertEquals(tac2.getYearlyQuota(),200,.0001);
        assertEquals(tac2.getQuotaRemaining(),100,.0001);
        assertEquals(tac3.getYearlyQuota(),200,.0001);
        assertEquals(tac3.getQuotaRemaining(),100,.0001);

        //if I increase the yearly quota, it will affect everyone but not the remaining quota because it has been consumed already
        factory.setQuota(new FixedDoubleParameter(300));
        final MonoQuotaRegulation tac4 = factory.apply(state);
        assertEquals(tac1.getYearlyQuota(),300,.0001);
        assertEquals(tac1.getQuotaRemaining(),100,.0001);
        assertEquals(tac2.getYearlyQuota(),300,.0001);
        assertEquals(tac2.getQuotaRemaining(),100,.0001);
        assertEquals(tac3.getYearlyQuota(),300,.0001);
        assertEquals(tac3.getQuotaRemaining(),100,.0001);

        //in fact they are all the same object
        assertEquals(tac1,tac2);
        assertEquals(tac1,tac3);
        assertEquals(tac1,tac4);



    }
}