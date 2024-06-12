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

package uk.ac.ox.oxfish.fisher.heatmap.regression.basis;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AtomicDouble;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map.Entry;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Created by carrknight on 3/7/17.
 */
public class RBFNetworkRegression implements GeographicalRegression<Double> {

    /**
     * a list of pairs (rather than a map just because the link is so strong) Basis  to its weight
     * when making predictions!
     */
    private final LinkedList<Entry<RBFBasis, AtomicDouble>> network;

    /**
     * functions used to turn an observation into a double[]
     */
    private final ObservationExtractor[] extractors;

    private final double learningRate;

    public RBFNetworkRegression(
        final ObservationExtractor[] extractors,
        final int order,
        final double[] min,
        final double[] max,
        final double learningRate,
        final double initialWeight
    ) {

        Preconditions.checkArgument(max.length == min.length);
        Preconditions.checkArgument(max.length == extractors.length);
        this.extractors = extractors;
        this.learningRate = learningRate;
        // check each step
        final double[] step = new double[max.length];
        for (int i = 0; i < step.length; i++) {
            step[i] = (max[i] - min[i]) / (double) (order - 1);
        }

        final double initialBandwidth = 2 * (Arrays.stream(step).max().getAsDouble());

        this.network = new LinkedList<>();

        // builds combinations without recursion
        // taken mostly from here : http://stackoverflow.com/a/29910788/975904
        final int totalDimension = (int) Math.pow(order, extractors.length);
        for (int i = 0; i < totalDimension; i++) {
            final double[] indices = new double[min.length];

            // how often we need to reset
            for (int j = 0; j < extractors.length; j++) {
                final int period = (int) Math.pow(order, extractors.length - j - 1);

                final int index = i / period % order;
                indices[j] = min[j] + step[j] * index;

            }
            System.out.println(Arrays.toString(indices));
            network.add(entry(
                new RBFBasis(
                    initialBandwidth,
                    Arrays.copyOf(indices, indices.length)
                ),
                new AtomicDouble(initialWeight)
            ));
        }

    }

    /**
     * Getter for property 'network'.
     *
     * @return Value for property 'network'.
     */
    public LinkedList<Entry<RBFBasis, AtomicDouble>> getNetwork() {
        return network;
    }

    /**
     * Getter for property 'learningRate'.
     *
     * @return Value for property 'learningRate'.
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * extract numerical values for the x and feed that array into the RBF network to predict
     *
     * @param tile   tile where we are predicting
     * @param time   time at which we are predicting
     * @param fisher fisher making prediction
     * @param model  the rest of the model
     * @return a number representing the sum of weighted basis
     */
    @Override
    public double predict(
        final SeaTile tile,
        final double time,
        final Fisher fisher,
        final FishState model
    ) {

        return predict(extractObservation(tile, time, fisher, model));
    }

    /**
     * standard RBF network sum of weighted distances from center
     */
    private double predict(final double[] observation) {

        double sum = 0;
        for (final Entry<RBFBasis, AtomicDouble> basis : network) {
            sum += basis.getValue().doubleValue() * basis.getKey().evaluate(observation);
        }
        return sum;
    }

    private double[] extractObservation(
        final SeaTile tile,
        final double time,
        final Fisher fisher,
        final FishState model
    ) {
        final double[] observation = new double[extractors.length];
        for (int i = 0; i < observation.length; i++) {
            observation[i] = extractors[i].extract(tile, time, fisher, model);
        }
        return observation;
    }

    /**
     * Do gradient descent on the weights of each basis
     *
     * @param observation
     * @param fisher
     * @param model
     */
    @Override
    public void addObservation(
        final GeographicalObservation<Double> observation,
        final Fisher fisher,
        final FishState model
    ) {

        // get x and y
        final double[] x = extractObservation(observation.getTile(), observation.getTime(),
            fisher, model
        );
        final double y = observation.getValue();
        // now get prediction
        final double prediction = predict(x);

        // gradient descent!
        final double increment = learningRate * (y - prediction);
        for (final Entry<RBFBasis, AtomicDouble> basis : network) {
            // todo you can make this faster by storing the evaluate from the predict call!
            basis.getValue().set(basis.getValue().doubleValue() +
                increment * basis.getKey().evaluate(x));
        }

    }

    /**
     * turn the "V" value of the geographical observation into a number
     *
     * @param observation
     * @param fisher
     * @return
     */
    @Override
    public double extractNumericalYFromObservation(
        final GeographicalObservation<Double> observation,
        final Fisher fisher
    ) {
        return observation.getValue();
    }

    /**
     * Parameters are the bandwidths of each basis
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        final double[] parameters = new double[network.size()];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = network.get(i).getKey().getBandwidth();
        }
        return parameters;
    }

    /**
     * set each element of the array as a basis' bandwidth
     *
     * @param parameters the new parameters for this regresssion
     */
    @Override
    public void setParameters(final double[] parameters) {
        for (int i = 0; i < parameters.length; i++) {
            network.get(i).getKey().setBandwidth(parameters[i]);
        }
    }

    @Override
    public void start(
        final FishState model,
        final Fisher fisher
    ) {

    }

    @Override
    public void turnOff(final Fisher fisher) {

    }
}
