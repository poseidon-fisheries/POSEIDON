package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.distance.RBFKernel;
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
    private double badAverage;

    /**
     * gives us the theta for the good prior
     */
    private double goodAverage;


    private double standardDeviation;
    /**
     * its inverse penalizes observations that are far so that the priors are stronger
     * the penalty comes by dividing sigma by the the RBF Kernel
     */
    private RBFKernel distancePenalty;

    /**
     * daily drift of probabilities towards the middle
     */
    private final double drift;

    private final Distance distance;

    private final NauticalMap map;

    public GoodBadRegression(
            NauticalMap map,
            Distance distance,
            MersenneTwisterFast random,
            double badAverage,
            double goodAverage,
            double deviation,
            double distanceBandwidth,
            double drift
    ) {
        this.map = map;
        this.drift = drift;
        this.badAverage = badAverage;
        this.goodAverage = goodAverage;
        this.standardDeviation = deviation;
        this.distance = distance;
        this.distancePenalty = new RBFKernel(distanceBandwidth);

        //each tile its own random probability
        spots = new HashMap<>();
        List<SeaTile> tiles = map.getAllSeaTilesExcludingLandAsList();
        for(SeaTile tile : tiles) {
            spots.put(tile,random.nextDouble());
        }

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
            double distance = this.distance.distance(probability.getKey(),
                                            observation.getTile(),
                                            map);
            double rbf = distancePenalty.transform(distance);
            //if the evidence has even a shred of strenght, update
            if(rbf >= FishStateUtilities.EPSILON)
            {
                double evidenceStrength = 1d/ rbf;


                //all that follows is standard bayes



                double goodPrior = probability.getValue();
                double goodLikelihood = FishStateUtilities.normalPDF(
                        goodAverage,standardDeviation*evidenceStrength).apply(observation.getValue());
                double goodPosterior =  goodPrior *goodLikelihood;
                assert  Double.isFinite(goodPosterior);
                assert  goodPosterior >=0;


                double badPrior = probability.getValue();
                double badLikelihood = FishStateUtilities.normalPDF(
                        badAverage,standardDeviation*evidenceStrength).apply(observation.getValue());
                double badPosterior = badPrior*badLikelihood;
                assert  badPosterior >=0;
                assert  Double.isFinite(badPosterior);

                if(badPosterior + goodPosterior == 0) {
                    //if it's many standard deviations away then just default to one or the other
                    if (observation.getValue() > goodAverage)
                        probability.setValue(1d);
                    else if (observation.getValue() < badAverage)
                        probability.setValue(0d);
                    else
                        probability.setValue(.5d); //if you are here that's some very poor averages/std you got
                }
                else
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

    /**
     * Transforms the parameters used (and that can be changed) into a double[] array so that it can be inspected
     * from the outside without knowing the inner workings of the regression
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        return new double[]{
            distancePenalty.getBandwidth(),
            badAverage,
            goodAverage,
            standardDeviation
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

        assert parameterArray.length == 4;
        distancePenalty.setBandwidth(parameterArray[0]);
        badAverage = parameterArray[1];
        goodAverage = parameterArray[2];
        standardDeviation = parameterArray[3];
    }
}
