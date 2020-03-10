package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.util.LinkedList;
import java.util.List;

import static uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter.quickParametrize;

/**
 * like simple optimization parameter, but puts the same number at multiple addresses
 *
 *
 */
public class MultipleOptimizationParameter implements OptimizationParameter {



    private List<String> addressesToModify =  new LinkedList<>(); //"literPerKilometer";


    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents -10
     */
    private double minimum = 0;

    /**
     * assuming x comes in ranges of -10 to 10 (EVA dumb default), this represents 10
     */
    private double maximum = 5;


    private boolean alwaysPositive = false;

    public MultipleOptimizationParameter() {
    }

    public MultipleOptimizationParameter(List<String> addressesToModify, double minimum, double maximum) {
        this.addressesToModify = addressesToModify;
        this.minimum = minimum;
        this.maximum = maximum;
    }


    @Override
    public int size() {
        return 1;
    }

    @Override
    public String parametrize(Scenario scenario, double[] inputs) {
        double realValue =minimum+((maximum-minimum)/(10-(-10)))*(inputs[0] - (-10));
        if(realValue < 0 & alwaysPositive)
            realValue = 0;

        for (String addressToModify : addressesToModify) {
            quickParametrize(scenario, realValue, addressToModify);

        }

        return String.valueOf(realValue);

    }

    @Override
    public String getName() {
        return addressesToModify.get(0);
    }

    public List<String> getAddressesToModify() {
        return addressesToModify;
    }

    public void setAddressesToModify(List<String> addressesToModify) {
        this.addressesToModify = addressesToModify;
    }

    public double getMinimum() {
        return minimum;
    }

    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    public double getMaximum() {
        return maximum;
    }

    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    public boolean isAlwaysPositive() {
        return alwaysPositive;
    }

    public void setAlwaysPositive(boolean alwaysPositive) {
        this.alwaysPositive = alwaysPositive;
    }
}
