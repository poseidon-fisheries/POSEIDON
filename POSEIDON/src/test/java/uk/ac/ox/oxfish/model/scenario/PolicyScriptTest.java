/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.scenario;

import org.jfree.util.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.Assert.*;


@SuppressWarnings("rawtypes")
public class PolicyScriptTest {


    private FishState state;

    @BeforeEach
    public void setUp() throws Exception {
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(5);
        scenario.setRegulation(new AnarchyFactory());
        final FixedProportionGearFactory gears = new FixedProportionGearFactory();
        gears.setCatchabilityPerHour(new FixedDoubleParameter(1d));
        scenario.setGear(gears);
        state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);

        assertEquals(5, state.getFishers().size());
        for (final Fisher fisher : state.getFishers()) {
            assertEquals(1, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(), fisher.getRegulation());

        }

    }

    @Test
    public void noChanges() throws Exception {
        Log.info("all null means no changes");


        final PolicyScript script = new PolicyScript();
        script.apply(state);

        assertEquals(5, state.getFishers().size());
        for (final Fisher fisher : state.getFishers()) {
            assertEquals(1, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(), fisher.getRegulation());
        }
    }


    @Test
    public void ruleChanges() throws Exception {

        Log.info("Change of regulations propagates correctly");

        final PolicyScript script = new PolicyScript();
        final TACMonoFactory regulation = new TACMonoFactory();
        regulation.setQuota(new FixedDoubleParameter(5000d));
        script.setRegulation(regulation);
        script.apply(state);

        assertEquals(5, state.getFishers().size());
        for (final Fisher fisher : state.getFishers()) {
            assertEquals(1, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertTrue(fisher.getRegulation() instanceof MonoQuotaRegulation);
            assertEquals(5000d, ((MonoQuotaRegulation) fisher.getRegulation()).getYearlyQuota(), .0001);
        }
    }

    @Test
    public void gearChanges() throws Exception {
        Log.info("Change of gears propagates correctly");


        final PolicyScript script = new PolicyScript();

        final FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(0d));
        script.setGear(gear);
        script.apply(state);

        assertEquals(5, state.getFishers().size());
        for (final Fisher fisher : state.getFishers()) {
            assertEquals(0, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(), fisher.getRegulation());

        }
    }

    @Test
    public void addFishers() throws Exception {

        Log.info("If I add fishers and change gear the new fishers will also have the new gear");

        final PolicyScript script = new PolicyScript();

        final FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(0d));
        script.setGear(gear);
        script.setChangeInNumberOfFishers(10);
        script.apply(state);

        assertEquals(15, state.getFishers().size());
        for (final Fisher fisher : state.getFishers()) {
            assertEquals(0, ((FixedProportionGear) fisher.getGear()).getProportionFished(), .0001);
            assertEquals(AnarchyFactory.getSingleton(), fisher.getRegulation());

        }
    }

    @Test
    public void yaml() {

        final FishYAML yaml = new FishYAML();
        final PolicyScript script = new PolicyScript();

        final FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(0d));
        script.setGear(gear);
        script.apply(state);
        final String representedPolicy = yaml.dump(script);
        System.out.println(representedPolicy);
        final PolicyScript read = yaml.loadAs(representedPolicy, PolicyScript.class);
        assertNull(read.getRegulation());
        assertTrue(read.getGear() instanceof FixedProportionGearFactory);
        assertNull(read.getChangeInNumberOfFishers());


        final HashMap<Integer, PolicyScript> scripts = new HashMap<>();
        scripts.put(10, script);

        final PolicyScript script2 = new PolicyScript();

        script2.setRegulation(new AnarchyFactory());
        script2.setChangeInNumberOfFishers(100);
        scripts.put(15, script2);

        final String dumpedScript = yaml.dump(scripts);
        System.out.println(dumpedScript);
        //need to read it in two steps, initially it's just a linkedhashmap all the way down
        final LinkedHashMap<Integer, LinkedHashMap> temp = yaml.load(dumpedScript);
        final HashMap<Integer, PolicyScript> readBack = new HashMap<>();
        for (final Map.Entry<Integer, LinkedHashMap> entry : temp.entrySet())
            //turn value into string and read it back forcing it as a policy script
            readBack.put(entry.getKey(), yaml.loadAs(yaml.dump(entry.getValue()), PolicyScript.class));

        assertEquals(2, readBack.size());
        assertEquals(100, (int) readBack.get(15).getChangeInNumberOfFishers());
        assertTrue(readBack.get(10).getGear() instanceof FixedProportionGearFactory);

    }
}