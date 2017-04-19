package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.FishPriceExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.InterceptExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.WeekendExtractor;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.strategies.fishing.LogitReturnStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Implementation of the WFS logit regression
 * Created by carrknight on 4/19/17.
 */
public class FloridaLogitReturnFactory implements AlgorithmFactory<LogitReturnStrategy>{

    //these are the handline parameters by default


    private DoubleParameter intercept = new FixedDoubleParameter(-3.47701);

    private DoubleParameter priceRedGrouper = new FixedDoubleParameter(0.92395);

    private DoubleParameter priceGagGrouper = new FixedDoubleParameter(-.65122);

    private DoubleParameter ratioCatchToFishHold = new FixedDoubleParameter(4.37828);

    private DoubleParameter weekendDummy = new FixedDoubleParameter(-0.24437);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LogitReturnStrategy apply(FishState state) {
        return new LogitReturnStrategy
                (
                new LogisticClassifier
                        (
                        //intercept:
                        new Pair<>(
                                new InterceptExtractor()
                                ,intercept.apply(state.getRandom())),
                        //price red grouper
                        new Pair<>(
                                new FishPriceExtractor(
                                        state.getBiology().getSpecie("RedGrouper")
                                ),
                                priceRedGrouper.apply(state.getRandom())
                        ),
                        //price gag grouper
                        new Pair<>(
                                new FishPriceExtractor(
                                        state.getBiology().getSpecie("GagGrouper")
                                ),
                                priceGagGrouper.apply(state.getRandom())
                        ),
                        //ratio catch to fish hold
                        new Pair<>(
                                new ObservationExtractor() {
                                    @Override
                                    public double extract(
                                            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
                                        return agent.getTotalWeightOfCatchInHold() / agent.getMaximumHold();
                                    }
                                },
                                ratioCatchToFishHold.apply(state.getRandom())


                        ),
                        //weekend dummy
                        new Pair<>(
                                new WeekendExtractor(),
                                weekendDummy.apply(state.getRandom())
                        )
                )


        );
    }

    /**
     * Getter for property 'intercept'.
     *
     * @return Value for property 'intercept'.
     */
    public DoubleParameter getIntercept() {
        return intercept;
    }

    /**
     * Setter for property 'intercept'.
     *
     * @param intercept Value to set for property 'intercept'.
     */
    public void setIntercept(DoubleParameter intercept) {
        this.intercept = intercept;
    }

    /**
     * Getter for property 'priceRedGrouper'.
     *
     * @return Value for property 'priceRedGrouper'.
     */
    public DoubleParameter getPriceRedGrouper() {
        return priceRedGrouper;
    }

    /**
     * Setter for property 'priceRedGrouper'.
     *
     * @param priceRedGrouper Value to set for property 'priceRedGrouper'.
     */
    public void setPriceRedGrouper(DoubleParameter priceRedGrouper) {
        this.priceRedGrouper = priceRedGrouper;
    }

    /**
     * Getter for property 'priceGagGrouper'.
     *
     * @return Value for property 'priceGagGrouper'.
     */
    public DoubleParameter getPriceGagGrouper() {
        return priceGagGrouper;
    }

    /**
     * Setter for property 'priceGagGrouper'.
     *
     * @param priceGagGrouper Value to set for property 'priceGagGrouper'.
     */
    public void setPriceGagGrouper(DoubleParameter priceGagGrouper) {
        this.priceGagGrouper = priceGagGrouper;
    }

    /**
     * Getter for property 'ratioCatchToFishHold'.
     *
     * @return Value for property 'ratioCatchToFishHold'.
     */
    public DoubleParameter getRatioCatchToFishHold() {
        return ratioCatchToFishHold;
    }

    /**
     * Setter for property 'ratioCatchToFishHold'.
     *
     * @param ratioCatchToFishHold Value to set for property 'ratioCatchToFishHold'.
     */
    public void setRatioCatchToFishHold(DoubleParameter ratioCatchToFishHold) {
        this.ratioCatchToFishHold = ratioCatchToFishHold;
    }

    /**
     * Getter for property 'weekendDummy'.
     *
     * @return Value for property 'weekendDummy'.
     */
    public DoubleParameter getWeekendDummy() {
        return weekendDummy;
    }

    /**
     * Setter for property 'weekendDummy'.
     *
     * @param weekendDummy Value to set for property 'weekendDummy'.
     */
    public void setWeekendDummy(DoubleParameter weekendDummy) {
        this.weekendDummy = weekendDummy;
    }
}
