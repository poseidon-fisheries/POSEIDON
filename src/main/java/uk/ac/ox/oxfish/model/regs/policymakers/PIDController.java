package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;

/**
 * The PID controller, the magical item that warms our coffee and taunts my terrible knowledge of control theory.
 * Basically it's an equation that does stuff without me having to REALLY understand how it works.
 * It has something to do with Lagrange transforms, I imagine.
 * Created by carrknight on 10/11/16.
 */
public class PIDController extends Controller {


    /**
     * proportional gain
     */
    private double p;

    /**
     * integral gain
     */
    private double i;

    /**
     * derivative gain
     */
    private double d;

    /**
     * policy value when error is 0 (basically our starting point)
     */
    private double offset;

    private double previousError = 0;


    private double sumOfErrors = 0;


    public PIDController(
            Sensor<FishState, Double> observed,
            Sensor<FishState, Double> target,
            Actuator<FishState, Double> actuator,
            IntervalPolicy interval,
            double p, double i, double d,
            double offset) {
        super(observed, target, actuator, interval);
        this.p = p;
        this.i = i;
        this.d = d;
        this.offset = offset;
    }

    @Override
    public double computePolicy(double currentVariable,
                                double target,
                                FishState model,
                                double oldPolicy)
    {

        //pid magic here
        double error = target - currentVariable;
        sumOfErrors += error;
        double derivative =  error - previousError;
        previousError = error;

        double policy = p * error + i * sumOfErrors + d * derivative;

        return  offset + policy;
    }

    /**
     * Getter for property 'p'.
     *
     * @return Value for property 'p'.
     */
    public double getP() {
        return p;
    }

    /**
     * Setter for property 'p'.
     *
     * @param p Value to set for property 'p'.
     */
    public void setP(double p) {
        this.p = p;
    }

    /**
     * Getter for property 'i'.
     *
     * @return Value for property 'i'.
     */
    public double getI() {
        return i;
    }

    /**
     * Setter for property 'i'.
     *
     * @param i Value to set for property 'i'.
     */
    public void setI(double i) {
        this.i = i;
    }

    /**
     * Getter for property 'd'.
     *
     * @return Value for property 'd'.
     */
    public double getD() {
        return d;
    }

    /**
     * Setter for property 'd'.
     *
     * @param d Value to set for property 'd'.
     */
    public void setD(double d) {
        this.d = d;
    }

    /**
     * Getter for property 'offset'.
     *
     * @return Value for property 'offset'.
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Setter for property 'offset'.
     *
     * @param offset Value to set for property 'offset'.
     */
    public void setOffset(double offset) {
        this.offset = offset;
    }
}
