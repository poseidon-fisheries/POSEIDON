package uk.ac.ox.oxfish.model.data.collectors;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * the data gatherer for a fisher that steps every year. It gathers:
 * <ul>
 *     <li> CASH</li>
 *     <li> NET_CASH_FLOW</li>
 * </ul>
 */
public class YearlyFisherTimeSeries extends TimeSeries<Fisher>
{


    public static final String CASH_COLUMN = "CASH";
    public static final String CASH_FLOW_COLUMN = "NET_CASH_FLOW";
    public static final String FUEL_CONSUMPTION = "FUEL_CONSUMPTION";
    public static final String FUEL_EXPENDITURE = "FUEL_EXPENDITURE";
    public static final String TRIPS = "NUMBER_OF_TRIPS";
    public static final String EFFORT = "HOURS_OF_EFFORT";
    public static final String FISHING_DISTANCE = "DISTANCE_TOW_TO_PORT";
    public static final String TRIP_DURATION = "MEAN_TRIP_DURATION";
    public static final String HOURS_OUT = "HOURS_AT_SEA";

    public YearlyFisherTimeSeries() {
        super(IntervalPolicy.EVERY_YEAR);
    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, Fisher observed) {
        //CASH
        registerGatherer(CASH_COLUMN, Fisher::getBankBalance, Double.NaN);

        registerGatherer(CASH_FLOW_COLUMN,
                         new Gatherer<Fisher>() {
            double oldCash = observed.getBankBalance();

            @Override
            public Double apply(Fisher fisher) {
                double flow = fisher.getBankBalance() - oldCash;
                oldCash = fisher.getBankBalance();
                return flow;
            }
        }, Double.NaN);


        registerGatherer(FUEL_CONSUMPTION,
                         fisher -> observed.getYearlyCounterColumn(FUEL_CONSUMPTION),Double.NaN);

        registerGatherer(FUEL_EXPENDITURE,
                         fisher -> observed.getYearlyCounterColumn(FUEL_EXPENDITURE),Double.NaN);

        registerGatherer(TRIPS,
                         fisher -> observed.getYearlyCounterColumn(TRIPS),Double.NaN);

        registerGatherer(EFFORT,
                         fisher -> observed.getYearlyCounterColumn(EFFORT),Double.NaN);

        registerGatherer(HOURS_OUT,
                         fisher -> observed.getYearlyCounterColumn(HOURS_OUT),Double.NaN);

        //this is a set because it gets accessed by two different gatherers and can be filled by either
        registerGatherer(FISHING_DISTANCE,
                         new Gatherer<Fisher>() {
                             final HashSet<TripRecord> alreadyExaminedTrips = new HashSet<>();
                             @Override
                             public Double apply(Fisher fisher) {

                                 NauticalMap map = state.getMap();
                                 SeaTile portLocation = fisher.getHomePort().getLocation();
                                 LinkedList<TripRecord> trips = new LinkedList<>(fisher.getFinishedTrips());
                                 boolean removedSome = trips.removeAll(alreadyExaminedTrips);
                                 assert removedSome ^ alreadyExaminedTrips.isEmpty();
                                 DoubleSummaryStatistics totalDistance = new DoubleSummaryStatistics();
                                 for(TripRecord record : trips)
                                 {
                                     for(SeaTile tile : record.getTilesFished())
                                     {
                                         totalDistance.accept(map.distance(tile,portLocation));
                                     }
                                 }
                                 assert totalDistance.getAverage() > 0 || totalDistance.getCount()==0;
                                 alreadyExaminedTrips.addAll(trips);

                                 if(totalDistance.getCount()==0)
                                     return Double.NaN;
                                 else
                                     return totalDistance.getAverage();

                             }
                         }, Double.NaN);
        registerGatherer(TRIP_DURATION,
                         new Gatherer<Fisher>() {
                             final HashSet<TripRecord> alreadyExaminedTrips = new HashSet<>();

                             @Override
                             public Double apply(Fisher fisher) {

                                 LinkedList<TripRecord> trips = new LinkedList<>(fisher.getFinishedTrips());
                                 boolean removedSome = trips.removeAll(alreadyExaminedTrips);
                                 assert removedSome ^ alreadyExaminedTrips.isEmpty();
                                 DoubleSummaryStatistics timeOut = new DoubleSummaryStatistics();
                                 for(TripRecord record : trips)
                                 {
                                    timeOut.accept(record.getDurationInHours());
                                 }
                                 assert timeOut.getAverage() > 0 || timeOut.getCount()==0;
                                 alreadyExaminedTrips.addAll(trips);

                                 if(timeOut.getCount()==0)
                                     return Double.NaN;
                                 else
                                     return timeOut.getAverage();

                             }
                         }, Double.NaN);

        //also aggregate
        for(Species species : state.getSpecies())
        {
            final String landings = species + " " + AbstractMarket.LANDINGS_COLUMN_NAME;
            registerGatherer(landings,
                             FishStateUtilities.generateYearlySum(observed.getDailyData().getColumn(
                                     landings))
                    , Double.NaN);





        }


        super.start(state, observed);

    }
}
