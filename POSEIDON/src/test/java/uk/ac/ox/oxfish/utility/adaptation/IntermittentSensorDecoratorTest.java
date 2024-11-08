package uk.ac.ox.oxfish.utility.adaptation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntermittentSensorDecoratorTest {


    @SuppressWarnings("unchecked")
    @Test
    public void intermittent() {

        final Sensor<FishState, Double> sensor = mock(Sensor.class);
        when(sensor.scan(any())).thenReturn(0d);

        final IntermittentSensorDecorator<Double> decorator = new IntermittentSensorDecorator<>(
            sensor,
            5
        );
        final FishState state = mock(FishState.class);
        when(state.getYear()).thenReturn(1);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);

        when(sensor.scan(any())).thenReturn(100d);
        //does not update
        Assertions.assertEquals(decorator.scan(state), 0d, .001);

        Assertions.assertEquals(decorator.scan(state), 0d, .001);
        when(state.getYear()).thenReturn(2);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);
        when(state.getYear()).thenReturn(4);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);
        when(state.getYear()).thenReturn(5);
        Assertions.assertEquals(decorator.scan(state), 0d, .001);


        //updates at year 6
        when(state.getYear()).thenReturn(6);
        Assertions.assertEquals(decorator.scan(state), 100d, .001);


    }
}