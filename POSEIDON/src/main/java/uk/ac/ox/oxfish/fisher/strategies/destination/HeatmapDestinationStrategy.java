/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

public class HeatmapDestinationStrategy extends AbstractHeatmapDestinationStrategy<Double> {

    private static final long serialVersionUID = 5914354370676828557L;

    public HeatmapDestinationStrategy(
        final GeographicalRegression<Double> heatmap,
        final AcquisitionFunction acquisition,
        final boolean ignoreFailedTrips,
        final AdaptationProbability probability,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final int stepSize,
        final ObjectiveFunction<Fisher> objectiveFunction
    ) {
        super(heatmap, acquisition, ignoreFailedTrips, probability, map, random, stepSize, objectiveFunction);
    }

    @Override
    void learnFromTripRecord(
        final TripRecord record,
        final SeaTile mostFishedTile,
        final Fisher fisherThatMadeTheTrip,
        final FishState model
    ) {
        getHeatmap().addObservation(
            new GeographicalObservation<>(
                mostFishedTile,
                model.getHoursSinceStart(),
                objectiveFunction.computeCurrentFitness(fisher, fisherThatMadeTheTrip)
            ),
            fisher,
            model
        );
    }
}
