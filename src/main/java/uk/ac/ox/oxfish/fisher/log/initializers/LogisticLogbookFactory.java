package uk.ac.ox.oxfish.fisher.log.initializers;


import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.*;
import uk.ac.ox.oxfish.geography.discretization.IdentityDiscretizerFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;

import java.util.ArrayList;
import java.util.function.Supplier;

public class LogisticLogbookFactory
        implements AlgorithmFactory<LogisticLogbookInitializer>
{

    //each flag represents a pre-made extractor

    private boolean dayOfTheYear = false;

    private boolean gasPrice = false;

    private boolean gridX = false;

    private boolean gridY = false;

    private boolean habitat = false;

    private boolean intercept = true;

    private boolean timeOfObservation = false;

    private boolean portDistance = false;

    private boolean simulatedCost = false;

    private boolean simulatedRevenue = false;

    private boolean windSpeed = false;

    private int periodHabit = -1;

    private int periodHabitContinuous = -1;

    /**
     * useful (in fact, needed) if you have multiple logbooks running at once!
     */
    private String identifier = "";


    private AlgorithmFactory<? extends MapDiscretizer> discretization
            = new IdentityDiscretizerFactory();


    private Locker<FishState, MapDiscretization> locker = new Locker<>() ;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public LogisticLogbookInitializer apply(FishState state) {

        ArrayList<ObservationExtractor> extractors = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();

        MapDiscretization discretized =
                locker.presentKey(state,
                                  new Supplier<MapDiscretization>() {
                                      @Override
                                      public MapDiscretization get() {
                                          MapDiscretization toReturn =
                                                  new MapDiscretization(discretization.apply(state));
                                          toReturn.discretize(state.getMap());
                                          return toReturn;
                                      }
                                  });

        if(dayOfTheYear)
        {
            extractors.add(new DayOfTheYearExtractor());
            names.add("day_of_the_year");
        }

        if(gasPrice)
        {
            extractors.add(new GasPriceExtractor());
            names.add("gas_price");
        }


        if(gridX)
        {
            extractors.add(new GridXExtractor());
            names.add("grid_x");
        }
        if(gridY)
        {
            extractors.add(new GridYExtractor());
            names.add("grid_y");
        }

        if(habitat)
        {
            extractors.add(new HabitatExtractor());
            names.add("habitat");
        }

        if(intercept)
        {
            extractors.add(new InterceptExtractor());
            names.add("intercept");
        }


        if(timeOfObservation)
        {
            extractors.add(new ObservationTimeExtractor());
            names.add("observation_time");
        }


        if(portDistance)
        {
            extractors.add(new PortDistanceExtractor());
            names.add("port_distance");
        }

        //todo put max hours out somehwere else

        if(simulatedCost)
        {
            extractors.add(new SimulatedHourlyCostExtractor(5*24));
            names.add("simulated_cost");
        }
        if(simulatedRevenue)
        {
            extractors.add(new SimulatedHourlyRevenueExtractor(5*24));
            names.add("simulated_revenue");
        }
        if(windSpeed)
        {
            extractors.add(new WindSpeedExtractor());
            names.add("wind_speed");
        }

        if(periodHabit>0)
        {
            extractors.add(new PeriodHabitBooleanExtractor(discretized, periodHabit));
            names.add("habit");
            //the logit discretized memory this extractor depends on is produced by
            // LogistiLogbookInitializer

        }

        if(periodHabitContinuous>0)
        {
            extractors.add(new PeriodHabitContinuousExtractor(discretized, periodHabitContinuous));
            names.add("habit_continuous");
            //the logit discretized memory this extractor depends on is produced by
            // LogistiLogbookInitializer

        }


        String[] nameArray =
                names.toArray(new String[names.size()]);
        ObservationExtractor[] observations =
                extractors.toArray(new ObservationExtractor[extractors.size()]);



        return new LogisticLogbookInitializer(
                discretized,
                observations,
                nameArray,
                identifier
        );





    }

    /**
     * Getter for property 'dayOfTheYear'.
     *
     * @return Value for property 'dayOfTheYear'.
     */
    public boolean isDayOfTheYear() {
        return dayOfTheYear;
    }

    /**
     * Setter for property 'dayOfTheYear'.
     *
     * @param dayOfTheYear Value to set for property 'dayOfTheYear'.
     */
    public void setDayOfTheYear(boolean dayOfTheYear) {
        this.dayOfTheYear = dayOfTheYear;
    }

    /**
     * Getter for property 'gasPrice'.
     *
     * @return Value for property 'gasPrice'.
     */
    public boolean isGasPrice() {
        return gasPrice;
    }

    /**
     * Setter for property 'gasPrice'.
     *
     * @param gasPrice Value to set for property 'gasPrice'.
     */
    public void setGasPrice(boolean gasPrice) {
        this.gasPrice = gasPrice;
    }

    /**
     * Getter for property 'gridX'.
     *
     * @return Value for property 'gridX'.
     */
    public boolean isGridX() {
        return gridX;
    }

    /**
     * Setter for property 'gridX'.
     *
     * @param gridX Value to set for property 'gridX'.
     */
    public void setGridX(boolean gridX) {
        this.gridX = gridX;
    }

    /**
     * Getter for property 'gridY'.
     *
     * @return Value for property 'gridY'.
     */
    public boolean isGridY() {
        return gridY;
    }

    /**
     * Setter for property 'gridY'.
     *
     * @param gridY Value to set for property 'gridY'.
     */
    public void setGridY(boolean gridY) {
        this.gridY = gridY;
    }

    /**
     * Getter for property 'habitat'.
     *
     * @return Value for property 'habitat'.
     */
    public boolean isHabitat() {
        return habitat;
    }

    /**
     * Setter for property 'habitat'.
     *
     * @param habitat Value to set for property 'habitat'.
     */
    public void setHabitat(boolean habitat) {
        this.habitat = habitat;
    }

    /**
     * Getter for property 'intercept'.
     *
     * @return Value for property 'intercept'.
     */
    public boolean isIntercept() {
        return intercept;
    }

    /**
     * Setter for property 'intercept'.
     *
     * @param intercept Value to set for property 'intercept'.
     */
    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    /**
     * Getter for property 'timeOfObservation'.
     *
     * @return Value for property 'timeOfObservation'.
     */
    public boolean isTimeOfObservation() {
        return timeOfObservation;
    }

    /**
     * Setter for property 'timeOfObservation'.
     *
     * @param timeOfObservation Value to set for property 'timeOfObservation'.
     */
    public void setTimeOfObservation(boolean timeOfObservation) {
        this.timeOfObservation = timeOfObservation;
    }

    /**
     * Getter for property 'portDistance'.
     *
     * @return Value for property 'portDistance'.
     */
    public boolean isPortDistance() {
        return portDistance;
    }

    /**
     * Setter for property 'portDistance'.
     *
     * @param portDistance Value to set for property 'portDistance'.
     */
    public void setPortDistance(boolean portDistance) {
        this.portDistance = portDistance;
    }

    /**
     * Getter for property 'simulatedCost'.
     *
     * @return Value for property 'simulatedCost'.
     */
    public boolean isSimulatedCost() {
        return simulatedCost;
    }

    /**
     * Setter for property 'simulatedCost'.
     *
     * @param simulatedCost Value to set for property 'simulatedCost'.
     */
    public void setSimulatedCost(boolean simulatedCost) {
        this.simulatedCost = simulatedCost;
    }

    /**
     * Getter for property 'simulatedRevenue'.
     *
     * @return Value for property 'simulatedRevenue'.
     */
    public boolean isSimulatedRevenue() {
        return simulatedRevenue;
    }

    /**
     * Setter for property 'simulatedRevenue'.
     *
     * @param simulatedRevenue Value to set for property 'simulatedRevenue'.
     */
    public void setSimulatedRevenue(boolean simulatedRevenue) {
        this.simulatedRevenue = simulatedRevenue;
    }

    /**
     * Getter for property 'windSpeed'.
     *
     * @return Value for property 'windSpeed'.
     */
    public boolean isWindSpeed() {
        return windSpeed;
    }

    /**
     * Setter for property 'windSpeed'.
     *
     * @param windSpeed Value to set for property 'windSpeed'.
     */
    public void setWindSpeed(boolean windSpeed) {
        this.windSpeed = windSpeed;
    }

    /**
     * Getter for property 'discretization'.
     *
     * @return Value for property 'discretization'.
     */
    public AlgorithmFactory<? extends MapDiscretizer> getDiscretization() {
        return discretization;
    }

    /**
     * Setter for property 'discretization'.
     *
     * @param discretization Value to set for property 'discretization'.
     */
    public void setDiscretization(
            AlgorithmFactory<? extends MapDiscretizer> discretization) {
        this.discretization = discretization;
    }

    /**
     * Getter for property 'periodHabit'.
     *
     * @return Value for property 'periodHabit'.
     */
    public int getPeriodHabit() {
        return periodHabit;
    }

    /**
     * Setter for property 'periodHabit'.
     *
     * @param periodHabit Value to set for property 'periodHabit'.
     */
    public void setPeriodHabit(int periodHabit) {
        this.periodHabit = periodHabit;
    }

    /**
     * Getter for property 'periodHabitContinuous'.
     *
     * @return Value for property 'periodHabitContinuous'.
     */
    public int getPeriodHabitContinuous() {
        return periodHabitContinuous;
    }

    /**
     * Setter for property 'periodHabitContinuous'.
     *
     * @param periodHabitContinuous Value to set for property 'periodHabitContinuous'.
     */
    public void setPeriodHabitContinuous(int periodHabitContinuous) {
        this.periodHabitContinuous = periodHabitContinuous;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


}
