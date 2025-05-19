/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.AbstractHeatmapDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * A regression that works by Beam hill-climbing to update regression parameters of its delegate
 * Created by carrknight on 8/26/16.
 */
public class SocialTuningRegression<V> implements GeographicalRegression<V> {

    final static private Sensor<Fisher, double[]> parameterSensor =
        fisher -> ((AbstractHeatmapDestinationStrategy<?>) fisher.getDestinationStrategy())
            .getHeatmap()
            .getParametersAsArray();
    /**
     * the underlying regression doing all the work
     */
    private final GeographicalRegression<V> delegate;
    private final AdaptationProbability probability;
    final private boolean yearly;
    final private AdaptationAlgorithm<double[]> optimizer;
    private ExploreImitateAdaptation<double[]> adaptation;


    public SocialTuningRegression(
        final GeographicalRegression<V> delegate,
        final AdaptationProbability probability,
        final boolean yearly
    ) {
        this.delegate = delegate;
        this.probability = probability;

        this.yearly = yearly;


        optimizer = new BeamHillClimbing<double[]>(
            (state, random, fisher, current) -> {
                final double[] toReturn = Arrays.copyOf(current, current.length);
                for (int i = 0; i < current.length; i++)
                    toReturn[i] = toReturn[i] * (.95 + random.nextDouble() * .1);
                return toReturn;
            }
        );


    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        delegate.start(model, fisher);
        //start the optimizer!
        final Predicate<Fisher> predictate = fisher12 -> fisher12.getDestinationStrategy() instanceof AbstractHeatmapDestinationStrategy;
        final Actuator<Fisher, double[]> actuator = (fisher1, change, model1) -> model1.scheduleOnce(
            (Steppable) simState ->
                ((AbstractHeatmapDestinationStrategy<?>) fisher1.getDestinationStrategy())
                    .getHeatmap()
                    .setParameters(Arrays.copyOf(change, change.length)),
            StepOrder.DAWN
        );

        if (yearly) {
            adaptation = new ExploreImitateAdaptation<>(
                predictate,
                optimizer,
                actuator,
                parameterSensor,
                new CashFlowObjective(365), probability, a -> true
            );
            fisher.addYearlyAdaptation(
                adaptation);
        }
        //else bimonthly
        else {
            adaptation = new ExploreImitateAdaptation<>(
                predictate,
                optimizer,
                actuator,
                parameterSensor,
                new CashFlowObjective(60), 0, 1, a -> true
            );
            fisher.addBiMonthlyAdaptation(adaptation);
        }


    }

    @Override
    public void turnOff(final Fisher fisher) {
        delegate.turnOff(fisher);
        if (adaptation != null) {
            if (yearly)
                fisher.removeYearlyAdaptation(adaptation);
            else
                fisher.removeBiMonthlyAdaptation(adaptation);
        }
    }

    /**
     * predict numerical value here
     *
     * @param tile
     * @param time
     * @param fisher
     * @param model
     * @return
     */
    @Override
    public double predict(final SeaTile tile, final double time, final Fisher fisher, final FishState model) {
        return delegate.predict(tile, time, fisher, model);
    }

    /**
     * learn from this observation
     *
     * @param observation
     * @param fisher
     * @param model
     */
    @Override
    public void addObservation(
        final GeographicalObservation<V> observation,
        final Fisher fisher, final FishState model
    ) {
        delegate.addObservation(observation, fisher, model);
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
        final GeographicalObservation<V> observation,
        final Fisher fisher
    ) {
        return delegate.extractNumericalYFromObservation(observation, fisher);
    }

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return delegate.getParametersAsArray();
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(final double[] parameterArray) {
        delegate.setParameters(parameterArray);
    }


    /**
     * Getter for property 'probability'.
     *
     * @return Value for property 'probability'.
     */
    public AdaptationProbability getProbability() {
        return probability;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }
}
