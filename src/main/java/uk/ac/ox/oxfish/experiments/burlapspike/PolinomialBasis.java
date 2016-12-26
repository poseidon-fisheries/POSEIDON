package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures;
import burlap.mdp.core.state.State;

/**
 * Created by carrknight on 12/19/16.
 */
public class PolinomialBasis implements DenseStateFeatures {



    private DenseStateFeatures delegate;

    private final int order;

    private final double intercept;


    public PolinomialBasis(DenseStateFeatures delegate, int order, double intercept) {
        this.delegate = delegate;
        this.order = order;
        this.intercept = intercept;
    }

    public PolinomialBasis(int order, int intercept) {
        this(new NumericVariableFeatures(), order, 1);
    }


    /**
     * Returns a feature vector represented as a double array for a given input state.
     *
     * @param s the input state to turn into a feature vector.
     * @return the feature vector represented as a double array.
     */
    @Override
    public double[] features(State s) {

        double[] delegateFeatures = delegate.features(s);
        double[] polinomial = new double[delegateFeatures.length*order+1];
        //always have an intercept
        polinomial[0] = intercept;
        int i=1;
        for(int original = 0; original<delegateFeatures.length; original++) {
            for (int currentOrder = 1; currentOrder <= order; currentOrder++) {
                polinomial[i++] = Math.pow(delegateFeatures[original],currentOrder);
            }
        }
        assert i == polinomial.length;
        return polinomial;
    }

    /**
     * Returns a copy of this {@link DenseStateFeatures}
     *
     * @return a copy of this {@link DenseStateFeatures}
     */
    @Override
    public DenseStateFeatures copy() {
        return new PolinomialBasis(delegate, order, 1);
    }
}
