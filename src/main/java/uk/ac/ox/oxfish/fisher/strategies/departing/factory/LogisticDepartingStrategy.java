package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * The base on which logistic strategies are instantiated. It works daily, so for a full day it will always say either
 * true or false according to a logistic probability
 * Created by carrknight on 9/11/15.
 */
public abstract class LogisticDepartingStrategy  implements DepartingStrategy, Steppable
{


    private final double l;

    private final double k;

    private final double x0;


    private Boolean  decisionTaken = null;

    private FishState model;


    public LogisticDepartingStrategy(double l, double k, double x0) {
        this.l = l;
        this.k = k;
        this.x0 = x0;
    }

    public double getL() {
        return l;
    }

    public double getK() {
        return k;
    }

    public double getX0() {
        return x0;
    }


    /**
     * logistic
     *
     *
     * @param fisher
     * @param model
     * @param random
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {

        //you have no countdown activated
        if (decisionTaken == null) {


            double x = computeX(fisher, model);
            x = Math.max(x, 0);

            double dailyProbability = FishStateUtilities.logisticProbability(l, k, x0, x);
            assert dailyProbability >= 0;
            assert dailyProbability <= 1;


            decisionTaken = model.getRandom().nextBoolean(dailyProbability);



            return decisionTaken;

        }
        else
            return decisionTaken;

    }

    /**
     * abstract method, returns whatever we need to plug in the logistic function
     */
    abstract public double computeX(Fisher fisher, FishState model);



    @Override
    public void start(FishState model, Fisher fisher) {

        this.model = model;
        model.scheduleEveryDay(this, StepOrder.DAWN);
    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    @Override
    public void step(SimState simState) {
        decisionTaken = null;
    }
}

