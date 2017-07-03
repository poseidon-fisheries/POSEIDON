package uk.ac.ox.oxfish.fisher.selfanalysis;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * Takes hourly profit objective function but adds potentially two thresholds above and below
 * Created by carrknight on 1/28/17.
 */
public class CutoffPerTripObjective implements ObjectiveFunction<Fisher>{


    private final HourlyProfitInTripObjective delegate;


    private final double lowThreshold;

    private final double highThreshold;

    public CutoffPerTripObjective(HourlyProfitInTripObjective delegate,
                                  double lowThreshold,
                                  double highThreshold)
    {
        if(Double.isFinite(lowThreshold) && Double.isFinite(highThreshold))
            Preconditions.checkArgument(lowThreshold<=highThreshold);
        this.delegate = delegate;
        this.lowThreshold = lowThreshold;
        this.highThreshold = highThreshold;
    }

    /**
     * compute current fitness of the agent
     *
     *
     * @param observer
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observer, Fisher observed) {
        return censor(delegate.computeCurrentFitness(observer, observed));
    }

    private double censor(double profit) {
        if(Double.isFinite(lowThreshold) && profit<=lowThreshold)
            return lowThreshold;
        if(Double.isFinite(highThreshold) && profit>=highThreshold)
            return highThreshold;
        return profit;
    }

}
