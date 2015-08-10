package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.data.YearlyFisherDataSet;

/**
 * The more money is gained in a fixed number of days, the better!
 * Created by carrknight on 8/4/15.
 */
public class CashFlowObjective implements  ObjectiveFunction<Fisher>
{


    public static final String CASH_COLUMN = YearlyFisherDataSet.CASH_COLUMN;
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
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observed) {
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

    /**
     * compute the fitness of the agent "in the previous step"; How far back that is
     * depends on the objective function itself
     *
     * @param observed the agent whose fitness we want
     * @return a fitness value: the higher the better
     */
    @Override
    public double computePreviousFitness(Fisher observed) {
        //get cash available period days ago
        double currentCash= getCashInPast(observed,period);
        //get cash in the past ( 2*period days away)
        double laggedCash = getCashInPast(observed, 2*period);

        return currentCash-laggedCash;

    }
}

