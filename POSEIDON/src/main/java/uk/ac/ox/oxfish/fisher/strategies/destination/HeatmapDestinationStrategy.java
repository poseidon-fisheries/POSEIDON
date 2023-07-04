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
