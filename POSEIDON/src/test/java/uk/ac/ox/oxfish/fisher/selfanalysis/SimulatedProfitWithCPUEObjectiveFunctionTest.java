/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class SimulatedProfitWithCPUEObjectiveFunctionTest {

    @Test
    public void simulatedProfit() {

        Fisher observer = mock(Fisher.class, RETURNS_DEEP_STUBS);
        Fisher observed = mock(Fisher.class, RETURNS_DEEP_STUBS);
        Gear gear = new FixedProportionGear(.1);
        when(observer.getGear()).thenReturn(gear);
        when(observed.getGear()).thenReturn(gear);
        when(observer.getLastFinishedTrip()).thenReturn(null);
        when(observed.getLastFinishedTrip()).thenReturn(null);


        ObjectiveFunction<Fisher> function = new SimulatedProfitWithCPUEObjectiveFunction(10);
        //no trip, it should be NaN
        Assertions.assertTrue(Double.isNaN(function.computeCurrentFitness(observer, observed)));
        Assertions.assertTrue(Double.isNaN(function.computeCurrentFitness(observer, observer)));


    }

    @Test
    public void sameGear() {

        Fisher observer = mock(Fisher.class, RETURNS_DEEP_STUBS);
        Fisher observed = mock(Fisher.class, RETURNS_DEEP_STUBS);
        Gear gear = new FixedProportionGear(.1);
        when(observer.getGear()).thenReturn(gear);
        when(observed.getGear()).thenReturn(gear);
        TripRecord fakeRecord = mock(TripRecord.class);
        when(observed.getLastFinishedTrip()).thenReturn(fakeRecord);


        when(fakeRecord.getTotalCPUE()).thenReturn(new double[]{.1, .1});
        when(fakeRecord.getMostFishedTileInTrip()).thenReturn(mock(SeaTile.class));

        ProfitFunction simulation = mock(ProfitFunction.class);
        // the simulator is fake, will just add all the CPUEs
        doAnswer(invocation -> Arrays.stream((double[])
            invocation.getArguments()[3]).sum())
            .when(simulation).hourlyProfitFromHypotheticalTripHere(any(), any(), any(), any(), anyBoolean());

        SimulatedProfitWithCPUEObjectiveFunction function = new SimulatedProfitWithCPUEObjectiveFunction(
            10,
            simulation
        );
        //the trip "profits" will be just .1 + .1
        Assertions.assertEquals(.2, (function).computeCurrentFitness(observer, observed), .001);

    }

    @Test
    public void differentGear() {

        Fisher observer = mock(Fisher.class, RETURNS_DEEP_STUBS);
        Fisher observed = mock(Fisher.class, RETURNS_DEEP_STUBS);
        Gear gear1 = mock(Gear.class);
        when(gear1.expectedHourlyCatch(any(), any(), anyInt(), any())).thenReturn(new double[]{.2, .2});
        Gear gear2 = mock(Gear.class);
        when(gear2.expectedHourlyCatch(any(), any(), anyInt(), any())).thenReturn(new double[]{.1, .1});
        when(observer.getGear()).thenReturn(gear1);
        when(observed.getGear()).thenReturn(gear2);
        TripRecord fakeRecord = mock(TripRecord.class);
        when(observed.getLastFinishedTrip()).thenReturn(fakeRecord);


        when(fakeRecord.getTotalCPUE()).thenReturn(new double[]{.1, .1});
        when(fakeRecord.getMostFishedTileInTrip()).thenReturn(mock(SeaTile.class));

        ProfitFunction simulation = mock(ProfitFunction.class);
        // the simulator is fake, will just add all the CPUEs
        doAnswer(invocation -> Arrays.stream((double[])
            invocation.getArguments()[3]).sum())
            .when(simulation).hourlyProfitFromHypotheticalTripHere(any(), any(), any(), any(), anyBoolean());

        SimulatedProfitWithCPUEObjectiveFunction function = new SimulatedProfitWithCPUEObjectiveFunction(
            10,
            simulation
        );
        //the trip "profits" will be just .2 + .2 (basically it's doubled by the fact that the fisher1 is more efficient)
        Assertions.assertEquals(.4, (function).computeCurrentFitness(observer, observed), .001);

    }

}
