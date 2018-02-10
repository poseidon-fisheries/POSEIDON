package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.PortBasedWaitTimesDecorator;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;

public class PortBasedWaitTimesFactory implements AlgorithmFactory<PortBasedWaitTimesDecorator>
{

    private AlgorithmFactory<? extends Regulation> delegate = new AnarchyFactory();


    private HashMap<String,Integer> portWaitTimes = new HashMap<>();
    {
        portWaitTimes.put("Port 1",0);
        portWaitTimes.put("Port 2",10);
    }

    @Override
    public PortBasedWaitTimesDecorator apply(FishState fishState) {
        return new PortBasedWaitTimesDecorator(
                delegate.apply(fishState),
                portWaitTimes
        );
    }


    public AlgorithmFactory<? extends Regulation> getDelegate() {
        return delegate;
    }

    public void setDelegate(AlgorithmFactory<? extends Regulation> delegate) {
        this.delegate = delegate;
    }

    public HashMap<String, Integer> getPortWaitTimes() {
        return portWaitTimes;
    }

    public void setPortWaitTimes(HashMap<String, Integer> portWaitTimes) {
        this.portWaitTimes = portWaitTimes;
    }
}
