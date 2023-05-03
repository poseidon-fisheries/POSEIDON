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
 * Created by carrknight on 10/13/16.
 */
public class UnifiedAmateurishDynamicFactory implements AlgorithmFactory<AmateurishDynamicStrategy> {


    private static final UnifiedAmateurishDynamicFactory instance = new UnifiedAmateurishDynamicFactory();
    private final WeakHashMap<FishState, AmateurishDynamicStrategy> instances = new WeakHashMap<>();
    private DoubleParameter discountRate = new FixedDoubleParameter(0);
    private DoubleParameter noiseRate = new FixedDoubleParameter(.02);
    private DoubleParameter learningRate = new FixedDoubleParameter(.00025);
    private DoubleParameter explorationSize = new FixedDoubleParameter(5);

    private UnifiedAmateurishDynamicFactory() {
    }

    public static UnifiedAmateurishDynamicFactory getInstance() {
        return instance;
    }

    public static Double percentageDifference(final double average, final double lastProfits) {
        //if there is no average (or you have no trips to compare), leave this at 0
        if (!Double.isFinite(average) || !Double.isFinite(lastProfits))
            return 0d;
        else if (average == 0) //can't divide!
        {
            if (lastProfits > average)
                return 1d;
            if (lastProfits < average)
                return -1d;
            else
                return 0d;
        } else
            return (lastProfits - average) / average;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public AmateurishDynamicStrategy apply(final FishState state) {

        AmateurishDynamicStrategy strategy = instances.get(state);
        if (strategy == null) {
            strategy = new AmateurishDynamicStrategy(
                learningRate.applyAsDouble(state.getRandom()),
                noiseRate.applyAsDouble(state.getRandom()),
                state.getMap(), state.getRandom(),
                (int) explorationSize.applyAsDouble(state.getRandom()),
                discountRate.applyAsDouble(state.getRandom()),
                //intercept

                new Sensor<Fisher, Double>() {
                    @Override
                    public Double scan(final Fisher system) {
                        return 1d;
                    }
                },

                //previous profits

                new Sensor<Fisher, Double>() {
                    @Override
                    public Double scan(final Fisher fisher) {
                        return fisher.getLastFinishedTrip() == null ? 0d : fisher.getLastFinishedTrip()
                            .getProfitPerHour(
                                true);
                    }
                },
                //  distance from average

                new Sensor<Fisher, Double>() {
                    @Override
                    public Double scan(final Fisher fisher) {

                        final double average = state.getLatestDailyObservation(
                            FishStateDailyTimeSeries.AVERAGE_LAST_TRIP_HOURLY_PROFITS);
                        //get last profits (to compare)
                        final double lastProfits = fisher.getLastFinishedTrip() == null ? Double.NaN : fisher.getLastFinishedTrip()
                            .getProfitPerHour(
                                true);
                        final double distance = average - lastProfits;
                        return Double.isFinite(distance) ? distance : 0d; // average is NaN at the beginning


                    }
                },
                //  distance from best friend
                new Sensor<Fisher, Double>() {
                    @Override
                    public Double scan(final Fisher fisher) {
                        final double best = fisher.getDirectedFriends().stream().filter(
                            new Predicate<Fisher>() {
                                @Override
                                public boolean test(final Fisher fisher) {
                                    return fisher.getLastFinishedTrip() != null;
                                }
                            }
                        ).mapToDouble(new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(final Fisher value) {
                                return value.getLastFinishedTrip().getProfitPerHour(true);
                            }
                        }).max().orElse(0d);

                        final double lastProfits = fisher.getLastFinishedTrip() == null ? 0d : fisher.getLastFinishedTrip()
                            .getProfitPerHour(
                                true);
                        return best - lastProfits;

                    }
                }

                // times exploited
                , new Sensor<Fisher, Double>() {
                @Override
                public Double scan(final Fisher fisher) {
                    final List<TripRecord> trips = fisher.getFinishedTrips();
                    if (trips.size() < 2)
                        return 0d;
                    else {
                        int timesExploited = 0;
                        final ListIterator<TripRecord> iterator = trips.listIterator(trips.size());
                        final SeaTile seatile = iterator.previous().getMostFishedTileInTrip();
                        while (iterator.hasPrevious()) {
                            final SeaTile tileBefore = iterator.previous().getMostFishedTileInTrip();
                            if (tileBefore == seatile)
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
                    public Double scan(final Fisher fisher) {
                        return fisher.getHoursAtPort() / 24;

                    }
                }

            );
            instances.put(state, strategy);

            final Set<String> columns = strategy.getActionsTaken().getValidCounters();
            final AmateurishDynamicStrategy finalStrategy = strategy;

            for (final String string : columns) {
                final DataColumn dailyColumn = state.getDailyDataSet().registerGatherer(
                    string,
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(final FishState state) {
                            return finalStrategy.getActionsTaken().getColumn(
                                string);
                        }
                    },
                    Double.NaN
                );
                state.getYearlyDataSet().registerGatherer(
                    string,
                    FishStateUtilities.generateYearlySum(dailyColumn),
                    Double.NaN
                );
            }
        }

        return strategy;


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
    public void setNoiseRate(final DoubleParameter noiseRate) {
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
    public void setLearningRate(final DoubleParameter learningRate) {
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
    public void setExplorationSize(final DoubleParameter explorationSize) {
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
    public void setDiscountRate(final DoubleParameter discountRate) {
        this.discountRate = discountRate;
    }
}
