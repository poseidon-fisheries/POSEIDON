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
public class PlanningHeatmapDestinationStrategy extends HeatmapDestinationStrategy {


    private final ProfitFunctionRegression regression;


    public PlanningHeatmapDestinationStrategy(
            ProfitFunctionRegression profitRegression,
            AcquisitionFunction acquisition, boolean ignoreFailedTrips,
            AdaptationProbability probability,
            NauticalMap map, MersenneTwisterFast random, int stepSize) {
        super(profitRegression, acquisition, ignoreFailedTrips, probability, map, random, stepSize);
        this.regression = profitRegression;

    }



    public static PlanningHeatmapDestinationStrategy AlmostPerfectKnowledge(
            double maxHours, int numberOfSpecies,
            AcquisitionFunction acquisition, boolean ignoreFailedTrips,
            AdaptationProbability probability,
            NauticalMap map, MersenneTwisterFast random, int stepSize,
            GlobalBiology biology){

        GeographicalRegression<Double>[] catches = new GeographicalRegression[numberOfSpecies];
        for(int i=0; i<catches.length; i++)
            catches[i]= new AlmostPerfectKnowledgeRegression(i,biology);
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

         regression.addObservation(
                new GeographicalObservation<>(mostFishedTile,model.getHoursSinceStart(),
                                              record),
                fisher
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
    private static class AlmostPerfectKnowledgeRegression implements GeographicalRegression<Double>
    {

        private final int speciesIndex;

        private final GlobalBiology biology;


        public AlmostPerfectKnowledgeRegression(int speciesIndex, GlobalBiology biology) {
            this.speciesIndex = speciesIndex;
            this.biology = biology;
        }

        @Override
        public double predict(SeaTile tile, double time, Fisher fisher) {
            return fisher.getGear().expectedHourlyCatch(fisher,tile,1,biology)[speciesIndex];
        }


        //ignored
        @Override
        public void addObservation(
                GeographicalObservation<Double> observation, Fisher fisher) {

        }

        /**
         * ignored
         */
        @Override
        public void start(FishState model,Fisher fisher) {

        }

        /**
         * ignored
         */
        @Override
        public void turnOff(Fisher fisher) {

        }

        /**
         * It's already a double so return it!
         */
        @Override
        public double extractNumericalYFromObservation(
                GeographicalObservation<Double> observation, Fisher fisher) {
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
        public void setParameters(double[] parameterArray) {
            Preconditions.checkState(false, "perfect knowledge has no parameters to set!");
        }
    }
}
