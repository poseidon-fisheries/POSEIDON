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

package uk.ac.ox.oxfish.fisher.heatmap.regression.tripbased;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.List;
import java.util.function.Function;

/**
 * A geographical regression which is in reality a container of multiple geographical regressions, each predicting catches/hr
 * of a given species and this function actually simulating profits for each
 * Created by carrknight on 7/14/16.
 */
public class ProfitFunctionRegression
    implements Function<SeaTile, double[]>,
    GeographicalRegression<TripRecord> {


    private final ProfitFunction profit;

    private final GeographicalRegression<Double>[] catches;


    private Fisher fisher;

    private double currentTime;

    private FishState state;


    @SuppressWarnings({"unchecked", "rawtypes"})
    public ProfitFunctionRegression(
        final ProfitFunction function,
        final AlgorithmFactory<? extends GeographicalRegression<Double>> regressionMaker,
        final FishState state
    ) {
        catches = new GeographicalRegression[state.getSpecies().size()];
        for (int i = 0; i < catches.length; i++) {
            catches[i] = regressionMaker.apply(state);
        }
        this.profit = function;

    }

    public ProfitFunctionRegression(
        final ProfitFunction function,
        final GeographicalRegression<Double>[] catches
    ) {
        this.catches = catches;
        this.profit = function;

    }

    @Override
    public double predict(
        final SeaTile tile, final double time, final Fisher fisher, final FishState model
    ) {

        return this.predict(tile, time, state, fisher, false);


    }


    public double predict(
        final SeaTile tile, final double time, final FishState state, final Fisher fisher, final boolean verbose
    ) {

        this.state = state;
        this.currentTime = time;
        this.fisher = fisher;
        return profit.hourlyProfitFromHypotheticalTripHere(fisher, tile, state, apply(tile), verbose);


    }

    /**
     * Returns all the prediction of catches per hour in an array (indexed for species)
     *
     * @param tile the function argument
     * @return the function result
     */
    @Override
    public double[] apply(final SeaTile tile) {

        final double[] expectedHourlyCatches = new double[catches.length];
        for (int i = 0; i < expectedHourlyCatches.length; i++)
            expectedHourlyCatches[i] = Math.max(
                0,
                catches[i].predict(tile, currentTime, fisher, state)
            );
        return expectedHourlyCatches;

    }

    @Override
    public void addObservation(
        final GeographicalObservation<TripRecord> observation,
        final Fisher fisher,
        final FishState model
    ) {
        for (int i = 0; i < catches.length; i++) {
            catches[i].addObservation(
                new GeographicalObservation<>(
                    observation.getTile(),
                    observation.getTime(),
                    //check only the very last hour spent fishing, otherwise it's too optimistic!
                    observation.getValue().getLastFishingRecordOfTile(
                        observation.getTile()).getFishCaught().getWeightCaught(i)
                ),
////                                     check the average during the trip
//                                                  observation.getValue().getFishingRecordOfTile(
//                                                          observation.getTile()).getFishCaught().getWeightCaught(i)),
                fisher, state
            );
        }
    }

    public GeographicalRegression<Double>[] catchesRegression() {
        return catches;
    }


    //ignored

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.state = model;
        for (final GeographicalRegression<?> reg : catches)
            reg.start(model, fisher);
    }

    //ignored

    @Override
    public void turnOff(final Fisher fisher) {
        for (final GeographicalRegression<?> reg : catches)
            reg.turnOff(fisher);
    }

    /**
     * It's already a double so return it!
     */
    @Override
    public double extractNumericalYFromObservation(
        final GeographicalObservation<TripRecord> observation, final Fisher fisher
    ) {
        return observation.getValue().getProfitPerHour(true);
    }


    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {

        double[] toReturn = new double[0];
        for (int i = 0; i < catches.length; i++)
            toReturn = FishStateUtilities.concatenateArray(toReturn, catches[i].getParametersAsArray());

        return toReturn;
    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(final double[] parameterArray) {


        final int numberOfParameters = catches[0].getParametersAsArray().length;
        assert parameterArray.length == numberOfParameters * catches.length;
        if (numberOfParameters > 0) {
            final List<double[]> parameters = FishStateUtilities.splitArray(parameterArray, numberOfParameters);
            assert parameters.size() == catches.length;
            for (int i = 0; i < catches.length; i++)
                catches[i].setParameters(parameters.get(i));
        }

    }
}
