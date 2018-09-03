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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FishStateDailyTimeSeries;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static uk.ac.ox.oxfish.model.FishStateDailyTimeSeries.getAllMarketColumns;

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


            List<String> allPossibleColumns = getAllMarketColumns(observed.getAllMarketsForThisSpecie(species));
            for(String column : allPossibleColumns)
                registerGatherer(species + " " + column,
                                 FishStateUtilities.generateYearlySum(
                                         originalGatherer.getColumn(species + " " + column)),0d);




            //catches (includes discards)
            String catchesColumn = species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME;
            registerGatherer(catchesColumn,
                             FishStateUtilities.generateYearlySum(originalGatherer.getColumn(
                                     catchesColumn)), 0d);




            final String price = species + " Average Sale Price";
            registerGatherer(price,
                             new Gatherer<FishState>() {
                                 @Override
                                 public Double apply(FishState fishState) {
                                     final String earnings =  species + " " +AbstractMarket.EARNINGS_COLUMN_NAME;
                                     final String landings = species + " " + AbstractMarket.LANDINGS_COLUMN_NAME;

                                     DataColumn numerator = originalGatherer.getColumn(earnings);
                                     DataColumn denominator = originalGatherer.getColumn(landings);
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




        }

        for(Species species : observed.getSpecies())
        {
            final String biomass = "Biomass " + species.getName();
            registerGatherer(biomass,
                             new Gatherer<FishState>() {
                                 @Override
                                 public Double apply(FishState state1) {
                                     return originalGatherer.getLatestObservation(biomass);
                                 }
                             }
                    , Double.NaN);
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
        }, 0d);

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
        }, 0d);


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
        }, 0d);

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
        }, 0d);



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
        }, 0d);



        registerGatherer("Total Effort",
                         FishStateUtilities.generateYearlySum(originalGatherer.getColumn("Total Effort")), 0d);


        registerGatherer("Average Distance From Port", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.FISHING_DISTANCE);
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
        }, 0d);

        //weighs by trips
        registerGatherer("Weighted Average Distance From Port", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double sum = 0;
                double trips = 0;
                for (Fisher fisher : state.getFishers()) {
                    double trip = fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                    if(trip>0) {
                        sum += fisher.getLatestYearlyObservation(
                                FisherYearlyTimeSeries.FISHING_DISTANCE) * trip;
                        trips += trip;
                    }
                }
                return trips > 0 ?  sum / trips : 0d;
            }
        },0d);

        registerGatherer("Average Number of Trips", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS);
                            }
                        }).average().orElse(0d);
            }
        }, 0d);

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
        }, 0d);


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
        }, 0d);

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
        }, 0d);

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
        }, 0d);

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
        }, 0d);

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
        }, 0d);

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
        }, 0d);


        //do not just average the trip duration per fisher because otherwise you don't weigh them according to how many trips they actually did
        registerGatherer("Average Trip Duration", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                double hoursOut = observed.getFishers().stream().mapToDouble(
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
        }, 0d);

        registerGatherer("Actual Average Hours Out", new Gatherer<FishState>() {
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
                                        return value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT);
                                    }
                                }).average().orElse(0d);
            }
        }, 0d);



        registerGatherer("Average Hours Out", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getLatestYearlyObservation(FisherYearlyTimeSeries.HOURS_OUT);
                            }
                        }).average().orElse(0d);
            }
        }, 0d);

        registerGatherer("Number Of Active Fishers",
                         fishState ->
                                 (double)fishState.getFishers().stream().
                                         filter(new Predicate<Fisher>() {
                                             @Override
                                             public boolean test(Fisher fisher) {
                                                 return fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS)>0;
                                             }
                                         }).
                                         count(),
                         Double.NaN);


        if(state.getPorts().size()>1)
        {
            //add data on profits in each port
            for(Port port : state.getPorts())
            {

                String portname = port.getName();
                for(Species species : state.getBiology().getSpecies())
                {

                    state.getYearlyDataSet().registerGatherer(
                            portname + " " + species.getName() + " " + AbstractMarket.LANDINGS_COLUMN_NAME,
                            fishState -> fishState.getFishers().stream().
                                    filter(fisher -> fisher.getHomePort().equals(port)).
                                    mapToDouble(value -> value.getLatestYearlyObservation(
                                            species + " " + AbstractMarket.LANDINGS_COLUMN_NAME)).sum(), Double.NaN);
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
                                                          }, Double.NaN);
            }
        }


    }



}
