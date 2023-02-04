/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
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

    private Sensor<FishState,Double> offsetSetter;


    private boolean zeroOverflowProtection = false;


    private double minimumPolicy = Double.NEGATIVE_INFINITY;

    public PIDController(
            Sensor<FishState, Double> observed,
            Sensor<FishState, Double> target,
            Actuator<FishState, Double> actuator,
            int interval,
            double p, double i, double d,
            double offset) {
        super(observed, target, actuator, interval);
        this.p = p;
        this.i = i;
        this.d = d;
        this.offset = offset;
    }


    @Override
    public void start(FishState model) {
        super.start(model);

        model.getYearlyDataSet().registerGatherer(

                "Policy from PID Controller",
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {
                        return getPolicy();
                    }
                },
                Double.NaN);
    }

    @Override
    public double computePolicy(double currentVariable,
                                double target,
                                FishState model,
                                double oldPolicy)
    {
        if(offsetSetter != null)
        {
            offset = offsetSetter.scan(model);
            offsetSetter = null;
        }

        //pid magic here
        double error = target - currentVariable;
        sumOfErrors += error;
        double derivative =  error - previousError;
        previousError = error;

        double pidPolicy = p * error + i * sumOfErrors + d * derivative;


        //do not accumulate error past zero?
        final double policyToOutput = offset + pidPolicy;
        if(zeroOverflowProtection && i!=0){
            if(policyToOutput < 0)
            {
                sumOfErrors =  (- offset - d * derivative - p * error)/i;
            }
        }

        System.out.println("PID Target: " + target);
        System.out.println("PID Error: " + error);
        System.out.println("PID MV: " + policyToOutput);


        return Math.max(policyToOutput, minimumPolicy);
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

    public Sensor<FishState, Double> getOffsetSetter() {
        return offsetSetter;
    }

    public void setOffsetSetter(Sensor<FishState, Double> offsetSetter) {
        this.offsetSetter = offsetSetter;
    }

    public double getPreviousError() {
        return previousError;
    }

    public double getSumOfErrors() {
        return sumOfErrors;
    }


    public boolean isZeroOverflowProtection() {
        return zeroOverflowProtection;
    }

    public void setZeroOverflowProtection(boolean zeroOverflowProtection) {
        this.zeroOverflowProtection = zeroOverflowProtection;
    }

    public double getMinimumPolicy() {
        return minimumPolicy;
    }

    public void setMinimumPolicy(double minimumPolicy) {
        this.minimumPolicy = minimumPolicy;
    }


}
