package uk.ac.ox.oxfish.model.scenario;

import org.jfree.util.Log;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class PolicyScriptTest {


    private FishState state;

    @Before
    public void setUp() throws Exception {
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(5);
        scenario.setRegulation(new AnarchyFactory());
        FixedProportionGearFactory gears = new FixedProportionGearFactory();
        gears.setCatchabilityPerHour(new FixedDoubleParameter(1d));
        scenario.setGear(gears);
        state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);

        assertEquals(5, state.getFishers().size());
        for(Fisher fisher : state.getFishers()) {
            assertEquals(1, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(),fisher.getRegulation());

        }

    }

    @Test
    public void noChanges() throws Exception {
        Log.info("all null means no changes");



        PolicyScript script  = new PolicyScript();
        script.apply(state);

        assertEquals(5, state.getFishers().size());
        for(Fisher fisher : state.getFishers()) {
            assertEquals(1, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(),fisher.getRegulation());
        }
    }


    @Test
    public void ruleChanges() throws Exception {

        Log.info("Change of regulations propagates correctly");

        PolicyScript script  = new PolicyScript();
        TACMonoFactory regulation = new TACMonoFactory();
        regulation.setQuota(new FixedDoubleParameter(5000d));
        script.setRegulation(regulation);
        script.apply(state);

        assertEquals(5,state.getFishers().size());
        for(Fisher fisher : state.getFishers()) {
            assertEquals(1, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertTrue(fisher.getRegulation() instanceof MonoQuotaRegulation);
            assertEquals(5000d, ((MonoQuotaRegulation) fisher.getRegulation()).getYearlyQuota(),.0001);
        }
    }

    @Test
    public void gearChanges() throws Exception {
        Log.info("Change of gears propagates correctly");


        PolicyScript script  = new PolicyScript();

        FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(0d));
        script.setGear(gear);
        script.apply(state);

        assertEquals(5,state.getFishers().size());
        for(Fisher fisher : state.getFishers()) {
            assertEquals(0, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(),fisher.getRegulation());

        }
    }

    @Test
    public void addFishers() throws Exception {

        Log.info("If I add fishers and change gear the new fishers will also have the new gear");

        PolicyScript script  = new PolicyScript();

        FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(0d));
        script.setGear(gear);
        script.setChangeInNumberOfFishers(10);
        script.apply(state);

        assertEquals(15,state.getFishers().size());
        for(Fisher fisher : state.getFishers()) {
            assertEquals(0, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(),fisher.getRegulation());

        }
    }

    @Test
    public void  yaml()
    {

        FishYAML yaml = new FishYAML();
        PolicyScript script  = new PolicyScript();

        FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(0d));
        script.setGear(gear);
        script.apply(state);
        String representedPolicy = yaml.dump(script);
        System.out.println(representedPolicy);
        PolicyScript read = yaml.loadAs(representedPolicy,PolicyScript.class);
        assertTrue(read.getRegulation() == null);
        assertTrue(read.getGear() instanceof FixedProportionGearFactory);
        assertTrue(read.getChangeInNumberOfFishers() == null);


        HashMap<Integer,PolicyScript> scripts = new HashMap<>();
        scripts.put(10,script);

        PolicyScript script2  = new PolicyScript();

        script2.setRegulation(new AnarchyFactory());
        script2.setChangeInNumberOfFishers(100);
        scripts.put(15,script2);

        String dumpedScript = yaml.dump(scripts);
        System.out.println(dumpedScript);
        //need to read it in two steps, initially it's just a linkedhashmap all the way down
        LinkedHashMap<Integer,LinkedHashMap> temp = (LinkedHashMap<Integer, LinkedHashMap>) yaml.load(dumpedScript);
        HashMap<Integer,PolicyScript> readBack = new HashMap<>();
        for(Map.Entry<Integer,LinkedHashMap> entry : temp.entrySet())
        //turn value into string and read it back forcing it as a policy script
            readBack.put(entry.getKey(),yaml.loadAs(yaml.dump(entry.getValue()),PolicyScript.class));

        assertEquals(2,readBack.size());
        assertEquals(100,(int)readBack.get(15).getChangeInNumberOfFishers());
        assertTrue(readBack.get(10).getGear() instanceof FixedProportionGearFactory);

    }
}