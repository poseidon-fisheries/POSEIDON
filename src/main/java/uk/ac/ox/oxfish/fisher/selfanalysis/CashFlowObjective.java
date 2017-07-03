package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * The more money is gained in a fixed number of days, the better!
 * Created by carrknight on 8/4/15.
 */
public class CashFlowObjective implements  ObjectiveFunction<Fisher>
{


    /**
     * how many days pass between one check and the other? Basically cashflow:
     * Cash(today) - Cash(today-period)
     */
    private final int period;

    public CashFlowObjective(int period) {
        this.period = period;
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
        //get cash available today
        double currentCash= observed.getBankBalance();
        //get cash in the past (if not present, assumes it started at 0)
        double laggedCash = getCashInPast(observed,period);

        return currentCash-laggedCash;
    }

    private double getCashInPast(Fisher observed, int daysAgo) {


        return observed.getDailyData().numberOfObservations() > daysAgo ?
                observed.balanceXDaysAgo(daysAgo) : 0;
    }


}

