package uk.ac.ox.oxfish.model.regs.factory;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import javafx.collections.ObservableList;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.HashMap;

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



        factory.setQuotaType(MultiQuotaMapFactory.QuotaType.ITQ);

        verify(state,never()).registerStartable(any(ITQScaler.class));
        MultiQuotaRegulation apply = factory.apply(state);
        Log.info("the test read the following string: " + factory.getConvertedInitialQuotas());

        assertEquals(apply.getQuotaRemaining(0),1000d,.001);
        assertEquals(apply.getQuotaRemaining(2),10d,.001);
        //i am going to force the itq scaler to start
        ITQScaler scaler = new ITQScaler(apply);
        scaler.start(state);
        assertEquals(apply.getQuotaRemaining(0),10,.001);
        assertEquals(apply.getQuotaRemaining(2),0.1,.001);
        assertEquals(apply.getYearlyQuota()[0],10,.001);
        assertEquals(apply.getYearlyQuota()[2],0.1,.001);

    }


    @Test
    public void multiIQ() throws Exception
    {

        FishYAML yaml = new FishYAML();
        Log.info("This test tries to read \n" + yaml.dump(factory) + "\n as an IQ quota");


        factory.setQuotaType(MultiQuotaMapFactory.QuotaType.IQ);

        MultiQuotaRegulation apply = factory.apply(state);
        Log.info("the test read the following string: " + factory.getConvertedInitialQuotas());
        verify(state).registerStartable(any(ITQScaler.class));

        assertEquals(apply.getQuotaRemaining(0),1000d,.001);
        assertEquals(apply.getQuotaRemaining(2),10d,.001);
        //i am going to force the itq scaler to start
        ITQScaler scaler = new ITQScaler(apply);
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

        factory.setQuotaType(MultiQuotaMapFactory.QuotaType.TAC);

        MultiQuotaRegulation apply = factory.apply(state);
        verify(state,never()).registerStartable(any(ITQScaler.class));

        Log.info("the test read the following string: " + factory.getConvertedInitialQuotas());
        assertEquals(1000d,apply.getYearlyQuota()[0],.0001);
        assertEquals(10d,apply.getYearlyQuota()[2],.0001);
        assertTrue(Double.isInfinite(apply.getYearlyQuota()[1]));
        assertEquals(1000d,apply.getQuotaRemaining(0),.0001);
        assertEquals(10d,apply.getQuotaRemaining(2),.0001);
        assertTrue(Double.isInfinite(apply.getYearlyQuota()[1]));


    }

    @Test
    public void scalesCorrectlyIQ() throws Exception
    {
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(2);
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        SimpleMapInitializerFactory map = new SimpleMapInitializerFactory();
        map.setHeight(new FixedDoubleParameter(4));
        map.setWidth(new FixedDoubleParameter(4));
        map.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(map);

        FishState model = new FishState();
        model.setScenario(scenario);
        MultiQuotaMapFactory factory = new MultiQuotaMapFactory(
                MultiQuotaMapFactory.QuotaType.IQ,
                new Pair<>("Species 0",100.0)
        );
        scenario.setRegulation(factory);

        //should divide the quota in half!
        model.start();
        model.schedule.step(model);
        Fisher fisher = model.getFishers().get(0);
        assertEquals(fisher.getRegulation().maximumBiomassSellable(
                fisher,
                model.getSpecies().get(0),
                model
        ), 50.0, FishStateUtilities.EPSILON);
        fisher = model.getFishers().get(1);
        assertEquals(fisher.getRegulation().maximumBiomassSellable(
                fisher,
                model.getSpecies().get(0),
                model
        ),50.0,FishStateUtilities.EPSILON);


    }

    @Test
    public void scalesCorrectlyITQ() throws Exception
    {
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(2);
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        SimpleMapInitializerFactory map = new SimpleMapInitializerFactory();
        map.setHeight(new FixedDoubleParameter(4));
        map.setWidth(new FixedDoubleParameter(4));
        map.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(map);

        FishState model = new FishState();
        model.setScenario(scenario);
        MultiQuotaMapFactory factory = new MultiQuotaMapFactory(
                MultiQuotaMapFactory.QuotaType.ITQ,
                new Pair<>("Species 0",100.0)
        );
        HashMap<String, Double> quotaExchangedPerMatch = new HashMap<>();
        quotaExchangedPerMatch.put("Species 0", 200.0);
        factory.setQuotaExchangedPerMatch(quotaExchangedPerMatch);
        scenario.setRegulation(factory);

        //should divide the quota in half!
        model.start();
        model.schedule.step(model);
        Fisher fisher = model.getFishers().get(0);
        assertEquals(fisher.getRegulation().maximumBiomassSellable(
                fisher,
                model.getSpecies().get(0),
                model
        ), 50.0, FishStateUtilities.EPSILON);
        fisher = model.getFishers().get(1);
        assertEquals(fisher.getRegulation().maximumBiomassSellable(
                fisher,
                model.getSpecies().get(0),
                model
        ),50.0,FishStateUtilities.EPSILON);


    }

}