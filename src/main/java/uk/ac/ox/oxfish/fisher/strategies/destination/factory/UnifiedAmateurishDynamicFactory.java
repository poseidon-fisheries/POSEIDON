package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.AmateurishDynamicStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/**
 *
 * Created by carrknight on 10/13/16.
 */
public class UnifiedAmateurishDynamicFactory implements AlgorithmFactory<AmateurishDynamicStrategy>
{


    private DoubleParameter discountRate = new FixedDoubleParameter(0);

    private DoubleParameter noiseRate = new FixedDoubleParameter(.02);

    private DoubleParameter learningRate = new FixedDoubleParameter(.00025);

    private DoubleParameter explorationSize = new FixedDoubleParameter(5);


    private WeakHashMap<FishState,AmateurishDynamicStrategy> instances = new WeakHashMap<>();


    private UnifiedAmateurishDynamicFactory(){};

    private static UnifiedAmateurishDynamicFactory instance = new UnifiedAmateurishDynamicFactory();

    public static UnifiedAmateurishDynamicFactory getInstance(){
        return instance;
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public AmateurishDynamicStrategy apply(FishState state) {

        AmateurishDynamicStrategy strategy = instances.get(state);
        if(strategy == null)
        {
            strategy = new AmateurishDynamicStrategy(
                    learningRate.apply(state.getRandom()),
                    noiseRate.apply(state.getRandom()),
                    state.getMap(), state.getRandom(),
                    explorationSize.apply(state.getRandom()).intValue(),
                    discountRate.apply(state.getRandom()),
                    //intercept

                    new Sensor<Fisher, Double>() {
                        @Override
                        public Double scan(Fisher system) {
                            return 1d;
                        }
                    },

                    //previous profits

                    new Sensor<Fisher, Double>() {
                        @Override
                        public Double scan(Fisher fisher) {
                            return fisher.getLastFinishedTrip() == null ? 0d : fisher.getLastFinishedTrip().getProfitPerHour(
                                    true);
                        }
                    },
                    //  distance from average

                    new Sensor<Fisher, Double>() {
                        @Override
                        public Double scan(Fisher fisher) {

                            double average = state.getLatestDailyObservation(
                                    FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                            //get last profits (to compare)
                            double lastProfits = fisher.getLastFinishedTrip() == null ? Double.NaN : fisher.getLastFinishedTrip().getProfitPerHour(
                                    true);
                            double distance = average - lastProfits;
                            return Double.isFinite(distance) ? distance : 0d; // average is NaN at the beginning


                        }
                    },
                    //  distance from best friend
                    new Sensor<Fisher, Double>() {
                        @Override
                        public Double scan(Fisher fisher) {
                            double best = fisher.getDirectedFriends().stream().filter(
                                    new Predicate<Fisher>() {
                                        @Override
                                        public boolean test(Fisher fisher) {
                                            return fisher.getLastFinishedTrip() != null;
                                        }
                                    }
                            ).mapToDouble(new ToDoubleFunction<Fisher>() {
                                @Override
                                public double applyAsDouble(Fisher value) {
                                    return value.getLastFinishedTrip().getProfitPerHour(true);
                                }
                            }).max().orElse(0d);

                            double lastProfits = fisher.getLastFinishedTrip() == null ? 0d : fisher.getLastFinishedTrip().getProfitPerHour(
                                    true);
                            return best - lastProfits;

                        }
                    }

                    // times exploited
                    ,new Sensor<Fisher, Double>() {
                        @Override
                        public Double scan(Fisher fisher) {
                            List<TripRecord> trips = fisher.getFinishedTrips();
                            if(trips.size() < 2)
                                return 0d;
                            else
                            {
                                int timesExploited = 0;
                                ListIterator<TripRecord> iterator = trips.listIterator(trips.size());
                                SeaTile seatile = iterator.previous().getMostFishedTileInTrip();
                                while(iterator.hasPrevious())
                                {
                                    SeaTile tileBefore = iterator.previous().getMostFishedTileInTrip();
                                    if(tileBefore== seatile)
                                        timesExploited++;
                                    else
                                       break;
                                }
                                return (double) timesExploited;
                            }

                        }
                    },
                    // days at home
                    new Sensor<Fisher, Double>() {
                        @Override
                        public Double scan(Fisher fisher) {
                            return fisher.getHoursAtPort()/24;

                        }
                    }

            );
            instances.put(state,strategy);

            Set<String> columns = strategy.getActionsTaken().getData().keySet();
            AmateurishDynamicStrategy finalStrategy = strategy;

            for(String string : columns) {
                DataColumn dailyColumn = state.getDailyDataSet().registerGatherer(string,
                                                                             new Gatherer<FishState>() {
                                                                                 @Override
                                                                                 public Double apply(FishState state) {
                                                                                     return finalStrategy.getActionsTaken().getColumn(
                                                                                             string);
                                                                                 }
                                                                             },
                                                                             Double.NaN);
                state.getYearlyDataSet().registerGatherer(string,
                                                          FishStateUtilities.generateYearlySum(dailyColumn),
                                                          Double.NaN);
            }
        }

        return  strategy;


    }

    public static Double percentageDifference(double average, double lastProfits) {
        //if there is no average (or you have no trips to compare), leave this at 0
        if(!Double.isFinite(average) || !Double.isFinite(lastProfits) )
            return 0d;
        else if(average == 0) //can't divide!
        {
            if(lastProfits > average)
                return 1d;
            if(lastProfits < average)
                return -1d;
            else
                return 0d;
        }
        else
            return (lastProfits-average)/average;
    }

    /**
     * Getter for property 'noiseRate'.
     *
     * @return Value for property 'noiseRate'.
     */
    public DoubleParameter getNoiseRate() {
        return noiseRate;
    }

    /**
     * Setter for property 'noiseRate'.
     *
     * @param noiseRate Value to set for property 'noiseRate'.
     */
    public void setNoiseRate(DoubleParameter noiseRate) {
        this.noiseRate = noiseRate;
    }

    /**
     * Getter for property 'learningRate'.
     *
     * @return Value for property 'learningRate'.
     */
    public DoubleParameter getLearningRate() {
        return learningRate;
    }

    /**
     * Setter for property 'learningRate'.
     *
     * @param learningRate Value to set for property 'learningRate'.
     */
    public void setLearningRate(DoubleParameter learningRate) {
        this.learningRate = learningRate;
    }

    /**
     * Getter for property 'explorationSize'.
     *
     * @return Value for property 'explorationSize'.
     */
    public DoubleParameter getExplorationSize() {
        return explorationSize;
    }

    /**
     * Setter for property 'explorationSize'.
     *
     * @param explorationSize Value to set for property 'explorationSize'.
     */
    public void setExplorationSize(DoubleParameter explorationSize) {
        this.explorationSize = explorationSize;
    }

    /**
     * Getter for property 'discountRate'.
     *
     * @return Value for property 'discountRate'.
     */
    public DoubleParameter getDiscountRate() {
        return discountRate;
    }

    /**
     * Setter for property 'discountRate'.
     *
     * @param discountRate Value to set for property 'discountRate'.
     */
    public void setDiscountRate(DoubleParameter discountRate) {
        this.discountRate = discountRate;
    }
}
