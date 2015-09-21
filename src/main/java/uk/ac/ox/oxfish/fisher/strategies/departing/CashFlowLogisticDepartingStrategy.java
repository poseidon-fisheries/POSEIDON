package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.LogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A simple departing strategy that checks how much cash was made in a given period and compares it against a given target.
 * The lower
 * Created by carrknight on 9/11/15.
 */
public class CashFlowLogisticDepartingStrategy extends LogisticDepartingStrategy {



    private final double cashflowTarget;

    private final int cashflowPeriod;


    /**
     *  @param l the L parameter of the logistic curve
     * @param k the k parameter of the logistic curve
     * @param x0 the x0 parameter of the logistic curve
     * @param cashflowTarget the cashflow  level that satisfies the fisher
     * @param cashflowPeriod
     */
    public CashFlowLogisticDepartingStrategy(
            double l, double k, double x0,
            double cashflowTarget, int cashflowPeriod) {
        super(l,k,x0);
        this.cashflowTarget = cashflowTarget;
        this.cashflowPeriod =cashflowPeriod;
    }





    public double getCashflowTarget() {
        return cashflowTarget;
    }

    public int getCashflowPeriod() {
        return cashflowPeriod;
    }


    /**
     * abstract method, returns whatever we need to plug in the logistic function
     *
     * @param equipment the equipment
     * @param status the status
     * @param memory the memories of the fisher
     * @param model a link to the model
     */
    @Override
    public double computeX(
            FisherEquipment equipment, FisherStatus status, FisherMemory memory, FishState model) {
        double cash = status.getBankBalance();
        double previousCash = memory.numberOfDailyObservations() > cashflowPeriod ?
                memory.balanceXDaysAgo(cashflowPeriod) :
                0;

        double x = (cash-previousCash)/cashflowTarget;
        x = Math.max(x,0);
        return x;
    }
}
