package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.model.FishState;

import java.util.Scanner;

/**
 * memorizes last sensor output and returns it without updating for
 * a specified number of years
 */
public class IntermittentSensorDecorator<T>  implements Sensor<FishState,T>  {

    private T lastScan;

    private int lastYearCalled;

    private final Sensor<FishState,T> delegate;

    private final int minInterval;

    public IntermittentSensorDecorator(
            Sensor<FishState, T> delegate,
            int minInterval) {
        this.delegate = delegate;
        this.minInterval = minInterval;
    }

    @Override
    public T scan(FishState system) {

        if(lastScan == null ||
                system.getYear()-lastYearCalled>=minInterval){
            lastScan = delegate.scan(system);
            lastYearCalled = system.getYear();
        }
        return lastScan;


    }
}
