package uk.ac.ox.oxfish.fisher.selfanalysis;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SimulatedProfitWithCPUEObjectiveFunctionTest {

    @Test
    public void simulatedProfit() {

        Fisher observer = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher observed = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Gear gear = new FixedProportionGear(.1);
        when(observer.getGear()).thenReturn(gear);
        when(observed.getGear()).thenReturn(gear);
        when(observer.getLastFinishedTrip()).thenReturn(null);
        when(observed.getLastFinishedTrip()).thenReturn(null);


        ObjectiveFunction<Fisher> function = new SimulatedProfitWithCPUEObjectiveFunction(10);
        //no trip, it should be NaN
        assertTrue(Double.isNaN(function.computeCurrentFitness(observer,observed)));
        assertTrue(Double.isNaN(function.computeCurrentFitness(observer,observer)));



    }

    @Test
    public void sameGear() {

        Fisher observer = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher observed = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Gear gear = new FixedProportionGear(.1);
        when(observer.getGear()).thenReturn(gear);
        when(observed.getGear()).thenReturn(gear);
        TripRecord fakeRecord = mock(TripRecord.class);
        when(observed.getLastFinishedTrip()).thenReturn(fakeRecord);


        when(fakeRecord.getTotalCPUE()).thenReturn(new double[]{.1,.1});
        when(fakeRecord.getMostFishedTileInTrip()).thenReturn(mock(SeaTile.class));

        ProfitFunction simulation = mock(ProfitFunction.class);
        // the simulator is fake, will just add all the CPUEs
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return Arrays.stream((double[])
                        invocation.getArguments()[3]).sum();
            }
        })
                .when(simulation).hourlyProfitFromHypotheticalTripHere(any(),any(),any(),any(),anyBoolean());

        SimulatedProfitWithCPUEObjectiveFunction function = new SimulatedProfitWithCPUEObjectiveFunction(
                10,
                simulation
                );
        //the trip "profits" will be just .1 + .1
        assertEquals(.2,(function).computeCurrentFitness(observer,observed),.001);

    }

    @Test
    public void differentGear() {

        Fisher observer = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Fisher observed = mock(Fisher.class,RETURNS_DEEP_STUBS);
        Gear gear1 = mock(Gear.class); when(gear1.expectedHourlyCatch(any(),any(),anyInt(),any())).thenReturn(new double[]{.2,.2});
        Gear gear2 = mock(Gear.class);when(gear2.expectedHourlyCatch(any(),any(),anyInt(),any())).thenReturn(new double[]{.1,.1});
        when(observer.getGear()).thenReturn(gear1);
        when(observed.getGear()).thenReturn(gear2);
        TripRecord fakeRecord = mock(TripRecord.class);
        when(observed.getLastFinishedTrip()).thenReturn(fakeRecord);


        when(fakeRecord.getTotalCPUE()).thenReturn(new double[]{.1,.1});
        when(fakeRecord.getMostFishedTileInTrip()).thenReturn(mock(SeaTile.class));

        ProfitFunction simulation = mock(ProfitFunction.class);
        // the simulator is fake, will just add all the CPUEs
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return Arrays.stream((double[])
                        invocation.getArguments()[3]).sum();
            }
        })
                .when(simulation).hourlyProfitFromHypotheticalTripHere(any(),any(),any(),any(),anyBoolean());

        SimulatedProfitWithCPUEObjectiveFunction function = new SimulatedProfitWithCPUEObjectiveFunction(
                10,
                simulation
        );
        //the trip "profits" will be just .2 + .2 (basically it's doubled by the fact that the fisher1 is more efficient)
        assertEquals(.4,(function).computeCurrentFitness(observer,observed),.001);

    }

}