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

import uk.ac.ox.oxfish.fisher.strategies.departing.CashFlowLogisticDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.CompositeDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.departing.WeatherLogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * A fisher must find it both profitable and safe to go out.
 * Created by carrknight on 9/11/15.
 */
public class DoubleLogisticDepartingFactory implements AlgorithmFactory<CompositeDepartingStrategy> {


    private DoubleParameter weatherL = new FixedDoubleParameter(1);

    private DoubleParameter weatherK = new FixedDoubleParameter(10);

    private DoubleParameter weatherX0 = new FixedDoubleParameter(1);

    /**
     * how much windspeed affects "environment harshness"
     */
    private DoubleParameter windspeedSensitivity = new FixedDoubleParameter(.03);

    /**
     * how much boat length affects "environment harshness"
     */
    private DoubleParameter boatLengthSensitivity = new FixedDoubleParameter(-.02);


    private DoubleParameter efficiencyL = new FixedDoubleParameter(1);

    private DoubleParameter efficiencyK = new FixedDoubleParameter(20);

    private DoubleParameter efficiencyX0 = new FixedDoubleParameter(1);


    private DoubleParameter cashflowTarget = new FixedDoubleParameter(1000);

    private DoubleParameter cashflowPeriod = new FixedDoubleParameter(30);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CompositeDepartingStrategy apply(final FishState state) {

        return new CompositeDepartingStrategy(

            new CashFlowLogisticDepartingStrategy(
                efficiencyL.applyAsDouble(state.getRandom()),
                efficiencyK.applyAsDouble(state.getRandom()),
                efficiencyX0.applyAsDouble(state.getRandom()),
                cashflowTarget.applyAsDouble(state.getRandom()),
                (int) cashflowPeriod.applyAsDouble(state.getRandom())
            ),
            new WeatherLogisticDepartingStrategy(
                weatherL.applyAsDouble(state.getRandom()),
                weatherK.applyAsDouble(state.getRandom()),
                weatherX0.applyAsDouble(state.getRandom()),
                windspeedSensitivity.applyAsDouble(state.getRandom()),
                windspeedSensitivity.applyAsDouble(state.getRandom()),
                0
            )


        );


    }


    public DoubleParameter getWeatherL() {
        return weatherL;
    }

    public void setWeatherL(final DoubleParameter weatherL) {
        this.weatherL = weatherL;
    }

    public DoubleParameter getWeatherK() {
        return weatherK;
    }

    public void setWeatherK(final DoubleParameter weatherK) {
        this.weatherK = weatherK;
    }

    public DoubleParameter getWeatherX0() {
        return weatherX0;
    }

    public void setWeatherX0(final DoubleParameter weatherX0) {
        this.weatherX0 = weatherX0;
    }

    public DoubleParameter getWindspeedSensitivity() {
        return windspeedSensitivity;
    }

    public void setWindspeedSensitivity(final DoubleParameter windspeedSensitivity) {
        this.windspeedSensitivity = windspeedSensitivity;
    }

    public DoubleParameter getBoatLengthSensitivity() {
        return boatLengthSensitivity;
    }

    public void setBoatLengthSensitivity(final DoubleParameter boatLengthSensitivity) {
        this.boatLengthSensitivity = boatLengthSensitivity;
    }

    public DoubleParameter getEfficiencyL() {
        return efficiencyL;
    }

    public void setEfficiencyL(final DoubleParameter efficiencyL) {
        this.efficiencyL = efficiencyL;
    }

    public DoubleParameter getEfficiencyK() {
        return efficiencyK;
    }

    public void setEfficiencyK(final DoubleParameter efficiencyK) {
        this.efficiencyK = efficiencyK;
    }

    public DoubleParameter getEfficiencyX0() {
        return efficiencyX0;
    }

    public void setEfficiencyX0(final DoubleParameter efficiencyX0) {
        this.efficiencyX0 = efficiencyX0;
    }

    public DoubleParameter getCashflowTarget() {
        return cashflowTarget;
    }

    public void setCashflowTarget(final DoubleParameter cashflowTarget) {
        this.cashflowTarget = cashflowTarget;
    }

    public DoubleParameter getCashflowPeriod() {
        return cashflowPeriod;
    }

    public void setCashflowPeriod(final DoubleParameter cashflowPeriod) {
        this.cashflowPeriod = cashflowPeriod;
    }
}
