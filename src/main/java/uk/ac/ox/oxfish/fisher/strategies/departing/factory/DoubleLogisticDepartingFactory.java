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
public class DoubleLogisticDepartingFactory implements AlgorithmFactory<CompositeDepartingStrategy>
{


    private DoubleParameter weatherL = new FixedDoubleParameter(1);

    private DoubleParameter weatherK = new FixedDoubleParameter(10);

    private DoubleParameter weatherX0 = new FixedDoubleParameter(1) ;

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

    private DoubleParameter efficiencyX0 = new FixedDoubleParameter(1) ;


    private DoubleParameter cashflowTarget = new FixedDoubleParameter(1000);

    private DoubleParameter cashflowPeriod = new FixedDoubleParameter(30);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public CompositeDepartingStrategy apply(FishState state)
    {

        return new CompositeDepartingStrategy(

                new CashFlowLogisticDepartingStrategy(efficiencyL.apply(state.getRandom()),
                                                      efficiencyK.apply(state.getRandom()),
                                                      efficiencyX0.apply(state.getRandom()),
                                                      cashflowTarget.apply(state.getRandom()),
                                                      cashflowPeriod.apply(state.getRandom()).intValue()
                                                      ),
                new WeatherLogisticDepartingStrategy(
                        weatherL.apply(state.getRandom()),
                        weatherK.apply(state.getRandom()),
                        weatherX0.apply(state.getRandom()),
                        windspeedSensitivity.apply(state.getRandom()),
                        windspeedSensitivity.apply(state.getRandom()),
                        0)



                );



    }


    public DoubleParameter getWeatherL() {
        return weatherL;
    }

    public void setWeatherL(DoubleParameter weatherL) {
        this.weatherL = weatherL;
    }

    public DoubleParameter getWeatherK() {
        return weatherK;
    }

    public void setWeatherK(DoubleParameter weatherK) {
        this.weatherK = weatherK;
    }

    public DoubleParameter getWeatherX0() {
        return weatherX0;
    }

    public void setWeatherX0(DoubleParameter weatherX0) {
        this.weatherX0 = weatherX0;
    }

    public DoubleParameter getWindspeedSensitivity() {
        return windspeedSensitivity;
    }

    public void setWindspeedSensitivity(DoubleParameter windspeedSensitivity) {
        this.windspeedSensitivity = windspeedSensitivity;
    }

    public DoubleParameter getBoatLengthSensitivity() {
        return boatLengthSensitivity;
    }

    public void setBoatLengthSensitivity(DoubleParameter boatLengthSensitivity) {
        this.boatLengthSensitivity = boatLengthSensitivity;
    }

    public DoubleParameter getEfficiencyL() {
        return efficiencyL;
    }

    public void setEfficiencyL(DoubleParameter efficiencyL) {
        this.efficiencyL = efficiencyL;
    }

    public DoubleParameter getEfficiencyK() {
        return efficiencyK;
    }

    public void setEfficiencyK(DoubleParameter efficiencyK) {
        this.efficiencyK = efficiencyK;
    }

    public DoubleParameter getEfficiencyX0() {
        return efficiencyX0;
    }

    public void setEfficiencyX0(DoubleParameter efficiencyX0) {
        this.efficiencyX0 = efficiencyX0;
    }

    public DoubleParameter getCashflowTarget() {
        return cashflowTarget;
    }

    public void setCashflowTarget(DoubleParameter cashflowTarget) {
        this.cashflowTarget = cashflowTarget;
    }

    public DoubleParameter getCashflowPeriod() {
        return cashflowPeriod;
    }

    public void setCashflowPeriod(DoubleParameter cashflowPeriod) {
        this.cashflowPeriod = cashflowPeriod;
    }
}
