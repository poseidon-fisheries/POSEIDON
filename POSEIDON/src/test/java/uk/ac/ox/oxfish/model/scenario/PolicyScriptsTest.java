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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 5/3/16.
 */
public class PolicyScriptsTest {


    @Test
    public void callsCorrectly() throws Exception {

        final HashMap<Integer, PolicyScript> map = new HashMap<>();

        final PolicyScript tenYear = mock(PolicyScript.class);
        map.put(10, tenYear);
        final PolicyScript twentyYear = mock(PolicyScript.class);
        map.put(20, twentyYear);

        final PolicyScripts scripts = new PolicyScripts(map);

        final FishState state = mock(FishState.class);
        when(state.scheduleEveryYear(any(), any())).thenReturn(mock(Stoppable.class));

        //check that starts schedules it
        scripts.start(state);
        verify(state).scheduleEveryYear(scripts, StepOrder.DAWN);

        when(state.getYear()).thenReturn(1);
        scripts.step(state);
        verify(tenYear, times(0)).apply(state);
        verify(twentyYear, times(0)).apply(state);

        //notice that the step happens when the year is -1 the specified one
        //since this step technically occurs the last step of the old year rather than the first step of the old one
        when(state.getYear()).thenReturn(9);
        scripts.step(state);
        verify(tenYear, times(1)).apply(state);
        verify(twentyYear, times(0)).apply(state);


        when(state.getYear()).thenReturn(13);
        scripts.step(state);
        verify(tenYear, times(1)).apply(state);
        verify(twentyYear, times(0)).apply(state);

        when(state.getYear()).thenReturn(19);
        scripts.step(state);
        verify(tenYear, times(1)).apply(state);
        verify(twentyYear, times(1)).apply(state);

        //if time goes back (why?) then it steps again
        when(state.getYear()).thenReturn(9);
        scripts.step(state);
        verify(tenYear, times(2)).apply(state);
        verify(twentyYear, times(1)).apply(state);


    }


    @Test
    public void fromYamlCorrectly() {

        final String yaml = "scripts:\n" +
            "  1:\n" +
            "    PolicyScript:\n" +
            "      changeInNumberOfFishers: 100\n" +
            "      departingStrategy: null\n" +
            "      destinationStrategy: null\n" +
            "      fishingStrategy: null\n" +
            "      gear:\n" +
            "        Fixed Proportion:\n" +
            "          catchabilityPerHour: '0.06'\n" +
            "      regulation: null\n" +
            "      weatherStrategy: null\n" +
            "  10:\n" +
            "    PolicyScript:\n" +
            "      departingStrategy:\n" +
            "        Fixed Rest:\n" +
            "          hoursBetweenEachDeparture: '12.0'\n" +
            "      regulation:\n" +
            "        Mono-ITQ:\n" +
            "          individualQuota: '5000.0'\n";
        final FishYAML yamler = new FishYAML();
        final PolicyScripts scripts = yamler.loadAs(yaml, PolicyScripts.class);
        Assertions.assertEquals(scripts.getScripts().size(), 2);
        final PolicyScript firstYearPolicyScript = scripts.getScripts().get(1);
        final PolicyScript secondYearPolicyScript = scripts.getScripts().get(10);
        Assertions.assertNull(firstYearPolicyScript.getFishingStrategy());
        Assertions.assertNull(secondYearPolicyScript.getFishingStrategy());
        Assertions.assertTrue(firstYearPolicyScript.getGear() instanceof FixedProportionGearFactory);
        Assertions.assertEquals(((FixedDoubleParameter) ((FixedProportionGearFactory) firstYearPolicyScript.getGear()).getCatchabilityPerHour()).getValue(),
            .06,
            .0001);


    }

    @Test
    public void fromYamlCorrectlyOneScript() {

        final String yaml = "PolicyScript:\n" +
            "  changeInNumberOfFishers: 100\n" +
            "  departingStrategy: null\n" +
            "  destinationStrategy: null\n" +
            "  fishingStrategy: null\n" +
            "  gear:\n" +
            "    Fixed Proportion:\n" +
            "      catchabilityPerHour: '0.06'\n" +
            "  regulation: null\n" +
            "  weatherStrategy: null";
        final FishYAML yamler = new FishYAML();
        final PolicyScript script = yamler.loadAs(yaml, PolicyScript.class);
        Assertions.assertNull(script.getFishingStrategy());
        Assertions.assertTrue(script.getGear() instanceof FixedProportionGearFactory);
        Assertions.assertEquals(((FixedDoubleParameter) ((FixedProportionGearFactory) script.getGear()).getCatchabilityPerHour()).getValue(),
            .06,
            .0001);


    }
}