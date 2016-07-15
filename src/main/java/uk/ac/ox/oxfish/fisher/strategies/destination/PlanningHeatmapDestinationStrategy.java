package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.acquisition.AcquisitionFunction;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.ProfitFunctionRegression;
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
public class PlanningHeatmapDestinationStrategy extends HeatmapDestinationStrategy {




    public PlanningHeatmapDestinationStrategy(
            ProfitFunctionRegression profitRegression,
            AcquisitionFunction acquisition, boolean ignoreFailedTrips,
            AdaptationProbability probability,
            NauticalMap map, MersenneTwisterFast random, int stepSize) {
        super(profitRegression, acquisition, ignoreFailedTrips, probability, map, random, stepSize);
    }



    public static PlanningHeatmapDestinationStrategy AlmostPerfectKnowledge(
            double maxHours, int numberOfSpecies,
            AcquisitionFunction acquisition, boolean ignoreFailedTrips,
            AdaptationProbability probability,
            NauticalMap map, MersenneTwisterFast random, int stepSize
    ){

        GeographicalRegression<Double>[] catches = new GeographicalRegression[numberOfSpecies];
        for(int i=0; i<catches.length; i++)
            catches[i]= new AlmostPerfectKnowledgeRegression(i);
        return new PlanningHeatmapDestinationStrategy(
                new ProfitFunctionRegression(
                        new ProfitFunction(maxHours),
                        catches

                ),
                acquisition,ignoreFailedTrips,probability,map,random,stepSize
        );
    }




    @Override
    protected void learnFromTripRecord(
            TripRecord record, SeaTile mostFishedTile, Fisher fisher, FishState model) {

        ((ProfitFunctionRegression) getProfitRegression()).addObservation(
                new GeographicalObservation<>(mostFishedTile,model.getHoursSinceStart(),
                                              record),
                fisher
        );

    }


    /**
     * for now this class stays here as it really makes sense when used in this context and no other but might
     * make it more generic later
     */
    private static class AlmostPerfectKnowledgeRegression implements GeographicalRegression<Double>
    {

        private final int speciesIndex;

        public AlmostPerfectKnowledgeRegression(int speciesIndex) {
            this.speciesIndex = speciesIndex;
        }

        @Override
        public double predict(SeaTile tile, double time, FishState state, Fisher fisher) {
            return fisher.getGear().expectedHourlyCatch(fisher,tile,1,state.getBiology())[speciesIndex];
        }

        //ignored
        @Override
        public void addObservation(
                GeographicalObservation<Double> observation, Fisher fisher) {

        }
    }
}
