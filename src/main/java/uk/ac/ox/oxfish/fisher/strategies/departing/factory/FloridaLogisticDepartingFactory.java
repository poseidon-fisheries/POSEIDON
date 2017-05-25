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

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by carrknight on 4/12/17.
 */
public class FloridaLogisticDepartingFactory implements AlgorithmFactory<DailyLogisticDepartingStrategy>
{


    private DoubleParameter intercept = new FixedDoubleParameter(-2.075184);
    private DoubleParameter spring = new FixedDoubleParameter(0.725026);
    private DoubleParameter summer = new FixedDoubleParameter(0.624472);
    private DoubleParameter winter = new FixedDoubleParameter(0.266862);
    private DoubleParameter weekend = new FixedDoubleParameter(-0.097619);
    private DoubleParameter windSpeedInKnots = new FixedDoubleParameter(-0.046672);
    //this is in $ / gallon; we convert it in the OSMOSE WFS
    private DoubleParameter realDieselPrice = new FixedDoubleParameter(-0.515073);
    //this is in $/lbs; we convert it in the OSMOSE WFS
    private DoubleParameter priceRedGrouper = new FixedDoubleParameter(-0.3604);
    //this is in $/lbs; we convert it in the OSMOSE WFS
    private DoubleParameter priceGagGrouper = new FixedDoubleParameter(0.649616);



    /**
     * creates the correct logit
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DailyLogisticDepartingStrategy apply(FishState state) {

        LinkedHashMap<ObservationExtractor,Double> betas = new LinkedHashMap();



        Double coefficient = intercept.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put(new InterceptExtractor(), coefficient);
        coefficient = spring.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put(new SeasonExtractor(Season.SPRING), coefficient);
        coefficient = summer.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put(new SeasonExtractor(Season.SUMMER), coefficient);
        coefficient = winter.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put(new SeasonExtractor(Season.WINTER), coefficient);
        coefficient = weekend.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put(new WeekendExtractor(), coefficient);
        coefficient = windSpeedInKnots.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put(new WindSpeedExtractor(), coefficient);

        coefficient = realDieselPrice.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put(new GasPriceExtractor(), coefficient);

        coefficient = priceRedGrouper.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put( new FishPriceExtractor(state.getBiology().getSpecie("RedGrouper")), coefficient);

        coefficient = priceGagGrouper.apply(state.getRandom());
        if(Double.isFinite(coefficient))
            betas.put( new FishPriceExtractor(state.getBiology().getSpecie("GagGrouper")), coefficient);




        return new DailyLogisticDepartingStrategy(
                new LogisticClassifier(
                        betas
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
