package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.xmlpull.v1.builder.xpath.jaxen.expr.Step;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFKernel;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RegressionDistance;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.Distance;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Inspired by folk interviews, what if you are only classifying spots as good/bad.
 * We assume actual draws are ~N(theta,sigma) where theta is different for good and bad spots
 * Created by carrknight on 8/22/16.
 */
public class GoodBadRegression implements GeographicalRegression<Double>, Steppable
{


    /**
     * what subjective probability do we give to this spot being good
     */
    private final HashMap<SeaTile,Double> spots;


    /**
     * gives us the theta for the bad prior
     */
    private Function<Fisher, Double> badAverageExtractor;

    /**
     * gives us the theta for the good prior
     */
    private Function<Fisher, Double> goodAverageExtractor;


    private Function<Fisher, Double> stdExtractor;

    /**
     * its inverse penalizes observations that are far so that the priors are stronger
     * the penalty comes by dividing sigma by the the RBF Kernel
     */
    private RBFKernel distancePenalty;

    /**
     * daily drift of probabilities towards the middle
     */
    private final double drift;

    public GoodBadRegression(
            NauticalMap map,
            Distance distance,
            MersenneTwisterFast random,
            Function<Fisher, Double> badAverageExtractor,
            Function<Fisher, Double> goodAverageExtractor,
            Function<Fisher, Double> stdExtractor,
            double distanceBandwidth,
            double drift
    ) {
        this.drift = drift;
        this.badAverageExtractor = badAverageExtractor;
        this.goodAverageExtractor = goodAverageExtractor;
        this.stdExtractor = stdExtractor;
        this.distancePenalty = new RBFKernel(new RegressionDistance() {
            @Override
            public double distance(
                    Fisher fisher, SeaTile tile, double currentTimeInHours, GeographicalObservation observation) {
                return distance.distance(tile,observation.getTile(),map);
            }
        },distanceBandwidth);

        //each tile its own random probability
        spots = new HashMap<>();
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for(SeaTile tile : tiles) {
            spots.put(tile,random.nextDouble());
        }

    }

    public GoodBadRegression(
            NauticalMap map,
            Distance distance,
            MersenneTwisterFast random,
            double badAverage,
            double goodAverage,
            double deviation,
            double distanceBandwidth,
            double drift){
        this(map, distance, random,
             new Function<Fisher, Double>() {
                 @Override
                 public Double apply(Fisher tile) {
                     return badAverage;
                 }
             },
             new Function<Fisher, Double>() {
                 @Override
                 public Double apply(Fisher tile) {
                     return goodAverage;
                 }
             },
             new Function<Fisher, Double>() {
                 @Override
                 public Double apply(Fisher tile) {
                     return deviation;
                 }
             },
             distanceBandwidth,drift);

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
        receipt = model.scheduleEveryDay(
                this, StepOrder.DAWN);
    }


    @Override
    public void step(SimState simState) {
        for (Map.Entry<SeaTile, Double> probability : spots.entrySet()) {
            double good = probability.getValue();
            Preconditions.checkArgument(good >= 0);
            Preconditions.checkArgument(good <= 1);
            double bad = 1 - good;
            probability.setValue((good + drift) / (good + drift + bad + drift));
        }
    }
    /**
     * learn from this observation
     *
     * @param observation
     * @param fisher
     */
    @Override
    public void addObservation(
            GeographicalObservation<Double> observation, Fisher fisher)
    {

        for(Map.Entry<SeaTile,Double> probability : spots.entrySet())
        {
            double rbf = distancePenalty.distance(
                    fisher, probability.getKey(), observation.getTime(), observation);
            //if the evidence has even a shred of strenght, update
            if(rbf >= FishStateUtilities.EPSILON)
            {
                double evidenceStrength = 1d/ rbf;

                double goodAverage =  goodAverageExtractor.apply(fisher);
                double badAverage = badAverageExtractor.apply(fisher);
                double sigma = stdExtractor.apply(fisher);


                double goodPrior = probability.getValue();
                double goodLikelihood = FishStateUtilities.normalPDF(
                        goodAverage,sigma*evidenceStrength).apply(observation.getValue());
                double goodPosterior =  goodPrior *goodLikelihood;
                assert  Double.isFinite(goodPosterior);
                assert  goodPosterior >=0;


                double badPrior = probability.getValue();
                double badLikelihood = FishStateUtilities.normalPDF(
                        badAverage,sigma*evidenceStrength).apply(observation.getValue());
                double badPosterior = badPrior*badLikelihood;
                assert  badPosterior >=0;
                assert  Double.isFinite(badPosterior);


                probability.setValue(goodPosterior/(badPosterior+goodPosterior));
            }
        }

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        if(receipt!= null)
            receipt.stop();
    }

    /**
     * predict numerical value here
     *
     * @param tile
     * @param time
     * @param fisher
     * @return
     */
    @Override
    public double predict(SeaTile tile, double time, Fisher fisher) {

        Double probabilityGood = spots.get(tile);
        if(probabilityGood==null)
            return Double.NaN;
        else
        {
            double goodAverage = goodAverageExtractor.apply(fisher);
            double badAverage = badAverageExtractor.apply(fisher);
            return probabilityGood * goodAverage + (1-probabilityGood) * badAverage;
        }


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
            GeographicalObservation<Double> observation, Fisher fisher) {
        return observation.getValue();
    }
}
