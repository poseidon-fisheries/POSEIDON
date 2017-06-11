package uk.ac.ox.oxfish.model.regs.factory;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class MultiQuotaMapFactoryTest {


    private GlobalBiology biology;
    private FishState state;
    private MultiQuotaMapFactory factory;

    @Before
    public void setUp() throws Exception {
        Log.set(Log.LEVEL_INFO);
        factory = new MultiQuotaMapFactory();
        factory.getInitialQuotas().put("First",1000d);
        factory.getInitialQuotas().put("Third",10d);


        state = mock(FishState.class,RETURNS_DEEP_STUBS);
        biology = new GlobalBiology(new Species("First"),new Species("Second"),new Species("third"));
        when(state.getBiology()).thenReturn(biology);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        when(state.getSpecies()).thenReturn(biology.getSpecies());
        when(state.getNumberOfFishers()).thenReturn(100);

    }

    @Test
    public void multiITQ() throws Exception
    {

        FishYAML yaml = new FishYAML();
        Log.info("This test tries to read \n" + yaml.dump(factory) + "\n as an ITQ quota");


        factory.getQuotaExchangedPerMatch().put("First",5d);
        factory.getQuotaExchangedPerMatch().put("Third",10d);



        factory.setItq(true);

        MultiQuotaRegulation apply = factory.apply(state);
        Log.info("the test read the following string: " + factory.getConvertedInitialQuotas());

        assertEquals(apply.getQuotaRemaining(0),1000d,.001);
        assertEquals(apply.getQuotaRemaining(2),10d,.001);
        //i am going to force the itq scaler to start
        MultiQuotaMapFactory.ITQScaler scaler = new MultiQuotaMapFactory.ITQScaler(apply);
        scaler.start(state);
        assertEquals(apply.getQuotaRemaining(0),10,.001);
        assertEquals(apply.getQuotaRemaining(2),0.1,.001);
        assertEquals(apply.getYearlyQuota()[0],10,.001);
        assertEquals(apply.getYearlyQuota()[2],0.1,.001);

    }

    @Test
    public void multiTAC() throws Exception
    {


        FishYAML yaml = new FishYAML();
        Log.info("This test tries to read \n" + yaml.dump(factory) + "\n as a TAC quota");

        factory.setItq(false);

        MultiQuotaRegulation apply = factory.apply(state);
        Log.info("the test read the following string: " + factory.getConvertedInitialQuotas());
        assertEquals(1000d,apply.getYearlyQuota()[0],.0001);
        assertEquals(10d,apply.getYearlyQuota()[2],.0001);
        assertTrue(Double.isInfinite(apply.getYearlyQuota()[1]));
        assertEquals(1000d,apply.getQuotaRemaining(0),.0001);
        assertEquals(10d,apply.getQuotaRemaining(2),.0001);
        assertTrue(Double.isInfinite(apply.getYearlyQuota()[1]));


    }
}