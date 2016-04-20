package uk.ac.ox.oxfish.model.market.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MACongestedMarket;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Create Congested Market with Moving Average congestion
 * Created by carrknight on 1/6/16.
 */
public class MACongestedMarketFactory implements AlgorithmFactory<MACongestedMarket>
{


    /**
     * demand intercept
     */
    private DoubleParameter demandIntercept = new FixedDoubleParameter(10d);

    /**
     * demand slope
     */
    private DoubleParameter demandSlope = new FixedDoubleParameter(0.001);

    /**
     * moving average size
     */
    private DoubleParameter observationWindow = new FixedDoubleParameter(30);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MACongestedMarket apply(FishState fishState) {
        return  new MACongestedMarket(
                demandIntercept.apply(fishState.getRandom()),
                demandSlope.apply(fishState.getRandom()),
                observationWindow.apply(fishState.getRandom()).intValue()
        );
    }

    public DoubleParameter getDemandIntercept() {
        return demandIntercept;
    }

    public void setDemandIntercept(DoubleParameter demandIntercept) {
        this.demandIntercept = demandIntercept;
    }

    public DoubleParameter getDemandSlope() {
        return demandSlope;
    }

    public void setDemandSlope(DoubleParameter demandSlope) {
        this.demandSlope = demandSlope;
    }

    public DoubleParameter getObservationWindow() {
        return observationWindow;
    }

    public void setObservationWindow(DoubleParameter observationWindow) {
        this.observationWindow = observationWindow;
    }
}
