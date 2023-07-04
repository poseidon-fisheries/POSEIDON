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
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.LinkedList;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.maybeGetFadManager;

/**
 * the data gatherer for a fisher that steps every year. It gathers:
 * <ul>
 *     <li> CASH</li>
 *     <li> NET_CASH_FLOW</li>
 * </ul>
 */
public class FisherYearlyTimeSeries extends TimeSeries<Fisher> {


    public static final String CASH_COLUMN = "CASH";
    public static final String CASH_FLOW_COLUMN = "NET_CASH_FLOW";
    public static final String FUEL_CONSUMPTION = "FUEL_CONSUMPTION";
    public static final String FUEL_EXPENDITURE = "FUEL_EXPENDITURE";
    public static final String VARIABLE_COSTS = "VARIABLE_COSTS";
    public static final String EARNINGS = "EARNINGS";
    public static final String TRIPS = "NUMBER_OF_TRIPS";
    public static final String EFFORT = "HOURS_OF_EFFORT";
    public static final String FISHING_DISTANCE = "DISTANCE_TOW_TO_PORT";
    public static final String TRIP_DURATION = "MEAN_TRIP_DURATION";
    public static final String HOURS_OUT = "HOURS_AT_SEA";
    public static final String PROFITS_PER_HOUR = "TRIP_PROFITS_PER_HOUR";
    public static final String PROFITS_PER_TRIP = "PROFITS_PER_TRIP";
    public static final String ACTIVE_FADS_LIMIT_COLUMN_NAME = "Active FADs limit";
    private static final long serialVersionUID = 8256173666950929101L;

    public FisherYearlyTimeSeries() {
        super(IntervalPolicy.EVERY_YEAR);
    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(final FishState state, final Fisher observed) {
        //CASH
        registerGatherer(CASH_COLUMN, Fisher::getBankBalance, Double.NaN);

        registerGatherer(CASH_FLOW_COLUMN,
            new Gatherer<Fisher>() {
                private static final long serialVersionUID = 4879018499700801027L;
                double oldCash = observed.getBankBalance();

                @Override
                public Double apply(final Fisher fisher) {
                    final double flow = fisher.getBankBalance() - oldCash;
                    oldCash = fisher.getBankBalance();
                    return flow;
                }
            }, Double.NaN
        );


        registerGatherer(FUEL_CONSUMPTION,
            fisher -> observed.getYearlyCounterColumn(FUEL_CONSUMPTION), Double.NaN
        );

        registerGatherer(FUEL_EXPENDITURE,
            fisher -> observed.getYearlyCounterColumn(FUEL_EXPENDITURE), Double.NaN
        );

        registerGatherer(VARIABLE_COSTS,
            fisher -> observed.getYearlyCounterColumn(VARIABLE_COSTS), Double.NaN
        );

        registerGatherer(EARNINGS,
            fisher -> observed.getYearlyCounterColumn(EARNINGS), Double.NaN
        );

        registerGatherer(TRIPS,
            fisher -> observed.getYearlyCounterColumn(TRIPS), Double.NaN
        );

        registerGatherer(EFFORT,
            fisher -> observed.getYearlyCounterColumn(EFFORT), Double.NaN
        );

        registerGatherer(HOURS_OUT,
            fisher -> observed.getYearlyCounterColumn(HOURS_OUT), Double.NaN
        );
        registerGatherer(PROFITS_PER_HOUR,
            fisher ->
                (observed.getYearlyCounterColumn(EARNINGS) - observed.getYearlyCounterColumn(VARIABLE_COSTS)) /
                    observed.getYearlyCounterColumn(HOURS_OUT), Double.NaN
        );
        registerGatherer(PROFITS_PER_TRIP,
            fisher ->
                (observed.getYearlyCounterColumn(EARNINGS) - observed.getYearlyCounterColumn(VARIABLE_COSTS)) /
                    observed.getYearlyCounterColumn(TRIPS), Double.NaN
        );

        //this is a set because it gets accessed by two different gatherers and can be filled by either
        registerGatherer(FISHING_DISTANCE,
            new Gatherer<Fisher>() {
                private static final long serialVersionUID = -1529959563740267593L;
                final HashSet<TripRecord> alreadyExaminedTrips = new HashSet<>();

                @Override
                public Double apply(final Fisher fisher) {

                    final NauticalMap map = state.getMap();
                    final SeaTile portLocation = fisher.getHomePort().getLocation();
                    final Collection<TripRecord> trips = new LinkedList<>(fisher.getFinishedTrips());
                    final boolean removedSome = trips.removeAll(alreadyExaminedTrips);
                    assert removedSome ^ alreadyExaminedTrips.isEmpty();
                    final DoubleSummaryStatistics totalDistance = new DoubleSummaryStatistics();
                    for (final TripRecord record : trips) {
                        for (final SeaTile tile : record.getTilesFished()) {
                            totalDistance.accept(map.distance(tile, portLocation));
                        }
                    }
                    assert totalDistance.getAverage() > 0 || totalDistance.getCount() == 0;
                    alreadyExaminedTrips.addAll(trips);

                    if (totalDistance.getCount() == 0)
                        return Double.NaN;
                    else
                        return totalDistance.getAverage();

                }
            }, Double.NaN
        );
        registerGatherer(TRIP_DURATION,
            (Gatherer<Fisher>) fisher -> observed.getYearlyCounterColumn(HOURS_OUT) /
                observed.getYearlyCounterColumn(TRIPS), Double.NaN
        );

        //also aggregate
        for (final Species species : state.getSpecies()) {
            final String landings = species + " " + AbstractMarket.LANDINGS_COLUMN_NAME;
            registerGatherer(
                landings,
                FishStateUtilities.generateYearlySum(observed.getDailyData().getColumn(landings)),
                Double.NaN,
                KILOGRAM,
                "Biomass"
            );

            final String catches = species + " " + FisherDailyTimeSeries.CATCHES_COLUMN_NAME;


            registerGatherer(catches,
                FishStateUtilities.generateYearlySum(observed.getDailyData().getColumn(
                    catches))
                , Double.NaN
            );

        }

        registerGatherer(
            ACTIVE_FADS_LIMIT_COLUMN_NAME,
            fisher ->
                maybeGetFadManager(fisher)
                    .map(FadManager::getActionSpecificRegulations)
                    .flatMap(ActiveActionRegulations::getActiveFadLimits)
                    .map(activeFadLimits -> activeFadLimits.getLimit(fisher))
                    .orElse(Integer.MAX_VALUE)
                    .doubleValue(),
            0
        );

        registerGatherer(
            NumberOfActiveFadsGatherer.COLUMN_NAME,
            new NumberOfActiveFadsGatherer(),
            0
        );

        super.start(state, observed);

    }
}
