package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.Port;
import uk.ac.ox.oxfish.model.data.DataSet;
import uk.ac.ox.oxfish.model.data.IntervalPolicy;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.Markets;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

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

        //get all the ports
        final HashSet<Port> ports = observed.getPorts();

        for(Specie specie : observed.getSpecies()) {
            List<Market> toAggregate = new LinkedList<>();
            //now get for each port, its markets
            for (Port port : ports) {
                final Market market = port.getMarket(specie);
                if(market != null)
                    toAggregate.add(market);


            }
            //now register it!
            final String landingsColumnName = AbstractMarket.LANDINGS_COLUMN_NAME;
            final String earningsColumnName = AbstractMarket.EARNINGS_COLUMN_NAME;
            registerGather(specie + " " + landingsColumnName,
                           //so "stream" is a trick from Java 8. In this case it just sums up all the data
                           model -> toAggregate.stream().mapToDouble(
                                   value -> value.getData().getLatestObservation(landingsColumnName))
                                   .sum(),Double.NaN);

            registerGather(specie  + " " + earningsColumnName,
                           //so "stream" is a trick from Java 8. In this case it just sums up all the data
                           model -> toAggregate.stream().mapToDouble(
                                   value -> value.getData().getLatestObservation(earningsColumnName))
                                   .sum(),Double.NaN);
        }

        final List<Fisher> fishers = state.getFishers();
        //number of fishers
        registerGather("Number of Fishers", ignored -> (double) fishers.size(),0d);
        //fishers who are actually out
        registerGather("Fishers at Sea", ignored -> fishers.stream().mapToDouble(
                value -> value.getLocation().equals(value.getHomePort().getLocation()) ? 0 : 1).sum(),0d);


        super.start(state, observed);
    }
}
