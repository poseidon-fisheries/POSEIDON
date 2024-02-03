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

package uk.ac.ox.oxfish.utility.adaptation.maximization;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.AbstractAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by carrknight on 10/4/16.
 */
public class GravitationalSearchAdaptation<T> extends AbstractAdaptation<T> {

    private final int maximumSpeed = 2;
    private final DoubleParameter initialSpeed;
    /**
     * we work in a space of numbers but we might be controlling solutions that are
     * in fact not numbers but something else. This transforms back and forth
     */
    private final CoordinateTransformer<T> transformer;
    /**
     * how the agent should judge himself and others, which in this algorithm is really the mass
     */
    private final ObjectiveFunction<Fisher> mass;
    /**
     * the higher it is the faster you move towards higher mass
     */
    private final double gravitationalConstant;
    /**
     * you only copy this many people, and always the best ones
     */
    private final int explorationMaximum;
    /**
     * you need this in memory rather than always calling the transformer because you might lose information in terms
     * of discretization
     */
    private double[] currentCoordinates;
    private double[] speed;
    /**
     * bounds up and down the coordinates
     */
    private Consumer<double[]> coordinatesBounder = doubles -> {
    };
    public GravitationalSearchAdaptation(
        final Sensor<Fisher, T> sensor, final Actuator<Fisher, T> actuator,
        final Predicate<Fisher> validator,
        final CoordinateTransformer<T> transformer,
        final ObjectiveFunction<Fisher> mass, final double gravitationalConstant,
        final int explorationMaximum,
        final DoubleParameter initialSpeed,
        final MersenneTwisterFast random
    ) {
        super(sensor, actuator, validator);
        this.transformer = transformer;
        this.mass = mass;
        this.gravitationalConstant = gravitationalConstant;
        this.explorationMaximum = explorationMaximum;
        this.initialSpeed = initialSpeed;


    }

    @Override
    public void turnOff(final Fisher fisher) {

    }

    @Override
    protected void onStart(final FishState model, final Fisher fisher) {
        currentCoordinates = transformer.toCoordinates(
            getSensor().scan(fisher),
            fisher,
            model
        );

        if (currentCoordinates != null) {
            initializeSpeed(model);
        }
    }

    private void initializeSpeed(final FishState model) {
        speed = new double[currentCoordinates.length];
        for (int i = 0; i < speed.length; i++)
            speed[i] = initialSpeed.applyAsDouble(model.getRandom());
    }

