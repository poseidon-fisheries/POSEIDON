package uk.ac.ox.oxfish.geography.ports;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Created by carrknight on 3/13/17.
 */
public class PortListInitializer implements PortInitializer {


    private final LinkedHashMap<String,Coordinate> ports;

    public PortListInitializer(LinkedHashMap<String, Coordinate> ports) {
        this.ports = ports;
        Preconditions.checkArgument(ports.size()>0);
    }

    /**
     * Create and add ports to map, return them as a list.
     * Supposedly this is called during the early scenario setup. The map is built, the biology is built
     * and the marketmap can be built.
     *
     * @param map            the map to place ports in
     * @param mapmakerRandom the randomizer
     * @param marketFactory  a function that returns the market associated with a location. We might refactor this at some point*
     * @param model
     * @return the list of ports that have been built and added to the map. It can be ignored.
     */
    @Override
    public List<Port> buildPorts(
            NauticalMap map, MersenneTwisterFast mapmakerRandom, Function<SeaTile, MarketMap> marketFactory,
            FishState model) {
        List<Port> toReturn = new ArrayList<>(ports.size());
        for(Map.Entry<String,Coordinate> entry : ports.entrySet()) {
            SeaTile location = map.getSeaTile((int) entry.getValue().x,
                                              (int) entry.getValue().y);

            Port newPort = new Port(entry.getKey(),
                              location,
                              marketFactory.apply(location),
                              //ports start with price = 0 because I assume the scenario will have its own rules for gas price

                              0
            );
            toReturn.add(newPort);
            map.addPort(newPort);
        }

        //add data on profits in each port
        for(Port port : toReturn)
        {
            model.getYearlyDataSet().registerGatherer("Average Cash-Flow at " + port.getName(),
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
                                                                                      YearlyFisherTimeSeries.CASH_FLOW_COLUMN);
                                                                          }
                                                                      }).sum() /
                                                                      fishers.size();
                                                          }
                                                      }, Double.NaN);
        }


        return toReturn;
    }
}
