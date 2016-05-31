package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.List;
import java.util.function.ToDoubleFunction;

/**
 * Aggregate data. Goes through all the ports and all the markets and
 * aggregate landings and earnings by species
 * Created by carrknight on 6/16/15.
 */
public class FishStateDailyTimeSeries extends TimeSeries<FishState> {


    public static final String AVERAGE_LAST_TRIP_PROFITS = "Average Last Trip Profits";

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


        for(Species species : observed.getSpecies())
        {
            //get all the markets for this species
            final List<Market> toAggregate = observed.getAllMarketsForThisSpecie(species);
            //now register it!
            final String landingsColumnName = AbstractMarket.LANDINGS_COLUMN_NAME;
            final String earningsColumnName = AbstractMarket.EARNINGS_COLUMN_NAME;
            registerGatherer(species + " " + landingsColumnName,
                             //so "stream" is a trick from Java 8. In this case it just sums up all the data
                             new Gatherer<FishState>() {
                                 @Override
                                 public Double apply(FishState model) {
                                     return toAggregate.stream().mapToDouble(
                                             value -> value.getData().getLatestObservation(landingsColumnName))
                                             .sum();
                                 }
                             }, Double.NaN);

            registerGatherer(species + " " + earningsColumnName,
                             //so "stream" is a trick from Java 8. In this case it just sums up all the data
                             new Gatherer<FishState>() {
                                 @Override
                                 public Double apply(FishState model) {
                                     return toAggregate.stream().mapToDouble(
                                             value -> value.getData().getLatestObservation(earningsColumnName))
                                             .sum();
                                 }
                             }, Double.NaN);



        }

        final List<Fisher> fishers = state.getFishers();
        //number of fishers
        registerGatherer("Number of Fishers", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return (double) fishers.size();
            }
        }, 0d);
        //fishers who are actually out
        registerGatherer("Fishers at Sea", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return fishers.stream().mapToDouble(
                        value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum();
            }
        }, 0d);

        registerGatherer("Total Effort", new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                return value.getDailyCounter().getColumn(YearlyFisherTimeSeries.EFFORT);
                            }
                        }).sum();
            }
        }, 0d);

        registerGatherer(AVERAGE_LAST_TRIP_PROFITS, new Gatherer<FishState>() {
            @Override
            public Double apply(FishState ignored) {
                return observed.getFishers().stream().mapToDouble(
                        new ToDoubleFunction<Fisher>() {
                            @Override
                            public double applyAsDouble(Fisher value) {
                                TripRecord lastTrip = value.getLastFinishedTrip();
                                if(lastTrip == null || !Double.isFinite(lastTrip.getProfitPerHour(true) ))
                                    return 0d;
                                else
                                    return lastTrip.getProfitPerHour(true);
                            }
                        }).average().orElse(0d);
            }
        }, 0d);


        final NauticalMap map = state.getMap();
        final List<SeaTile> allSeaTilesAsList = map.getAllSeaTilesAsList();

        for(Species species : observed.getSpecies())
        {
            registerGatherer("Biomass " + species.getName(),
                             new Gatherer<FishState>() {
                                 @Override
                                 public Double apply(FishState state1) {
                                     return allSeaTilesAsList.stream().mapToDouble(
                                             value -> value.getBiomass(species)).sum();
                                 }
                             },
                             0d);
        }




        super.start(state, observed);
    }
}