    @Override
    public T concreteAdaptation(final Fisher toAdapt, final FishState state, final MersenneTwisterFast random) {
        if (currentCoordinates == null) {
            currentCoordinates = transformer.toCoordinates(
                getSensor().scan(toAdapt),
                toAdapt,
                state
            );
            if (currentCoordinates != null && speed == null) {
                initializeSpeed(state);
            }
            if (currentCoordinates == null)
                return null;
        }
        double personalMass = mass.computeCurrentFitness(toAdapt, toAdapt);
        //don't bother if you don't have mass or coordinates
        Preconditions.checkNotNull(currentCoordinates);
        Preconditions.checkState(Double.isFinite(personalMass), Arrays.toString(currentCoordinates));


        //turn list of fishers into a list of coordinates--->utility
        final List<MutablePair<double[], Double>> masses = state.getFishers().stream().map(
                fisher ->
                    new MutablePair<>(
                        transformer.toCoordinates(getSensor().scan(fisher), fisher, state),
                        mass.computeCurrentFitness(toAdapt, fisher)
                    ))
            //ignore those that have no coordinates or no utility
            .filter(pair -> pair.getFirst() != null &&
                pair.getSecond() != null &&
                Double.isFinite(pair.getSecond())).
            //collect them in a list
                collect(Collectors.toList());

        assert masses.size() >= 1; //we must be in at least
        if (masses.size() == 1) //don't bother if there is no one else
            return
                addSpeedAndReturn(new double[speed.length], random, toAdapt, state);

        //get best and worst
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (final MutablePair<double[], Double> currentMass : masses) {
            if (max < currentMass.getSecond())
                max = currentMass.getSecond();
            if (min > currentMass.getSecond())
                min = currentMass.getSecond();
        }
        //if we are all the same, don't bother
        if (max == min)
            addSpeedAndReturn(new double[speed.length], random, toAdapt, state);

        assert max > min;
        final double ratio = max - min;
        //reweigh everything (need to do this twice!)
        double sum = 0; //first we bound
        for (final MutablePair<double[], Double> currentMass : masses) {
            final double newValue = (currentMass.getSecond() - min) / ratio;
            currentMass.setSecond(newValue);
            sum += newValue;
        }
        //and then we normalize
        for (final MutablePair<double[], Double> currentMass : masses) {
            currentMass.setSecond(currentMass.getSecond() / sum);
        }
        personalMass = Math.max(((personalMass - min) / ratio) / sum, .00001);


        //get top K
        final List<MutablePair<double[], Double>> topMasses = masses.stream().sorted(
                (o1, o2) -> -Double.compare(o1.getSecond(), o2.getSecond())).
            limit(explorationMaximum).collect(Collectors.toList());


        //compute forces
        final double[] force = new double[currentCoordinates.length];
        for (final MutablePair<double[], Double> topMass : topMasses) {
            final double distance = euclideanDistance(currentCoordinates, topMass.getFirst());
            if (distance > 0) {
                for (int d = 0; d < currentCoordinates.length; d++) {
                    force[d] += random.nextDouble() *
                        gravitationalConstant * (-currentCoordinates[d] + topMass.getFirst()[d]) *
                        (personalMass * topMass.getSecond()) / (distance + 0.00001);
                    assert Double.isFinite(force[d]);
                }
            }
        }
        //now add inertia and you are dooone!
        for (int d = 0; d < currentCoordinates.length; d++)
            force[d] = force[d] / personalMass;

        return addSpeedAndReturn(force, random, toAdapt, state);

    }

    public T addSpeedAndReturn(
        final double[] acceleration, final MersenneTwisterFast random,
        final Fisher fisher,
        final FishState model
    ) {
        assert acceleration.length == speed.length;
        assert currentCoordinates.length == speed.length;
        for (int i = 0; i < acceleration.length; i++) {
            assert Double.isFinite(acceleration[i]);

            speed[i] = acceleration[i] + speed[i] * random.nextDouble();
            speed[i] = Math.min(speed[i], maximumSpeed);
            speed[i] = Math.max(speed[i], -maximumSpeed);
            currentCoordinates[i] = currentCoordinates[i] + speed[i];
            assert Double.isFinite(currentCoordinates[i]);
        }
        coordinatesBounder.accept(currentCoordinates);
        return transformer.fromCoordinates(currentCoordinates, fisher, model);
    }

    private double euclideanDistance(final double[] coordinate1, final double[] coordinate2) {
        double distance = 0;
        for (int i = 0; i < coordinate1.length; i++)
            distance += Math.pow(coordinate1[i] - coordinate2[i], 2);
        return distance;
    }

    public void setCoordinatesBounder(final Consumer<double[]> coordinatesBounder) {
        this.coordinatesBounder = coordinatesBounder;
    }

    public class MutablePair<A, B> {


        private A first;

        private B second;

        public MutablePair(final A first, final B second) {
            this.first = first;
            this.second = second;
        }


        public A getFirst() {
            return first;
        }

        /**
         * Setter for property 'first'.
         *
         * @param first Value to set for property 'first'.
         */
        public void setFirst(final A first) {
            this.first = first;
        }

        public B getSecond() {
            return second;
        }

        /**
         * Setter for property 'second'.
         *
         * @param second Value to set for property 'second'.
         */
        public void setSecond(final B second) {
            this.second = second;
        }
    }

}
