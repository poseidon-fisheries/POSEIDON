package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.OneDimensionalKalmanFilter;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFDistance;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleKalmanRegression implements GeographicalRegression<Double> {


    /**
     * 1/exp(-distance/h) will be our distance penalty
     */
    private final RBFDistance distancePenalty;

    private double evidenceUncertainty;

    /**
     * the daily gaussian noise to add to uncertainty for each filter
     */
    private double drift;


    private final double minValue;

    /**
     * the prediction is mean + optimism * error
     */
    private double optimism;


    private final double maxValue;

    private double initialUncertainty;

    /**
     * if this is different from 0 then our emission model is z = (1+fishingHerePenalty)x
     * that is we assume our observation is slightly biased upward (since we might think we are ruining the spot for later)
     */
    private double fishingHerePenalty;

    private final HashMap<SeaTile,OneDimensionalKalmanFilter> filters = new HashMap<>();

    private final NauticalMap map;

    /**
     * geographical distance
     */
    private final static ManhattanDistance distancer = new ManhattanDistance() ;

    public SimpleKalmanRegression(
            double distancePenalty,
            double drift,
            double minValue,
            double maxValue,
            double initialVariance,
            double evidenceUncertainty,
            double optimism,
            double fishingHerePenalty,
            NauticalMap map,
            MersenneTwisterFast random) {
        this.distancePenalty =  new RBFDistance(distancePenalty);
        this.drift = drift;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.initialUncertainty = initialVariance;
        this.fishingHerePenalty = fishingHerePenalty;
        this.map = map;
        this.evidenceUncertainty = evidenceUncertainty;
        this.optimism = optimism;

        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for(SeaTile tile : tiles)
            filters.put(tile,new OneDimensionalKalmanFilter(
                    1,1,initialVariance,
                    random.nextDouble()*(maxValue-minValue) + minValue,
                    drift
            ));


    }


    private Stoppable receipt;


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        //every morning drift out a bit
        receipt = model.scheduleEveryDay(new Steppable() {
            @Override
            public void step(SimState simState) {
                for (OneDimensionalKalmanFilter filter : filters.values())
                    filter.elapseTime();
            }
        }, StepOrder.DAWN);
    }

    @Override
    public double predict(
            SeaTile tile, double time, Fisher fisher, FishState model) {
        OneDimensionalKalmanFilter kalmanFilter = filters.get(tile);
        return kalmanFilter == null ? Double.NaN :
                kalmanFilter.getStateEstimate() + optimism * kalmanFilter.getStandardDeviation();
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff(Fisher fisher) {
        if(receipt!= null)
            receipt.stop();
    }

    @Override
    public void addObservation(
            GeographicalObservation<Double> observation, Fisher fisher, FishState model)
    {

        for(Map.Entry<SeaTile,OneDimensionalKalmanFilter> filter : filters.entrySet())
        {

            double rbfDistance = distancePenalty.transform(
                    distancer.distance(filter.getKey(),observation.getTile()));
            double evidencePenalty = evidenceUncertainty + (1/ rbfDistance-1);

             if(!Double.isFinite(evidencePenalty ) || rbfDistance <= 0.0001) //don't bother with extremely small information
                 continue;

            if(rbfDistance == 1 && fishingHerePenalty != 0 )
            {
                filter.getValue().setEmissionMultiplier(1+fishingHerePenalty);
            }

            filter.getValue().observe(observation.getValue(),
                                      evidencePenalty);

            filter.getValue().setEmissionMultiplier(1);

        }
    }


    /**
     * Getter for property 'distancePenalty'.
     *
     * @return Value for property 'distancePenalty'.
     */
    public RBFDistance getDistancePenalty() {
        return distancePenalty;
    }

    /**
     * Getter for property 'evidenceUncertainty'.
     *
     * @return Value for property 'evidenceUncertainty'.
     */
    public double getEvidenceUncertainty() {
        return evidenceUncertainty;
    }

    /**
     * Getter for property 'optimism'.
     *
     * @return Value for property 'optimism'.
     */
    public double getOptimism() {
        return optimism;
    }

    /**
     * Getter for property 'fishingHerePenalty'.
     *
     * @return Value for property 'fishingHerePenalty'.
     */
    public double getFishingHerePenalty() {
        return fishingHerePenalty;
    }

    public double getDrift() {
        return drift;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getInitialUncertainty() {
        return initialUncertainty;
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


        return  new double[]{
            distancePenalty.getBandwidth(),
                evidenceUncertainty,
                drift,
                optimism,
                fishingHerePenalty
        };

    }

    /**
     * given an array of parameters (of size equal to what you'd get if you called the getter) the regression is supposed
     * to transition to these parameters
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameterArray) {
        assert parameterArray.length == 5;
        distancePenalty.setBandwidth(parameterArray[0]);
        evidenceUncertainty = parameterArray[1];
        drift = parameterArray[2];
        for(OneDimensionalKalmanFilter kalman : filters.values())
            kalman.setDrift(drift);
        optimism = parameterArray[3];
        fishingHerePenalty = parameterArray[4];
    }
}
