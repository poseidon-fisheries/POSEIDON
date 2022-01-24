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

package uk.ac.ox.oxfish.model.data.collectors;

import static tech.units.indriya.AbstractUnit.ONE;
import static tech.units.indriya.unit.Units.HOUR;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.model.FishStateDailyTimeSeries.getAllMarketColumns;
import static uk.ac.ox.oxfish.utility.Measures.KILOMETRE;

import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Aggregate data, yearly. Mostly just sums up what the daily data-set discovered
 * Created by carrknight on 6/29/15.
 */
public class FishStateYearlyTimeSeries extends TimeSeries<FishState>
{

    private final FishStateDailyTimeSeries originalGatherer;

    public FishStateYearlyTimeSeries(
            FishStateDailyTimeSeries originalGatherer) {
        super(IntervalPolicy.EVERY_YEAR, StepOrder.AGGREGATE_DATA_GATHERING);
        this.originalGatherer = originalGatherer;
    }


    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, FishState observed) {
        super.start(state, observed);


        final String fuel = FisherYearlyTimeSeries.FUEL_CONSUMPTION;
        registerGatherer(fuel, new Gatherer<FishState>() {
            @Override
            public Double apply(FishState state1) {
                Double sum = 0d;
                for (Fisher fisher : state1.getFishers())
                    sum += fisher.getYearlyData().getColumn(fuel).getLatest();

                return sum;
            }
        }, Double.NaN);


        for(Species species : observed.getSpecies())
        {

            List<String> allMarketColumns = getAllMarketColumns(observed.getAllMarketsForThisSpecie(species));
            for (String marketColumn : allMarketColumns) {
                registerYearlySumGatherer(species + " " + marketColumn);
            }

            //catches (includes discards)
            final String catchesColumn = species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME;
            registerGatherer(catchesColumn,
                             FishStateUtilities.generateYearlySum(originalGatherer.getColumn(
                                     catchesColumn)), 0d);


            //CPUE
            registerGatherer(species + " CPUE",
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState fishState) {
                            final String catches =  catchesColumn;
                            final String effort = "Total Effort";

                            DataColumn numerator = originalGatherer.getColumn(catches);
                            DataColumn denominator = originalGatherer.getColumn(effort);
                            final Iterator<Double> numeratorIterator = numerator.descendingIterator();
                            final Iterator<Double>  denominatorIterator = denominator.descendingIterator();
                            if(!numeratorIterator.hasNext()) //not ready/year 1
                                return Double.NaN;
                            double sumNumerator = 0;
                            double sumDenominator = 0;
                            for(int i=0; i<365; i++) {
                                //it should be step 365 times at most, but it's possible that this agent was added halfway through
                                //and only has a partially filled collection
                                if(numeratorIterator.hasNext()) {
                                    sumNumerator += numeratorIterator.next();
                                    sumDenominator += denominatorIterator.next();
                                }
                            }
                            return  sumNumerator/sumDenominator;

                        }
                    },Double.NaN);

            //CPHO
            registerGatherer(species + " CPHO",
                    new Gatherer<FishState>() {
                        @Override
                        public Double apply(FishState fishState) {
                            final String catches =  catchesColumn;

                            DataColumn numerator = originalGatherer.getColumn(catches);
                            final Iterator<Double> numeratorIterator = numerator.descendingIterator();
                            if(!numeratorIterator.hasNext()) //not ready/year 1
                                return Double.NaN;
                            double sumNumerator = 0;
                            for(int i=0; i<365; i++) {
                                //it should be step 365 times at most, but it's possible that this agent was added halfway through
                                //and only has a partially filled collection
                                if(numeratorIterator.hasNext()) {
                                    sumNumerator += numeratorIterator.next();
                                }
                            }
                            double sumDenominator = fishState.getFishers().stream().
                                    mapToDouble(value -> value.getLatestYearlyObservation(
                                            FisherYearlyTimeSeries.HOURS_OUT)).sum();
                            return  sumNumerator/sumDenominator;

                        }
                    },Double.NaN);

            final String price = species + " Average Sale Price";
            final String earnings = species + " " + AbstractMarket.EARNINGS_COLUMN_NAME;
            final String landings = species + " " + AbstractMarket.LANDINGS_COLUMN_NAME;
            // Only add average sales price gatherers if we have columns for earnings and landings,
            // avoiding NPE when running a minimal scenario with no markets.
            Optional.ofNullable(originalGatherer.getColumn(earnings)).ifPresent(numerator ->
                Optional.ofNullable(originalGatherer.getColumn(landings)).ifPresent(denominator ->
                    registerGatherer(
                        price,
                        fishState -> {
                            final Iterator<Double> numeratorIterator =
                                numerator.descendingIterator();
                            final Iterator<Double> denominatorIterator =
                                denominator.descendingIterator();
                            if (!numeratorIterator.hasNext()) { //not ready/year 1
                                return Double.NaN;
                            }
                            double sumNumerator = 0;
                            double sumDenominator = 0;
                            for (int i = 0; i < 365; i++) {
                                // it should be step 365 times at most, but it's possible
                                // that this agent was added halfway through and only has
                                // a partially filled collection
                                if (numeratorIterator.hasNext()) {
                                    sumNumerator += numeratorIterator.next();
                                    sumDenominator += denominatorIterator.next();
                                }
                            }
                            return sumNumerator / sumDenominator;
                        },
                        Double.NaN,
                        currency,
                        "Price"
                    )
                )
            );
        }

        for (Species species : observed.getSpecies()) {
            final String biomass = "Biomass " + species.getName();
            registerGatherer(
                biomass,
                fishState -> fishState.getTotalBiomass(species),
                Double.NaN,
                KILOGRAM,
                "Biomass"
            );
        }

        registerGatherer("Average Cash-Flow", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                            }
                        }).sum() /
                        observed.getFishers().size();
            }
        }, 0d, currency, "Cash flow");

        registerGatherer("Median Cash-Flow", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double[] profits = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                            }
                        }).toArray();
                if(profits.length == 0)
                    return Double.NaN;
                if (profits.length % 2 == 0)
                   return  (profits[profits.length/2] + profits[profits.length/2 - 1])/2;
                else
                    return profits[profits.length/2];
            }
        }, 0d, currency, "Cash flow");


        registerGatherer("Actual Median Cash-Flow", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double[] profits = observed.getFishers().stream().
                        filter(
                                new Predicate<Fisher>() {
                                    @Override
                                    public boolean test(Fisher fisher) {
                                        return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 0;

                                    }
                                }
                        ).mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                            }
                        }).toArray();
                if(profits.length == 0)
                    return Double.NaN;
                if (profits.length % 2 == 0)
                    return  (profits[profits.length/2] + profits[profits.length/2 - 1])/2;
                else
                    return profits[profits.length/2];
            }
        }, 0d, currency, "Cash flow");

        registerGatherer("Actual Median Trip Profits", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double[] profits = observed.getFishers().stream().
                        filter(
                                new Predicate<Fisher>() {
                                    @Override
                                    public boolean test(Fisher fisher) {
                                        return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 0;

                                    }
                                }
                        ).mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.EARNINGS)-
                                        value.getLatestYearlyObservation(FisherYearlyTimeSeries.VARIABLE_COSTS);
                            }
                        }).toArray();
                if(profits.length == 0)
                    return Double.NaN;
                if (profits.length % 2 == 0)
                    return  (profits[profits.length/2] + profits[profits.length/2 - 1])/2;
                else
                    return profits[profits.length/2];
            }
        }, 0d, currency, "Profits");



        registerGatherer("Actual Average Cash-Flow", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().
                        filter(
                                new Predicate<Fisher>() {
                                    @Override
                                    public boolean test(Fisher fisher) {
                                        return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 0;

                                    }
                                }
                        ).

                        mapToDouble(
                                new ToDoubleFunction<Fisher>() {
                                    @Override
                                    public double applyAsDouble(Fisher value) {
                                        return value.getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                    }
                                }).average().orElse(0d);
            }
        }, 0d, currency, "Cash flow");



        registerGatherer("Actual Average Cash Balance", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().
                        filter(
                                new Predicate<Fisher>() {
                                    @Override
                                    public boolean test(Fisher fisher) {
                                        return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 0;

                                    }
                                }
                        ).

                        mapToDouble(
                                new ToDoubleFunction<Fisher>() {
                                    @Override
                                    public double applyAsDouble(Fisher value) {
                                        return value.getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN);
                                    }
                                }).average().orElse(0d);
            }
        }, 0d, currency, "Balance");


        registerYearlySumGatherer("Total Effort");

        registerGatherer(
            "Average Distance From Port",
            ignored -> observed.getFishers()
                .stream()
                .mapToDouble(value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.FISHING_DISTANCE))
                .filter(Double::isFinite)
                .sum() / observed.getFishers().size(),
            0d,
            KILOMETRE,
            "Distance"
        );

        //weighs by trips
        registerGatherer(
            "Weighted Average Distance From Port",
            ignored -> {
                double sum = 0;
                double trips = 0;
                for (Fisher fisher : state.getFishers()) {
                    double trip = fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                    if (trip > 0) {
                        sum += fisher.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.FISHING_DISTANCE) * trip;
                        trips += trip;
                    }
                }
                return trips > 0 ? sum / trips : 0d;
            },
            0d,
            KILOMETRE,
            "Distance"
        );

        registerGatherer(
            "Average Number of Trips",
            ignored -> observed.getFishers()
                .stream()
                .mapToDouble(value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS))
                .average()
                .orElse(0d),
            0d,
            ONE,
            "Trips"
        );

        registerGatherer(
            "Total Number of Trips",
            ignored -> observed.getFishers()
                .stream()
                .mapToDouble(value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS))
                .sum(),
            0d,
            ONE,
            "Trips"
        );

        registerGatherer("Average Gas Expenditure", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.FUEL_EXPENDITURE);
                            }
                        }).filter(
                        new DoublePredicate() {
                            @Override
                            public boolean test(double d) {
                                return Double.isFinite(d);
                            }
                        }).sum() /
                        observed.getFishers().size();
            }
        }, 0d, currency, "Expenditure");


        registerGatherer("Average Variable Costs", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {

                DoubleSummaryStatistics costs = new DoubleSummaryStatistics();
                for(Fisher fisher : observed.getFishers()) {
                    double variableCosts = fisher.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.VARIABLE_COSTS);
                    if(Double.isFinite(variableCosts))
                        costs.accept(variableCosts);
                }

                return costs.getAverage();
            }
        }, 0d, currency, "Costs");

        registerGatherer("Total Variable Costs", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {

                DoubleSummaryStatistics costs = new DoubleSummaryStatistics();
                for(Fisher fisher : observed.getFishers()) {
                    double variableCosts = fisher.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.VARIABLE_COSTS);
                    if(Double.isFinite(variableCosts))
                        costs.accept(variableCosts);
                }

                return costs.getSum();
            }
        }, 0d, currency, "Costs");

        registerGatherer("Total Earnings", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {

                DoubleSummaryStatistics earnings = new DoubleSummaryStatistics();
                for(Fisher fisher : observed.getFishers()) {
                    double variableCosts = fisher.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.EARNINGS);
                    if(Double.isFinite(variableCosts))
                        earnings.accept(variableCosts);
                }

                return earnings.getSum();
            }
        }, 0d, currency, "Earnings");

        registerGatherer("Average Earnings", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {

                DoubleSummaryStatistics earnings = new DoubleSummaryStatistics();
                for(Fisher fisher : observed.getFishers()) {
                    double variableCosts = fisher.getLatestYearlyObservation(
                            FisherYearlyTimeSeries.EARNINGS);
                    if(Double.isFinite(variableCosts))
                        earnings.accept(variableCosts);
                }

                return earnings.getAverage();
            }
        }, 0d, currency, "Earnings");

        registerGatherer("Average Trip Earnings", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double hoursOut = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.EARNINGS);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();
                double trips = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();

                return trips > 0 ? hoursOut/trips : 0d;
            }
        }, 0d, currency, "Earnings");

        registerGatherer("Average Trip Variable Costs", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double hoursOut = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.VARIABLE_COSTS);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();
                double trips = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();

                return trips > 0 ? hoursOut/trips : 0d;
            }
        }, 0d, currency, "Costs");

        registerGatherer("Average Trip Income", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double earnings = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher fisher) {
                                return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.EARNINGS) -
                                        fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.VARIABLE_COSTS);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();
                double trips = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();

                return trips > 0 ? earnings/trips : 0d;
            }
        }, 0d);



        registerGatherer("Average Income per Hour Out", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double earnings = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher fisher) {
                                return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.EARNINGS) -
                                        fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.VARIABLE_COSTS);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();
                double hours = observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT);
                            }
                        }).filter(new DoublePredicate() { //skip boats that made no trips
                    @Override
                    public boolean test(double value) {
                        return Double.isFinite(value);
                    }
                }).sum();

                return hours > 0 ? earnings/hours : 0d;
            }
        }, 0d);

        //do not just average the trip duration per fisher because otherwise you don't weigh them according to how many trips they actually did
        registerGatherer(
            "Average Trip Duration",
            ignored -> {
                //skip boats that made no trips
                double hoursOut = observed.getFishers().stream()
                    .mapToDouble(value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT))
                    .filter(Double::isFinite)
                    .sum();
                //skip boats that made no trips
                double trips = observed.getFishers().stream()
                    .mapToDouble(value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS))
                    .filter(Double::isFinite)
                    .sum();
                return trips > 0 ? hoursOut / trips : 0d;
            },
            0d,
            HOUR,
            "Duration"
        );

        registerGatherer(
            "Actual Average Hours Out",
            ignored -> observed.getFishers().stream()
                .filter(fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 0)
                .mapToDouble(value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT))
                .average()
                .orElse(0d),
            0d,
            HOUR,
            "Duration"
        );

        registerGatherer(
            "Average Hours Out",
            ignored -> observed.getFishers().stream()
                .mapToDouble(value -> value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT))
                .average()
                .orElse(0d),
            0d,
            HOUR,
            "Duration"
        );

        registerGatherer(
            "Number Of Active Fishers",
            fishState ->
                (double) fishState.getFishers().stream().
                    filter(fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS) > 0).
                    count(),
            Double.NaN,
            ONE,
            "Fishers"
        );

        if(state.getPorts().size()>1)
        {
            //add data on profits in each port
            for(Port port : state.getPorts())
            {

                String portname = port.getName();

                for (Species species : state.getBiology().getSpecies()) {
                    final String columnName = species.getName() + " " + AbstractMarket.LANDINGS_COLUMN_NAME;
                    state.getYearlyDataSet().registerGatherer(
                        portname + " " + columnName,
                        fishState -> fishState.getFishers().stream()
                            .filter(fisher -> fisher.getHomePort().equals(port))
                            .mapToDouble(value -> value.getLatestYearlyObservation(columnName))
                            .sum(),
                        Double.NaN,
                        KILOGRAM,
                        "Biomass"
                    );
                }

                state.getYearlyDataSet().registerGatherer(portname + " Total Income",
                                                          fishState ->
                                                                  fishState.getFishers().stream().
                                                                          filter(fisher -> fisher.getHomePort().equals(port)).
                                                                          mapToDouble(value -> value.getLatestYearlyObservation(
                                                                                  FisherYearlyTimeSeries.CASH_FLOW_COLUMN)).sum(), Double.NaN);


                state.getYearlyDataSet().registerGatherer(portname + " Average Distance From Port",
                                                          fishState ->
                                                                  fishState.getFishers().stream().
                                                                          filter(fisher -> fisher.getHomePort().equals(port)).
                                                                          mapToDouble(value -> value.getLatestYearlyObservation(
                                                                                  FisherYearlyTimeSeries.FISHING_DISTANCE)).
                                                                          filter(Double::isFinite).average().
                                                                          orElse(Double.NaN), Double.NaN);

                state.getYearlyDataSet().registerGatherer(portname + " " +FisherYearlyTimeSeries.TRIPS,
                                                          fishState ->
                                                                  fishState.getFishers().stream().
                                                                          filter(fisher -> fisher.getHomePort().equals(port)).
                                                                          mapToDouble(value -> value.getLatestYearlyObservation(
                                                                                  FisherYearlyTimeSeries.TRIPS)).average().
                                                                          orElse(Double.NaN), 0 );



                state.getYearlyDataSet().registerGatherer(portname + " Number Of Fishers",
                                                          fishState ->
                                                                  (double)fishState.getFishers().stream().
                                                                          filter(fisher ->
                                                                                         fisher.getHomePort().
                                                                                                 equals(port)).count(),
                                                          Double.NaN);

                state.getYearlyDataSet().registerGatherer(portname + " Number Of Active Fishers",
                                                          fishState ->
                                                                  (double)fishState.getFishers().stream().
                                                                          filter(new Predicate<Fisher>() {
                                                                              @Override
                                                                              public boolean test(Fisher fisher) {
                                                                                  return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS)>0;
                                                                              }
                                                                          }).
                                                                          filter(fisher ->
                                                                                         fisher.getHomePort().
                                                                                                 equals(port)).count(),
                                                          Double.NaN);


                //some scenarios collect it automatically
                if(state.getYearlyDataSet().getColumn("Total Hours Out")==null) {
                    state.getYearlyDataSet().registerGatherer("Total Hours Out",
                            fishState ->
                                    fishState.getFishers().stream().
                                            mapToDouble(value -> value.getLatestYearlyObservation(
                                                    FisherYearlyTimeSeries.HOURS_OUT)).sum(),
                            0);
                }
                state.getYearlyDataSet().registerGatherer("Average Cash-Flow at " + port.getName(),
                                                          new Gatherer<FishState>() {
                                                              @Override
                                                              public Double apply(FishState observed) {
                                                                  List<Fisher> fishers = observed.getFishers().stream().
                                                                          filter(new Predicate<Fisher>() {
                                                                              @Override
                                                                              public boolean test(Fisher fisher) {
                                                                                  return fisher.getHomePort().equals(port);
                                                                              }
                                                                          }).collect(Collectors.toList());
                                                                  return fishers.stream().
                                                                          mapToDouble(
                                                                                  new ToDoubleFunction<Fisher>() {
                                                                                      @Override
                                                                                      public double applyAsDouble(Fisher value) {
                                                                                          return value.getLatestYearlyObservation(
                                                                                                  FisherYearlyTimeSeries.CASH_FLOW_COLUMN);
                                                                                      }
                                                                                  }).sum() /
                                                                          fishers.size();
                                                              }
                                                          }, Double.NaN, currency, "Cash flow");
            }
        }


    }

    private void registerYearlySumGatherer(final String columnName) {
        final DataColumn column = originalGatherer.getColumn(columnName);
        registerGatherer(
            columnName,
            FishStateUtilities.generateYearlySum(column),
            0d,
            column.getUnit(),
            column.getYLabel()
        );
    }

}
