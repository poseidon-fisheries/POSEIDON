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

package uk.ac.ox.oxfish.fisher.heatmap.regression;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.AdaptationAlgorithm;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * A regression that works by Beam hill-climbing to update regression parameters of its delegate
 * Created by carrknight on 8/26/16.
 */
public class SocialTuningRegression<V> implements GeographicalRegression<V> {

    final static private Sensor<Fisher, double[]> parameterSensor = new Sensor<Fisher, double[]>() {
        @Override
        public double[] scan(Fisher fisher) {
            return ((HeatmapDestinationStrategy) fisher.getDestinationStrategy()).getHeatmap().getParametersAsArray();
        }
    };
    /**
     * the underlying regression doing all the work
     */
    private final GeographicalRegression<V> delegate;
    private final AdaptationProbability probability;
    final private boolean yearly;
    final private AdaptationAlgorithm<double[]> optimizer;
    private ExploreImitateAdaptation<double[]> adaptation;


    public SocialTuningRegression(
        GeographicalRegression<V> delegate,
        AdaptationProbability probability,
        boolean yearly
    ) {
        this.delegate = delegate;
        this.probability = probability;

        this.yearly = yearly;


        optimizer = new BeamHillClimbing<double[]>(
            new RandomStep<double[]>() {
                @Override
                public double[] randomStep(
                    FishState state, MersenneTwisterFast random, Fisher fisher,
                    double[] current
                ) {
                    double[] toReturn = Arrays.copyOf(current, current.length);
                    for (int i = 0; i < current.length; i++)
                        toReturn[i] = toReturn[i] * (.95 + random.nextDouble() * .1);
                    return toReturn;
                }
            }
        );


    }

    @Override
    public void start(FishState model, Fisher fisher) {
        delegate.start(model, fisher);
        //start the optimizer!
        Predicate<Fisher> predictate = new Predicate<Fisher>() {
            @Override
            public boolean test(Fisher fisher) {
                return fisher.getDestinationStrategy() instanceof HeatmapDestinationStrategy;
            }
        };
        Actuator<Fisher, double[]> actuator = new Actuator<Fisher, double[]>() {
            @Override
            public void apply(Fisher fisher, double[] change, FishState model) {
                model.scheduleOnce(
                    new Steppable() {
                        @Override
                        public void step(SimState simState) {
                            ((HeatmapDestinationStrategy) fisher.getDestinationStrategy()).getHeatmap().setParameters(
                                Arrays.copyOf(change, change.length));
                        }
                    },
                    StepOrder.DAWN
                );

            }
        };

        if (yearly) {
            adaptation = new ExploreImitateAdaptation<>(
                predictate,
                optimizer,
                actuator,
                parameterSensor,
                new CashFlowObjective(365), probability, new Predicate<double[]>() {
                @Override
                public boolean test(double[] a) {
                    return true;
                }
            }
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
                new CashFlowObjective(60), 0, 1, new Predicate<double[]>() {
                @Override
                public boolean test(double[] a) {
                    return true;
                }
            }
            );
            fisher.addBiMonthlyAdaptation(adaptation);
        }


    }

    @Override
    public void turnOff(Fisher fisher) {
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
    public double predict(SeaTile tile, double time, Fisher fisher, FishState model) {
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
        GeographicalObservation<V> observation,
        Fisher fisher, FishState model
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
        GeographicalObservation<V> observation,
        Fisher fisher
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
    public void setParameters(double[] parameterArray) {
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
