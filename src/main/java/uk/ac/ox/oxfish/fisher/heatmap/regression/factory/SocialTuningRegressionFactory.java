package uk.ac.ox.oxfish.fisher.heatmap.regression.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.SocialTuningRegression;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.fisher.strategies.destination.HeatmapDestinationStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;
import uk.ac.ox.oxfish.utility.adaptation.probability.factory.FixedProbabilityFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.*;

/**
 * Created by carrknight on 8/26/16.
 */
public class SocialTuningRegressionFactory implements AlgorithmFactory<SocialTuningRegression<Double>> {



    private AlgorithmFactory<? extends GeographicalRegression> nested = new CompleteNearestNeighborRegressionFactory();
    {
        ((CompleteNearestNeighborRegressionFactory) nested).setDistanceFromPortBandwidth(new UniformDoubleParameter(0.1,1000));
        ((CompleteNearestNeighborRegressionFactory) nested).setHabitatBandwidth(new UniformDoubleParameter(0.1,1000));
        ((CompleteNearestNeighborRegressionFactory) nested).setTimeBandwidth(new UniformDoubleParameter(0.1,1000));
        ((CompleteNearestNeighborRegressionFactory) nested).setxBandwidth(new UniformDoubleParameter(0.1,1000));
        ((CompleteNearestNeighborRegressionFactory) nested).setyBandwidth(new UniformDoubleParameter(0.1,1000));
    }




    private boolean yearly = false;

    private AlgorithmFactory<? extends AdaptationProbability> probability = new FixedProbabilityFactory(.2,1);

    /**
     * mantains a (weak) set of fish states so that we initialize our data gatherers only once!
     */
    private final Set<FishState> weakStateMap = Collections.newSetFromMap(new WeakHashMap<>());



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public SocialTuningRegression apply(FishState state) {



        GeographicalRegression delegate = this.nested.apply(state);
        DoubleParameter[] zeros = new DoubleParameter[delegate.getParametersAsArray().length];
        Arrays.fill(zeros,new FixedDoubleParameter(0));


        //add data gathering if necessary
        if(!weakStateMap.contains(state))
        {
            weakStateMap.add(state);
            addDataGatherers(state,zeros.length);
            assert weakStateMap.contains(state);
        }

        return new SocialTuningRegression(
                delegate,
                probability.apply(state),
                yearly);

    }

    private void addDataGatherers(FishState state, int length) {


        for(int i=0; i<length; i++)
        {

            //first add data gatherers
            int finalI = i;
            state.
                    getYearlyDataSet().
                    registerGatherer("Average Heatmap Parameter " + i,
                                     model -> {
                                         double size =model.getFishers().size();
                                         if(size == 0)
                                             return Double.NaN;
                                         else
                                         {
                                             double total = 0;
                                             for(Fisher fisher1 : state.getFishers())
                                             {
                                                 total+=
                                                         ((HeatmapDestinationStrategy) fisher1.getDestinationStrategy()).
                                                                 getHeatmap().getParametersAsArray()[finalI];
                                             }
                                             return total/size;
                                         }
                                     }, Double.NaN);
        }

    }


    /**
     * Getter for property 'nested'.
     *
     * @return Value for property 'nested'.
     */
    public AlgorithmFactory<? extends GeographicalRegression> getNested() {
        return nested;
    }

    /**
     * Setter for property 'nested'.
     *
     * @param nested Value to set for property 'nested'.
     */
    public void setNested(
            AlgorithmFactory<? extends GeographicalRegression> nested) {
        this.nested = nested;
    }

    /**
     * Getter for property 'yearly'.
     *
     * @return Value for property 'yearly'.
     */
    public boolean isYearly() {
        return yearly;
    }

    /**
     * Setter for property 'yearly'.
     *
     * @param yearly Value to set for property 'yearly'.
     */
    public void setYearly(boolean yearly) {
        this.yearly = yearly;
    }

    /**
     * Getter for property 'probability'.
     *
     * @return Value for property 'probability'.
     */
    public AlgorithmFactory<? extends AdaptationProbability> getProbability() {
        return probability;
    }

    /**
     * Setter for property 'probability'.
     *
     * @param probability Value to set for property 'probability'.
     */
    public void setProbability(
            AlgorithmFactory<? extends AdaptationProbability> probability) {
        this.probability = probability;
    }
}
