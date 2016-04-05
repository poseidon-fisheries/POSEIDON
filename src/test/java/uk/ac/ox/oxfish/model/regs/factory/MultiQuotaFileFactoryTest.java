package uk.ac.ox.oxfish.model.regs.factory;

import org.jfree.util.Log;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MultiQuotaFileFactoryTest {


    private GlobalBiology biology;
    private FishState state;
    private MultiQuotaFileFactory factory;

    @Before
    public void setUp() throws Exception {
        String path = Paths.get("inputs","tests","quotas_test.csv").toString();
        biology = new GlobalBiology(new Species("Test 1"),
                                    new Species("Test 2"),
                                    new Species("Test 3"));

        state = mock(FishState.class);
        when(state.getNumberOfFishers()).thenReturn(100);
        when(state.getSpecies()).thenReturn(biology.getSpecies());
        factory = new MultiQuotaFileFactory();
        factory.setPathToFile(path);
    }

    @Test
    public void multiITQ() throws Exception
    {

        Log.info("This test tries to read " + factory.getPathToFile() + " as an ITQ quota");





        factory.setItq(true);

        MultiQuotaRegulation apply = factory.apply(state);
        Log.info("the test read the following string: " + factory.getRepresenter().toString());

        assertEquals(apply.getQuotaRemaining(0),100,.001);
        assertEquals(apply.getQuotaRemaining(2),150,.001);
        //i am going to force the itq scaler to start
        MultiQuotaFileFactory.ITQScaler scaler = new MultiQuotaFileFactory.ITQScaler(apply);
        scaler.start(state);
        assertEquals(apply.getQuotaRemaining(0),1,.001);
        assertEquals(apply.getQuotaRemaining(2),1.5,.001);
        assertEquals(apply.getYearlyQuota()[0],1,.001);
        assertEquals(apply.getYearlyQuota()[2],1.5,.001);

    }

    @Test
    public void multiTAC() throws Exception
    {



        Log.info("This test tries to read " + factory.getPathToFile() + " as an TAC quota");

        factory.setItq(false);

        MultiQuotaRegulation apply = factory.apply(state);
        Log.info("the test read the following string: " + factory.getRepresenter().toString());
        assertEquals(apply.getQuotaRemaining(0),100,.001);
        assertEquals(apply.getQuotaRemaining(2),150,.001);



    }
}