package uk.ac.ox.oxfish.fisher.heatmap.regression.basis;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.mutable.MutableDouble;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.MutablePair;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by carrknight on 3/7/17.
 */
public class RBFNetworkRegression implements GeographicalRegression<Double> {


    /**
     * a list of pairs (rather than a map just because the link is so strong)
     *  Basis  to its weight when making predictions!
     */
    private final LinkedList<Pair<RBFBasis,MutableDouble>> network;


    /**
     * functions used to turn an observation into a double[]
     */
    private final ObservationExtractor[] extractors;



    private final double learningRate;


    /**
     * Getter for property 'network'.
     *
     * @return Value for property 'network'.
     */
    public LinkedList<Pair<RBFBasis, MutableDouble>> getNetwork() {
        return network;
    }

    /**
     * Getter for property 'learningRate'.
     *
     * @return Value for property 'learningRate'.
     */
    public double getLearningRate() {
        return learningRate;
    }

    public RBFNetworkRegression(ObservationExtractor[] extractors,
                                int order,
                                double[] min,
                                double[] max,
                                double learningRate,
                                double initialWeight) {


        Preconditions.checkArgument(max.length == min.length);
        Preconditions.checkArgument(max.length == extractors.length);
        this.extractors = extractors;
        this.learningRate = learningRate;
        //check each step
        double[] step = new double[max.length];
        for(int i=0; i< step.length ; i++)
        {
            step[i] = (max[i]-min[i])/ (double)(order-1);
        }

        double initialBandwidth = 2*(Arrays.stream(step).max().getAsDouble()) ;

        this.network = new LinkedList<>();

        //builds combinations without recursion
        //taken mostly from here : http://stackoverflow.com/a/29910788/975904
        int totalDimension =  (int)Math.pow(order, extractors.length);
        for(int i = 0; i< totalDimension; i++)
        {
            double[] indices = new double[min.length];

            //how often we need to reset
            for(int j=0; j< extractors.length; j++)
            {
                int period = (int) Math.pow(order, extractors.length - j - 1);

                int index = i / period % order;
                indices[j] = min[j]+step[j]*index;

            }
            System.out.println(Arrays.toString(indices));
            network.add(new Pair<>(new RBFBasis(initialBandwidth,
                                                Arrays.copyOf(indices,indices.length)),
                                   new MutableDouble(initialWeight)));
        }



    }

    /**
     * extract numerical values for the x and feed that array into
     * the RBF network to predict
     *
     * @param tile tile where we are predicting
     * @param time time at which we are predicting
     * @param fisher fisher making prediction
     * @param model the rest of the model
     * @return a number representing the sum of weighted basis
     */
    @Override
    public double predict(
            SeaTile tile, double time, Fisher fisher, FishState model) {

        return predict(extractObservation(tile, time, fisher, model));
    }


    private double[] extractObservation(SeaTile tile,
                                        double time,
                                        Fisher fisher,
                                        FishState model)
    {
        double[] observation = new double[extractors.length];
        for(int i=0; i<observation.length; i++)
        {
            observation[i] = extractors[i].extract(tile, time, fisher, model);
        }
        return observation;
    }

    /**
     * standard RBF network sum of weighted distances from center
     */
    private double predict(double[] observation)
    {

        double sum = 0;
        for(Pair<RBFBasis,MutableDouble> basis : network)
        {
            sum += basis.getSecond().doubleValue() * basis.getFirst().evaluate(observation);
        }
        return sum;
    }

    /**
     * Do gradient descent on the weights of each basis
     *
     * @param observation
     * @param fisher
     * @param model
     */
    @Override
    public void addObservation(
            GeographicalObservation<Double> observation, Fisher fisher, FishState model)
    {

        //get x and y
        double[] x = extractObservation(observation.getTile(), observation.getTime(),
                                        fisher, model);
        double y = observation.getValue();
        //now get prediction
        double prediction = predict(x);

        //gradient descent!
        double increment = learningRate * (y-prediction);
        for(Pair<RBFBasis,MutableDouble> basis : network)
        {
            //todo you can make this faster by storing the evaluate from the predict call!
            basis.getSecond().setValue(basis.getSecond().doubleValue() + increment * basis.getFirst().evaluate(x));
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
     * Parameters are the bandwidths of each basis
     *
     * @return an array containing all the parameters of the model
     */
    @Override
    public double[] getParametersAsArray() {
        double[] parameters = new double[network.size()];
        for(int i=0; i<parameters.length; i++)
        {
            parameters[i] = network.get(i).getFirst().getBandwidth();
        }
        return parameters;
    }

    /**
     * set each element of the array as a basis' bandwidth
     *
     * @param parameterArray the new parameters for this regresssion
     */
    @Override
    public void setParameters(double[] parameters) {
        for(int i=0; i<parameters.length; i++)
        {
            network.get(i).getFirst().setBandwidth(parameters[i]);
        }
    }

    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }
}
