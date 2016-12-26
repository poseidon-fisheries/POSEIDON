package uk.ac.ox.oxfish.experiments.burlapspike;

import burlap.mdp.core.state.State;
import burlap.mdp.core.state.StateUtilities;
import burlap.mdp.core.state.UnknownKeyException;
import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.Iterator;
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

    private double averageYearlyLanding;

    private double averageYearlyEfforts;

    private double cpue;

    private double yearlyCpue;

    private double biomass;

    public static final String GAS_PRICE = "gasPrice";
    public static final String LANDINGS = "landings";
    public static final String MONTHS_LEFT = "monthsLeft";
    public static final String CUMULATIVE_EFFORT = "cumulativeEffort";
    public static final String AVERAGE_DISTANCE_TO_PORT = "averageDistanceToPort";
    public static final String DAY_OF_THE_YEAR = "dayOfTheYear";
    public static final String AVERAGE_YEARLY_LANDINGS = "averageYearlyLanding";
    public static final String AVERAGE_YEARLY_EFFORTS = "averageYearlyEfforts";
    public static final String CPUE = "cpue";
    public static final String YEARLY_CPUE = "yearlyCPUE";
    public static final String BIOMASS = "biomass";
    static private ArrayList<Object> names = Lists.newArrayList(GAS_PRICE, LANDINGS, MONTHS_LEFT,
                                                                CUMULATIVE_EFFORT, AVERAGE_DISTANCE_TO_PORT,
                                                                DAY_OF_THE_YEAR,AVERAGE_YEARLY_LANDINGS,
                                                                AVERAGE_YEARLY_EFFORTS,CPUE,YEARLY_CPUE, BIOMASS     ) ;


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
        if(variableKey.equals(AVERAGE_YEARLY_LANDINGS))
            return averageYearlyLanding;
        if(variableKey.equals(AVERAGE_YEARLY_EFFORTS))
            return averageYearlyEfforts;
        if(variableKey.equals(CPUE))
            return cpue;
        if(variableKey.equals(YEARLY_CPUE))
            return yearlyCpue;
        if(variableKey.equals(BIOMASS))
            return biomass;
        throw new UnknownKeyException(variableKey);
    }




    public ShodanStateOil() {
    }


    public ShodanStateOil(
            double gasPrice, double landings, int monthsLeft, double cumulativeEffort, double averageDistanceToPort,
            int dayOfTheYear, double averageYearlyLanding, double averageYearlyEfforts, double cpue, double yearlyCpue,
            double biomass) {
        this.gasPrice = gasPrice;
        this.landings = landings;
        this.monthsLeft = monthsLeft;
        this.cumulativeEffort = cumulativeEffort;
        this.averageDistanceToPort = averageDistanceToPort;
        this.dayOfTheYear = dayOfTheYear;
        this.averageYearlyLanding = averageYearlyLanding;
        this.averageYearlyEfforts = averageYearlyEfforts;
        this.cpue = cpue;
        this.yearlyCpue = yearlyCpue;
        this.biomass = biomass;
    }

    static ShodanStateOil fromState(FishState state)
    {

        //initially it's all 0
        if(state.getDay()<29)
            return new ShodanStateOil(state.getPorts().iterator().next().getGasPricePerLiter(),
                                      0,
                                      (ShodanEnvironment.YEARS_PER_EPISODE*365)/30+1,
                                      0d,
                                      0d,
                                      0,
                                      0d,0d,0d,0d,
                                      state.getTotalBiomass(state.getBiology().getSpecie(0)));


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
        for(int i=0; i<30; i++) {
            Double dayDistance = distance.next();
            totalDistance += Double.isFinite(dayDistance) ? dayDistance : 0 ;
        }
        totalDistance/=30;
        //cumulative effort
        double totalEffort=0;
        Iterator<Double> effort = state.getDailyDataSet().getColumn(
                "Total Effort").descendingIterator();
        while (effort.hasNext())
            totalEffort += effort.next();

        //if you ran the model less than 365 days then the average will be over whatever you currently did
        int yearWindow = Math.min(365,state.getDailyDataSet().getColumn("Species 0 Landings").size());
        assert yearWindow > 0;
        landings = state.getDailyDataSet().getColumn(
                "Species 0 Landings").descendingIterator();
        double averageLandings = 0;
        for(int i=0; i<yearWindow; i++)
            averageLandings += landings.next();
        averageLandings /= yearWindow;

        //average effort
        double averageEffort=0;
        double monthlyEffort = 0;
        effort = state.getDailyDataSet().getColumn(
                "Total Effort").descendingIterator();
        for(int i=0; i<yearWindow; i++) {
            Double next = effort.next();
            averageEffort += next;
            if(i<30)
                monthlyEffort += next;
        }
        averageEffort /= yearWindow;


        double cpue = monthlyLandings/monthlyEffort;
        double yearlyCPUE = averageLandings/averageEffort;


        return new ShodanStateOil(state.getPorts().iterator().next().getGasPricePerLiter(),
                                  monthlyLandings,
                                  (int)(Math.round((ShodanEnvironment.YEARS_PER_EPISODE*365-state.getDay())/30d))+1,
                                  totalEffort,
                                  totalDistance,
                                  state.getDayOfTheYear(),
                                  averageLandings,
                                  averageEffort,cpue,yearlyCPUE,
                                  state.getTotalBiomass(state.getBiology().getSpecie(0)));


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
     * Returns a copy of this state suitable for creating state transitions. This copy may be a shallow copy
     * or deep copy and is domain specific. The State implementation may indicate its copy level with the
     * {@link DeepCopyState} or {@link ShallowCopyState} annotations. If it is a shallow copy, you should not *directly*
     * modify any fields of a copied state without copying the fields first, or it could contaminate the state from
     * which the copy was made. Alternatively, use the {@link MutableState#set(Object, Object)} method to modify
     * {@link ShallowCopyState} copied states,
     * which for {@link ShallowCopyState} instances should perform a safe copy-on-write operation.
     *
     * @return a copy of this state.
     */
    @Override
    public State copy() {
        return new ShodanStateOil(gasPrice,landings,monthsLeft,cumulativeEffort,averageDistanceToPort,dayOfTheYear,
                                  averageYearlyLanding,averageYearlyEfforts,cpue,yearlyCpue,biomass);
    }

    /**




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
     * Setter for property 'averageDistanceToPort'.
     *
     * @param averageDistanceToPort Value to set for property 'averageDistanceToPort'.
     */
    public void setAverageDistanceToPort(double averageDistanceToPort) {
        this.averageDistanceToPort = averageDistanceToPort;
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


    /**
     * Getter for property 'averageYearlyLanding'.
     *
     * @return Value for property 'averageYearlyLanding'.
     */
    public double getAverageYearlyLanding() {
        return averageYearlyLanding;
    }

    /**
     * Setter for property 'averageYearlyLanding'.
     *
     * @param averageYearlyLanding Value to set for property 'averageYearlyLanding'.
     */
    public void setAverageYearlyLanding(double averageYearlyLanding) {
        this.averageYearlyLanding = averageYearlyLanding;
    }

    /**
     * Getter for property 'averageYearlyEfforts'.
     *
     * @return Value for property 'averageYearlyEfforts'.
     */
    public double getAverageYearlyEfforts() {
        return averageYearlyEfforts;
    }

    /**
     * Setter for property 'averageYearlyEfforts'.
     *
     * @param averageYearlyEfforts Value to set for property 'averageYearlyEfforts'.
     */
    public void setAverageYearlyEfforts(double averageYearlyEfforts) {
        this.averageYearlyEfforts = averageYearlyEfforts;
    }

    /**
     * Getter for property 'cpue'.
     *
     * @return Value for property 'cpue'.
     */
    public double getCpue() {
        return cpue;
    }

    /**
     * Setter for property 'cpue'.
     *
     * @param cpue Value to set for property 'cpue'.
     */
    public void setCpue(double cpue) {
        this.cpue = cpue;
    }

    /**
     * Getter for property 'yearlyCpue'.
     *
     * @return Value for property 'yearlyCpue'.
     */
    public double getYearlyCpue() {
        return yearlyCpue;
    }

    /**
     * Setter for property 'yearlyCpue'.
     *
     * @param yearlyCpue Value to set for property 'yearlyCpue'.
     */
    public void setYearlyCpue(double yearlyCpue) {
        this.yearlyCpue = yearlyCpue;
    }


    /**
     * Getter for property 'biomass'.
     *
     * @return Value for property 'biomass'.
     */
    public double getBiomass() {
        return biomass;
    }

    /**
     * Setter for property 'biomass'.
     *
     * @param biomass Value to set for property 'biomass'.
     */
    public void setBiomass(double biomass) {
        this.biomass = biomass;
    }

    @Override
    public String toString() {
        return StateUtilities.stateToString(this);
    }
}
