package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.List;

/**
 * Aggregate data. Goes through all the ports and all the markets and
 * aggregate landings and earnings by species
 * Created by carrknight on 6/16/15.
 */
public class FishStateDailyTimeSeries extends TimeSeries<FishState> {


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
                             model ->
                                     toAggregate.stream().mapToDouble(
                                     value -> value.getData().getLatestObservation(landingsColumnName))
                                     .sum(), Double.NaN);

            registerGatherer(species + " " + earningsColumnName,
                             //so "stream" is a trick from Java 8. In this case it just sums up all the data
                             model -> toAggregate.stream().mapToDouble(
                                     value -> value.getData().getLatestObservation(earningsColumnName))
                                     .sum(), Double.NaN);
        }

        final List<Fisher> fishers = state.getFishers();
        //number of fishers
        registerGatherer("Number of Fishers", ignored -> (double) fishers.size(), 0d);
        //fishers who are actually out
        registerGatherer("Fishers at Sea", ignored -> fishers.stream().mapToDouble(
                value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum(), 0d);


        final NauticalMap map = state.getMap();
        final List<SeaTile> allSeaTilesAsList = map.getAllSeaTilesAsList();

        for(Species species : observed.getSpecies())
        {
            registerGatherer("Biomass " + species.getName(),
                             state1 -> allSeaTilesAsList.stream().mapToDouble(value -> value.getBiomass(species)).sum(),
                             0d);
        }


        registerGatherer("Total Fishing Intensity", state1 -> state1.getMap().getFishingIntensity(),Double.NaN);


        super.start(state, observed);
    }
}
