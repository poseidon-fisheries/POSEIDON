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

package uk.ac.ox.oxfish.model.regs.factory;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;


@SuppressWarnings("unchecked")
public class MultiQuotaMapFactoryTest {


    private GlobalBiology biology;
    private FishState state;
    private MultiQuotaMapFactory factory;

    @BeforeEach
    public void setUp() throws Exception {
        Logger.getGlobal().setLevel(Level.INFO);
        factory = new MultiQuotaMapFactory();
        factory.getInitialQuotas().put("First", 1000d);
        factory.getInitialQuotas().put("Third", 10d);


        state = mock(FishState.class, RETURNS_DEEP_STUBS);
        biology = new GlobalBiology(new Species("First"), new Species("Second"), new Species("third"));
        when(state.getBiology()).thenReturn(biology);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());
        when(state.getSpecies()).thenReturn(biology.getSpecies());
        when(state.getNumberOfFishers()).thenReturn(100);

    }

    @Test
    public void multiITQ() throws Exception {

        final FishYAML yaml = new FishYAML();
        Logger.getGlobal().info("This test tries to read \n" + yaml.dump(factory) + "\n as an ITQ quota");


        factory.getQuotaExchangedPerMatch().put("First", 5d);
        factory.getQuotaExchangedPerMatch().put("Third", 10d);


        factory.setQuotaType(MultiQuotaMapFactory.QuotaType.ITQ);

        verify(state, never()).registerStartable(any(ITQScaler.class));
        final MultiQuotaRegulation apply = factory.apply(state);
        Logger.getGlobal().info("the test read the following string: " + factory.getConvertedInitialQuotas());

        assertEquals(apply.getQuotaRemaining(0), 1000d, .001);
        assertEquals(apply.getQuotaRemaining(2), 10d, .001);
        //i am going to force the itq scaler to start
        final ITQScaler scaler = new ITQScaler(apply);
        scaler.start(state);
        assertEquals(apply.getQuotaRemaining(0), 10, .001);
        assertEquals(apply.getQuotaRemaining(2), 0.1, .001);
        assertEquals(apply.getYearlyQuota()[0], 10, .001);
        assertEquals(apply.getYearlyQuota()[2], 0.1, .001);

    }


    @Test
    public void multiIQ() throws Exception {

        final FishYAML yaml = new FishYAML();
        Logger.getGlobal().info("This test tries to read \n" + yaml.dump(factory) + "\n as an IQ quota");


        factory.setQuotaType(MultiQuotaMapFactory.QuotaType.IQ);

        final MultiQuotaRegulation apply = factory.apply(state);
        Logger.getGlobal().info("the test read the following string: " + factory.getConvertedInitialQuotas());
        verify(state).registerStartable(any(ITQScaler.class));

        assertEquals(apply.getQuotaRemaining(0), 1000d, .001);
        assertEquals(apply.getQuotaRemaining(2), 10d, .001);
        //i am going to force the itq scaler to start
        final ITQScaler scaler = new ITQScaler(apply);
        scaler.start(state);
        assertEquals(apply.getQuotaRemaining(0), 10, .001);
        assertEquals(apply.getQuotaRemaining(2), 0.1, .001);
        assertEquals(apply.getYearlyQuota()[0], 10, .001);
        assertEquals(apply.getYearlyQuota()[2], 0.1, .001);

    }

    @Test
    public void multiTAC() throws Exception {


        final FishYAML yaml = new FishYAML();
        Logger.getGlobal().info("This test tries to read \n" + yaml.dump(factory) + "\n as a TAC quota");

        factory.setQuotaType(MultiQuotaMapFactory.QuotaType.TAC);

        final MultiQuotaRegulation apply = factory.apply(state);
        verify(state, never()).registerStartable(any(ITQScaler.class));

        Logger.getGlobal().info("the test read the following string: " + factory.getConvertedInitialQuotas());
        assertEquals(1000d, apply.getYearlyQuota()[0], .0001);
        assertEquals(10d, apply.getYearlyQuota()[2], .0001);
        assertTrue(Double.isInfinite(apply.getYearlyQuota()[1]));
        assertEquals(1000d, apply.getQuotaRemaining(0), .0001);
        assertEquals(10d, apply.getQuotaRemaining(2), .0001);
        assertTrue(Double.isInfinite(apply.getYearlyQuota()[1]));


    }

    @Test
    public void scalesCorrectlyIQ() {
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(2);
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        final SimpleMapInitializerFactory map = new SimpleMapInitializerFactory();
        map.setHeight(new FixedDoubleParameter(4));
        map.setWidth(new FixedDoubleParameter(4));
        map.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(map);

        final FishState model = new FishState();
        model.setScenario(scenario);
        final MultiQuotaMapFactory factory = new MultiQuotaMapFactory(
            MultiQuotaMapFactory.QuotaType.IQ,
            entry("Species 0", 100.0)
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
        ), 50.0, FishStateUtilities.EPSILON);


    }

    @Test
    public void scalesCorrectlyITQ() throws Exception {
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(2);
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        final SimpleMapInitializerFactory map = new SimpleMapInitializerFactory();
        map.setHeight(new FixedDoubleParameter(4));
        map.setWidth(new FixedDoubleParameter(4));
        map.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(map);

        final FishState model = new FishState();
        model.setScenario(scenario);
        final MultiQuotaMapFactory factory = new MultiQuotaMapFactory(
            MultiQuotaMapFactory.QuotaType.ITQ,
            entry("Species 0", 100.0)
        );
        final HashMap<String, Double> quotaExchangedPerMatch = new HashMap<>();
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
        ), 50.0, FishStateUtilities.EPSILON);


    }

}