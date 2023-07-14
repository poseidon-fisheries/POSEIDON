/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.*;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Season;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;

/**
 * Created by carrknight on 4/12/17.
 */
public class FloridaLogisticDepartingFactory implements AlgorithmFactory<DailyLogisticDepartingStrategy> {


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
    public DailyLogisticDepartingStrategy apply(final FishState state) {

        final LinkedHashMap<ObservationExtractor, Double> betas = new LinkedHashMap<>();


        double coefficient = intercept.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(new InterceptExtractor(), coefficient);
        coefficient = spring.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(new SeasonExtractor(Season.SPRING), coefficient);
        coefficient = summer.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(new SeasonExtractor(Season.SUMMER), coefficient);
        coefficient = winter.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(new SeasonExtractor(Season.WINTER), coefficient);
        coefficient = weekend.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(new WeekendExtractor(), coefficient);
        coefficient = windSpeedInKnots.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(new WindSpeedExtractor(), coefficient);

        coefficient = realDieselPrice.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(new GasPriceExtractor(), coefficient);

        coefficient = priceRedGrouper.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(
                new FishPriceExtractor(state.getBiology().getSpeciesByCaseInsensitiveName("RedGrouper")),
                coefficient
            );

        coefficient = priceGagGrouper.applyAsDouble(state.getRandom());
        if (Double.isFinite(coefficient))
            betas.put(
                new FishPriceExtractor(state.getBiology().getSpeciesByCaseInsensitiveName("GagGrouper")),
                coefficient
            );


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
    public void setIntercept(final DoubleParameter intercept) {
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
    public void setSpring(final DoubleParameter spring) {
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
    public void setSummer(final DoubleParameter summer) {
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
    public void setWinter(final DoubleParameter winter) {
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
    public void setWeekend(final DoubleParameter weekend) {
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
    public void setWindSpeedInKnots(final DoubleParameter windSpeedInKnots) {
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
    public void setRealDieselPrice(final DoubleParameter realDieselPrice) {
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
    public void setPriceRedGrouper(final DoubleParameter priceRedGrouper) {
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
    public void setPriceGagGrouper(final DoubleParameter priceGagGrouper) {
        this.priceGagGrouper = priceGagGrouper;
    }


}
