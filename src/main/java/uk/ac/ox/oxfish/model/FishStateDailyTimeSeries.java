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

package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.groupingBy;
import static tech.units.indriya.unit.Units.HOUR;
import static tech.units.indriya.unit.Units.KILOGRAM;

/**
 * Aggregate data. Goes through all the ports and all the markets and
 * aggregate landings and earnings by species
 * Created by carrknight on 6/16/15.
 */
public class FishStateDailyTimeSeries extends TimeSeries<FishState> {


    public static final String AVERAGE_LAST_TRIP_HOURLY_PROFITS = "Average Last Trip Hourly Profits";

    public FishStateDailyTimeSeries() {
        super(IntervalPolicy.EVERY_DAY,StepOrder.YEARLY_DATA_GATHERING);
    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, FishState observed) {

        //get all the markets for this species
        //now register each
        observed.getSpecies().forEach(species ->
            registerSummaryGatherers(
                observed.getAllMarketsForThisSpecie(species)
                    .stream()
                    .flatMap(market -> market.getData().getColumns().stream())
                    ::iterator,
                species.getName() + " %s"
            )
        );

        //add a counter for all catches (including discards) by asking each fisher individually
        for(Species species : observed.getSpecies())
        {

            String catchesColumn = species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME;
            registerGatherer(catchesColumn,
                             new Gatherer<FishState>() {
                                 @Override
                                 public Double apply(FishState ignored) {
                                     return observed.getFishers().stream().mapToDouble(
                                             new ToDoubleFunction<Fisher>() {
                                                 @Override
                                                 public double applyAsDouble(Fisher value) {
                                                     return value.getDailyCounter().getCatchesPerSpecie(species.getIndex());
                                                 }
                                             }).sum();
                                 }
                             }, 0d, KILOGRAM, "Biomass");
        }

        final List<Fisher> fishers = state.getFishers();



        registerGatherer("Total Effort", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getDailyCounter().getColumn(FisherYearlyTimeSeries.EFFORT);
                            }
                        }).sum();
            }
        }, 0d, HOUR, "Effort");

        registerGatherer(AVERAGE_LAST_TRIP_HOURLY_PROFITS, new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {

                if(fishers.size()==0)
                    return 0d;

                double sum = 0;
                for (Fisher fisher : observed.getFishers()) {
                    TripRecord lastTrip = fisher.getLastFinishedTrip();
                    if (lastTrip != null ) {
                        double lastProfits = lastTrip.getProfitPerHour(true);
                        if(Double.isFinite(lastProfits)) //NaN or Infinite are assumed to be 0 here
                            sum+= lastProfits;
                    }

                }

                return sum/(double)fishers.size();
            }
        }, 0d);






        super.start(state, observed);
    }

    public static List<String> getAllMarketColumns(Collection<Market> markets) {
        //get all important columns
        return markets.stream()
            .flatMap(market -> market.getData().getColumns().stream().map(DataColumn::getName))
            .distinct()
            .collect(Collectors.toList());
    }

    private void registerSummaryGatherers(Iterable<DataColumn> allColumns, String nameTemplate) {
        stream(allColumns)
            .collect(groupingBy(DataColumn::getName))
            .forEach((name, columns) -> {
                checkArgument(
                    columns.stream().map(DataColumn::getName).distinct().limit(2).count() == 1,
                    "All columns named '%s' must have same y-label.",
                    columns.get(0).getName()
                );
                checkArgument(
                    columns.stream().map(DataColumn::getUnit).distinct().limit(2).count() == 1,
                    "All columns named '%s' must have same unit.",
                    columns.get(0).getName()
                );
                registerGatherer(
                    String.format(nameTemplate, name),
                    __ -> columns.stream().mapToDouble(DataColumn::getLatest).sum(),
                    Double.NaN,
                    columns.get(0).getUnit(),
                    columns.get(0).getYLabel()
                );
            });
    }

}
