package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.Season;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 4/12/17.
 */
public class HandlineFloridaLogisticDepartingFactory implements AlgorithmFactory<DailyLogisticDepartingStrategy>
{


    private DoubleParameter intercept = new FixedDoubleParameter(-2.075184);
    private DoubleParameter spring = new FixedDoubleParameter(0.725026);
    private DoubleParameter summer = new FixedDoubleParameter(0.624472);
    private DoubleParameter winter = new FixedDoubleParameter(0.266862);
    private DoubleParameter weekend = new FixedDoubleParameter(-0.097619);
    private DoubleParameter windSpeedInKnots = new FixedDoubleParameter(-0.046672);
    private DoubleParameter realDieselPrice = new FixedDoubleParameter(-0.515073);
    private DoubleParameter priceRedGrouper = new FixedDoubleParameter(-0.3604);
    private DoubleParameter priceGagGrouper = new FixedDoubleParameter(0.649616);



    /**
     * creates the correct logit
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DailyLogisticDepartingStrategy apply(FishState state) {

        return new DailyLogisticDepartingStrategy(
                new LogisticClassifier(
                        //intercept:
                        new Pair<>(
                                new InterceptExtractor()
                                ,intercept.apply(state.getRandom())),
                        //spring
                        new Pair<>(
                                new SeasonExtractor(Season.SPRING),
                                spring.apply(state.getRandom())
                        ),
                        //summer
                        new Pair<>(
                                new SeasonExtractor(Season.SUMMER),
                                summer.apply(state.getRandom())
                        ),
                        //winter
                        new Pair<>(
                                new SeasonExtractor(Season.SUMMER),
                                winter.apply(state.getRandom())
                        ),
                        //weekend
                        new Pair<>(
                                new WeekendExtractor(),
                                weekend.apply(state.getRandom())

                        ),
                        new Pair<>(new WindSpeedExtractor(),
                                   windSpeedInKnots.apply(state.getRandom())),

                        //realDieselPrice
                        new Pair<>(
                                new GasPriceExtractor(),
                                realDieselPrice.apply(state.getRandom())

                        ),
                        //priceRedGrouper
                        new Pair<>(
                                new FishPriceExtractor(
                                        state.getBiology().getSpecie("RedGrouper")
                                ),
                                priceRedGrouper.apply(state.getRandom())
                        ),
                        //priceGagGrouper
                        new Pair<>(
                                new FishPriceExtractor(
                                        state.getBiology().getSpecie("GagGrouper")
                                ),
                                priceGagGrouper.apply(state.getRandom())
                        )
                ));


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
     * Getter for property 'spring'.
     *
     * @return Value for property 'spring'.
     */
    public DoubleParameter getSpring() {
        return spring;
    }

    /**
     * Setter for property 'spring'.
     *
     * @param spring Value to set for property 'spring'.
     */
    public void setSpring(DoubleParameter spring) {
        this.spring = spring;
    }

    /**
     * Getter for property 'summer'.
     *
     * @return Value for property 'summer'.
     */
    public DoubleParameter getSummer() {
        return summer;
    }

    /**
     * Setter for property 'summer'.
     *
     * @param summer Value to set for property 'summer'.
     */
    public void setSummer(DoubleParameter summer) {
        this.summer = summer;
    }

    /**
     * Getter for property 'winter'.
     *
     * @return Value for property 'winter'.
     */
    public DoubleParameter getWinter() {
        return winter;
    }

    /**
     * Setter for property 'winter'.
     *
     * @param winter Value to set for property 'winter'.
     */
    public void setWinter(DoubleParameter winter) {
        this.winter = winter;
    }

    /**
     * Getter for property 'weekend'.
     *
     * @return Value for property 'weekend'.
     */
    public DoubleParameter getWeekend() {
        return weekend;
    }

    /**
     * Setter for property 'weekend'.
     *
     * @param weekend Value to set for property 'weekend'.
     */
    public void setWeekend(DoubleParameter weekend) {
        this.weekend = weekend;
    }

    /**
     * Getter for property 'windSpeedInKnots'.
     *
     * @return Value for property 'windSpeedInKnots'.
     */
    public DoubleParameter getWindSpeedInKnots() {
        return windSpeedInKnots;
    }

    /**
     * Setter for property 'windSpeedInKnots'.
     *
     * @param windSpeedInKnots Value to set for property 'windSpeedInKnots'.
     */
    public void setWindSpeedInKnots(DoubleParameter windSpeedInKnots) {
        this.windSpeedInKnots = windSpeedInKnots;
    }

    /**
     * Getter for property 'realDieselPrice'.
     *
     * @return Value for property 'realDieselPrice'.
     */
    public DoubleParameter getRealDieselPrice() {
        return realDieselPrice;
    }

    /**
     * Setter for property 'realDieselPrice'.
     *
     * @param realDieselPrice Value to set for property 'realDieselPrice'.
     */
    public void setRealDieselPrice(DoubleParameter realDieselPrice) {
        this.realDieselPrice = realDieselPrice;
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



}
