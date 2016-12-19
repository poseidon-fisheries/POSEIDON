package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Shodan state where you know the gas price
 * Created by carrknight on 12/19/16.
 */
public class ShodanStateOil implements State {


    private double gasPrice;

    private double landings;

    private int monthsLeft;

    private double cumulativeEffort;

    private double averageDistanceToPort;

    private int dayOfTheYear;


    public static final String GAS_PRICE = "gasPrice";
    public static final String LANDINGS = "landings";
    public static final String MONTHS_LEFT = "monthsLeft";
    public static final String CUMULATIVE_EFFORT = "cumulativeEffort";
    public static final String AVERAGE_DISTANCE_TO_PORT = "averageDistanceToPort";
    public static final String DAY_OF_THE_YEAR = "dayOfTheYear";
    static private ArrayList<Object> names = Lists.newArrayList(GAS_PRICE, LANDINGS, MONTHS_LEFT,
                                                                CUMULATIVE_EFFORT, AVERAGE_DISTANCE_TO_PORT,
                                                                DAY_OF_THE_YEAR) ;


    /**
     * Returns the value for the given variable key. Changes to the returned value are not guaranteed to modify
     * the state.
     *
     * @param variableKey the variable key
     * @return the value for the given variable key
     */
    @Override
    public Object get(Object variableKey) {
        if(variableKey.equals(GAS_PRICE))
            return gasPrice;
        if(variableKey.equals(LANDINGS))
            return landings;
        if(variableKey.equals(MONTHS_LEFT))
            return monthsLeft;
        if(variableKey.equals(CUMULATIVE_EFFORT))
            return cumulativeEffort;
        if(variableKey.equals(AVERAGE_DISTANCE_TO_PORT))
            return averageDistanceToPort;
        if(variableKey.equals(DAY_OF_THE_YEAR))
            return dayOfTheYear;
        throw new UnknownKeyException(variableKey);
    }




    public ShodanStateOil() {
    }


    public ShodanStateOil(
            double gasPrice, double landings, int monthsLeft, double cumulativeEffort, double averageDistanceToPort,
            int dayOfTheYear) {
        this.gasPrice = gasPrice;
        this.landings = landings;
        this.monthsLeft = monthsLeft;
        this.cumulativeEffort = cumulativeEffort;
        this.averageDistanceToPort = averageDistanceToPort;
    }

    static ShodanStateOil fromState(FishState state)
    {

        //initially it's all 0
        if(state.getDay()<30)
            return new ShodanStateOil(state.getPorts().iterator().next().getGasPricePerLiter(),
                               0,
                               ShodanEnvironment.YEARS_PER_EPISODE*12,
                               0d,
                               0d,
                               0);

        //landings
        double monthlyLandings = 0;
        Iterator<Double> landings = state.getDailyDataSet().getColumn(
                "Species 0 Landings").descendingIterator();
        for(int i=0; i<30; i++)
            monthlyLandings += landings.next();

        //distance from port
        double totalDistance=0;
        Iterator<Double> distance = state.getDailyDataSet().getColumn(
                "Average Distance From Port").descendingIterator();
        for(int i=0; i<30; i++)
            totalDistance += distance.next();
        //cpue
        double totalEffort=0;
        Iterator<Double> effort = state.getDailyDataSet().getColumn(
                "Total Effort").descendingIterator();
        for(int i=0; i<30; i++)
            totalEffort += effort.next();


        return new ShodanStateOil(state.getPorts().iterator().next().getGasPricePerLiter(),
                                  monthlyLandings,
                                  (int)(ShodanEnvironment.YEARS_PER_EPISODE*1-state.getDay()/30),
                                  totalEffort,
                                  totalDistance,
                                  state.getDayOfTheYear());


    }

    /**
     * Returns the list of state variable keys.
     *
     * @return the list of state variable keys.
     */
    @Override
    public List<Object> variableKeys() {
        return names;
    }

    /**

     * @return a copy of this state.
     */
    @Override
    public State copy() {
        return new ShodanStateOil(gasPrice,landings,monthsLeft,cumulativeEffort,averageDistanceToPort,dayOfTheYear);
    }

    /**
     * Getter for property 'gasPrice'.
     *
     * @return Value for property 'gasPrice'.
     */
    public double getGasPrice() {
        return gasPrice;
    }

    /**
     * Setter for property 'gasPrice'.
     *
     * @param gasPrice Value to set for property 'gasPrice'.
     */
    public void setGasPrice(double gasPrice) {
        this.gasPrice = gasPrice;
    }

    /**
     * Getter for property 'landings'.
     *
     * @return Value for property 'landings'.
     */
    public double getLandings() {
        return landings;
    }

    /**
     * Setter for property 'landings'.
     *
     * @param landings Value to set for property 'landings'.
     */
    public void setLandings(double landings) {
        this.landings = landings;
    }

    /**
     * Getter for property 'monthsLeft'.
     *
     * @return Value for property 'monthsLeft'.
     */
    public int getMonthsLeft() {
        return monthsLeft;
    }

    /**
     * Setter for property 'monthsLeft'.
     *
     * @param monthsLeft Value to set for property 'monthsLeft'.
     */
    public void setMonthsLeft(int monthsLeft) {
        this.monthsLeft = monthsLeft;
    }

    /**
     * Getter for property 'cumulativeEffort'.
     *
     * @return Value for property 'cumulativeEffort'.
     */
    public double getCumulativeEffort() {
        return cumulativeEffort;
    }

    /**
     * Setter for property 'cumulativeEffort'.
     *
     * @param cumulativeEffort Value to set for property 'cumulativeEffort'.
     */
    public void setCumulativeEffort(double cumulativeEffort) {
        this.cumulativeEffort = cumulativeEffort;
    }

    /**
     * Getter for property 'averageDistanceToPort'.
     *
     * @return Value for property 'averageDistanceToPort'.
     */
    public double getAverageDistanceToPort() {
        return averageDistanceToPort;
    }

    /**
     * Getter for property 'dayOfTheYear'.
     *
     * @return Value for property 'dayOfTheYear'.
     */
    public int getDayOfTheYear() {
        return dayOfTheYear;
    }

    /**
     * Setter for property 'dayOfTheYear'.
     *
     * @param dayOfTheYear Value to set for property 'dayOfTheYear'.
     */
    public void setDayOfTheYear(int dayOfTheYear) {
        this.dayOfTheYear = dayOfTheYear;
    }

    @Override
    public String toString() {
        return StateUtilities.stateToString(this);
    }
}
