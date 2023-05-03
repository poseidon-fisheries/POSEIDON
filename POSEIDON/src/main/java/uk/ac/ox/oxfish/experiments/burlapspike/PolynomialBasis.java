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

package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.functionapproximation.dense.NumericVariableFeatures;
import burlap.mdp.core.state.State;

/**
 * Created by carrknight on 12/19/16.
 */
public class PolynomialBasis implements DenseStateFeatures {



    private DenseStateFeatures delegate;

    private final int order;

    private final double intercept;


    public PolynomialBasis(DenseStateFeatures delegate, int order, double intercept) {
        this.delegate = delegate;
        this.order = order;
        this.intercept = intercept;
    }

    public PolynomialBasis(int order, int intercept) {
        this(new NumericVariableFeatures(), order, intercept);
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
        return new PolynomialBasis(delegate, order, 1);
    }
}
