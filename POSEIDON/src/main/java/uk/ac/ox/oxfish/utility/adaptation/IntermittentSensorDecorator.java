package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.model.FishState;

/**
 * memorizes last sensor output and returns it without updating for
 * a specified number of years
 */
public class IntermittentSensorDecorator<T> implements Sensor<FishState, T> {

    private static final long serialVersionUID = 4215633694359433508L;
    private final Sensor<FishState, T> delegate;
    private final int minInterval;
    private T lastScan;
    private int lastYearCalled;

    public IntermittentSensorDecorator(
        final Sensor<FishState, T> delegate,
        final int minInterval
    ) {
        this.delegate = delegate;
        this.minInterval = minInterval;
    }

    @Override
    public T scan(final FishState system) {

        if (lastScan == null ||
            system.getYear() - lastYearCalled >= minInterval) {
            lastScan = delegate.scan(system);
            lastYearCalled = system.getYear();
        }
        return lastScan;


    }
}
