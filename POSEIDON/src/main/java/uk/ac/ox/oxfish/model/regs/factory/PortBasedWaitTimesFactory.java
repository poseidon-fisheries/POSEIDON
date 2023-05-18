package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.PortBasedWaitTimesDecorator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;
import java.util.Map;

public class PortBasedWaitTimesFactory implements AlgorithmFactory<PortBasedWaitTimesDecorator> {

    private AlgorithmFactory<? extends Regulation> delegate = new AnarchyFactory();


    private HashMap<String, Object> portWaitTimes = new HashMap<>();

    {
        portWaitTimes.put("Port 0", "0");
        //       portWaitTimes.put("Port 2","10");
    }

    @Override
    public PortBasedWaitTimesDecorator apply(FishState fishState) {


        HashMap<String, Integer> portWaitTimesInteger = new HashMap<>();
        for (Map.Entry<String, Object> waitTime : portWaitTimes.entrySet()) {
            Object value = waitTime.getValue();
            portWaitTimesInteger.put(
                waitTime.getKey(),
                value instanceof String ?
                    Integer.parseInt((String) value) :
                    (Integer) (value)

            );
        }

        return new PortBasedWaitTimesDecorator(
            delegate.apply(fishState),
            portWaitTimesInteger
        );
    }


    public AlgorithmFactory<? extends Regulation> getDelegate() {
        return delegate;
    }

    public void setDelegate(AlgorithmFactory<? extends Regulation> delegate) {
        this.delegate = delegate;
    }


    /**
     * Getter for property 'portWaitTimes'.
     *
     * @return Value for property 'portWaitTimes'.
     */
    public HashMap<String, Object> getPortWaitTimes() {
        return portWaitTimes;
    }

    /**
     * Setter for property 'portWaitTimes'.
     *
     * @param portWaitTimes Value to set for property 'portWaitTimes'.
     */
    public void setPortWaitTimes(HashMap<String, Object> portWaitTimes) {
        this.portWaitTimes = portWaitTimes;
    }
}
