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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.tripbased.ProfitFunctionRegression;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

/**
 * Like a heatmap destination strategy but uses the profit function regression to learn and predict
 * Created by carrknight on 7/14/16.
 */
public class PlanningHeatmapDestinationStrategy
    extends AbstractHeatmapDestinationStrategy<TripRecord> {

    private static final long serialVersionUID = -5616412244667234466L;
    private final ProfitFunctionRegression regression;


    public PlanningHeatmapDestinationStrategy(
        final ProfitFunctionRegression profitRegression,
        final AcquisitionFunction acquisition,
        final boolean ignoreFailedTrips,
        final AdaptationProbability probability,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final int stepSize
    ) {
        super(
            profitRegression,
            acquisition,
            ignoreFailedTrips,
            probability,
            map,
            random,
            stepSize,
            null
        );
        this.regression = profitRegression;

    }


    public static PlanningHeatmapDestinationStrategy AlmostPerfectKnowledge(
        final double maxHours, final int numberOfSpecies,
        final AcquisitionFunction acquisition, final boolean ignoreFailedTrips,
        final AdaptationProbability probability,
        final NauticalMap map, final MersenneTwisterFast random, final int stepSize,
        final GlobalBiology biology
    ) {

        @SuppressWarnings({"unchecked", "rawtypes"}) final GeographicalRegression<Double>[] catches =
            new GeographicalRegression[numberOfSpecies];
        for (int i = 0; i < catches.length; i++)
            catches[i] = new AlmostPerfectKnowledgeRegression(i, biology);
        return new PlanningHeatmapDestinationStrategy(
            new ProfitFunctionRegression(
                new ProfitFunction(maxHours),
                catches

            ),
            acquisition, ignoreFailedTrips, probability, map, random, stepSize
        );
    }


    @Override
    protected void learnFromTripRecord(
        final TripRecord record,
        final SeaTile mostFishedTile,
        final Fisher fisherThatMadeTheTrip,
        final FishState model
    ) {
        regression.addObservation(
            new GeographicalObservation<>(mostFishedTile, model.getHoursSinceStart(), record),
            fisherThatMadeTheTrip,
            model
        );

    }

    /**
     * Getter for property 'regression'.
     *
     * @return Value for property 'regression'.
     */
    public ProfitFunctionRegression getRegression() {
        return regression;
    }

    /**
     * for now this class stays here as it really makes sense when used in this context and no other but might
     * make it more generic later
     */
    private static class AlmostPerfectKnowledgeRegression implements GeographicalRegression<Double> {

        private final int speciesIndex;

        private final GlobalBiology biology;


        public AlmostPerfectKnowledgeRegression(final int speciesIndex, final GlobalBiology biology) {
            this.speciesIndex = speciesIndex;
            this.biology = biology;
        }

        @Override
        public double predict(final SeaTile tile, final double time, final Fisher fisher, final FishState model) {
            return fisher.getGear().expectedHourlyCatch(fisher, tile, 1, biology)[speciesIndex];
        }


        //ignored
        @Override
        public void addObservation(
            final GeographicalObservation<Double> observation, final Fisher fisher, final FishState model
        ) {

        }

        /**
         * ignored
         */
        @Override
        public void start(final FishState model, final Fisher fisher) {

        }

        /**
         * ignored
         */
        @Override
        public void turnOff(final Fisher fisher) {

        }

        /**
         * It's already a double so return it!
         */
        @Override
        public double extractNumericalYFromObservation(
            final GeographicalObservation<Double> observation, final Fisher fisher
        ) {
            return observation.getValue();
        }

        /**
         * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
         * from the outside without knowing the inner workings of the regression
         *
         * @return an array containing all the parameters of the model
         */
        @Override
        public double[] getParametersAsArray() {
            return new double[0];
        }

        /**
         * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
         * to transition to these parameters
         *
         * @param parameterArray the new parameters for this regresssion
         */
        @Override
        public void setParameters(final double[] parameterArray) {
            Preconditions.checkState(false, "perfect knowledge has no parameters to set!");
        }
    }
}
