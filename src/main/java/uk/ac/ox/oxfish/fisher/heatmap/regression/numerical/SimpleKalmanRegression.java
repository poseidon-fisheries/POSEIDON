package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.bayes.OneDimensionalKalmanFilter;
import uk.ac.ox.oxfish.geography.ManhattanDistance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimpleKalmanRegression implements NumericalGeographicalRegression
{


    /**
     * the % increase per unit of distance in evidence variance
     */
    private final double distancePenalty;

    private final double evidenceUncertainty;

    /**
     * the daily gaussian noise to add to uncertainty for each filter
     */
    private final double drift;


    private final double minValue;

    /**
     * the prediction is mean + optimism * error
     */
    private final double optimism;


    private final double maxValue;

    private final double initialUncertainty;

    private final HashMap<SeaTile,OneDimensionalKalmanFilter> filters = new HashMap<>();

    private final NauticalMap map;

    private final static ManhattanDistance distancer = new ManhattanDistance() ;

    public SimpleKalmanRegression(
            double distancePenalty,
            double drift,
            double minValue,
            double maxValue,
            double initialVariance,
            double evidenceUncertainty,
            double optimism, NauticalMap map,
            MersenneTwisterFast random) {
        this.distancePenalty = distancePenalty;
        this.drift = drift;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.initialUncertainty = initialVariance;
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
    public void start(FishState model) {
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
            SeaTile tile, double time, FishState state, Fisher fisher) {
        OneDimensionalKalmanFilter kalmanFilter = filters.get(tile);
        return kalmanFilter == null ? Double.NaN :
                kalmanFilter.getStateEstimate() + optimism * kalmanFilter.getStandardDeviation();
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if(receipt!= null)
            receipt.stop();
    }

    @Override
    public void addObservation(
            GeographicalObservation<Double> observation, Fisher fisher)
    {

        for(Map.Entry<SeaTile,OneDimensionalKalmanFilter> filter : filters.entrySet())
        {


            filter.getValue().observe(extractNumericalObservation(observation),
                                      extractObservationUncertainty(observation, filter));

        }
    }

    private double extractObservationUncertainty(
            GeographicalObservation<Double> observation, Map.Entry<SeaTile, OneDimensionalKalmanFilter> filter)
    {
        double distance = distancer.distance(observation.getTile(),filter.getKey(),map);
        return evidenceUncertainty + distancePenalty * (distance*distance);
    }

    private Double extractNumericalObservation(GeographicalObservation<Double> observation) {
        return observation.getValue();
    }


    public double getDistancePenalty() {
        return distancePenalty;
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
}
