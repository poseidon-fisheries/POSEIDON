package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A list of strategies. All have to return true or you don't depart
 * Created by carrknight on 9/11/15.
 */
public class CompositeDepartingStrategy implements DepartingStrategy
{


    final DepartingStrategy[] strategies;


    public CompositeDepartingStrategy(DepartingStrategy... strategies) {
        this.strategies = strategies;
    }


    @Override
    public void turnOff(Fisher fisher) {
        for(DepartingStrategy strategy : strategies)
            strategy.turnOff(fisher);
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        for(DepartingStrategy strategy : strategies)
            strategy.start(model, fisher);
    }

    /**
     * All the given strategies must return true for the fisher to go out
     *

     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(
            Fisher fisher, FishState model, MersenneTwisterFast random) {
        for(DepartingStrategy strategy : strategies)
            if(!strategy.shouldFisherLeavePort(fisher, model, model.getRandom()))
                return false;
        return true;
    }
}
