package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.DataSet;
import uk.ac.ox.oxfish.model.data.IntervalPolicy;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;

import java.util.List;

/**
 * Aggregate data. Goes through all the ports and all the markets and
 * aggregate landings and earnings by species
 * Created by carrknight on 6/16/15.
 */
public class FishStateDailyDataSet extends DataSet<FishState> {


    public FishStateDailyDataSet() {
        super(IntervalPolicy.EVERY_DAY,StepOrder.AGGREGATE_DATA_GATHERING);
    }

    /**
     * call this to start the observation
     *
     * @param state    model
     * @param observed the object to observe
     */
    @Override
    public void start(FishState state, FishState observed) {


        for(Specie specie : observed.getSpecies())
        {
            //get all the markets for this specie
            final List<Market> toAggregate = observed.getAllMarketsForThisSpecie(specie);
            //now register it!
            final String landingsColumnName = AbstractMarket.LANDINGS_COLUMN_NAME;
            final String earningsColumnName = AbstractMarket.EARNINGS_COLUMN_NAME;
            registerGatherer(specie + " " + landingsColumnName,
                             //so "stream" is a trick from Java 8. In this case it just sums up all the data
                             model ->
                                     toAggregate.stream().mapToDouble(
                                     value -> value.getData().getLatestObservation(landingsColumnName))
                                     .sum(), Double.NaN);

            registerGatherer(specie + " " + earningsColumnName,
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

        for(Specie specie : observed.getSpecies())
        {
            registerGatherer("Biomass " + specie.getName(),
                             state1 -> allSeaTilesAsList.stream().mapToDouble(value -> value.getBiomass(specie)).sum(),
                             0d);
        }


        super.start(state, observed);
    }
}
