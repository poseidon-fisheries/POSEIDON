package uk.ac.ox.oxfish.model.market.itq;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class that keeps track of who has traded and forbids him to trade for a fixed number of days
 * Created by carrknight on 12/16/15.
 */
public class PenaltyBox implements Steppable{


    /**
     * we keep here all the fishers that need to stay out of trading
     */
    private final HashMap<Fisher,AtomicInteger> penaltyBox = new HashMap<>();


    /**
     * how many days the trader is not allowed to trade further
     */
    private final int duration;


    public PenaltyBox(int duration)
    {
        Preconditions.checkArgument(duration>=0);
        this.duration = duration;
    }


    /**
     * will put the trader in the penalty box. If the trader is already in, it refreshes its penalty duration
     *
     * @param trader trader to put in the box
     */
    public void registerTrader(Fisher trader)
    {
        if(duration > 0)
            penaltyBox.put(trader,new AtomicInteger(duration));
    }


    /**
     * check if the trader is in the penalty box!
     * @param fisher
     * @return
     */
    public boolean has(Fisher fisher){
        return penaltyBox.containsKey(fisher);
    }

    /**
     * decrease duration by 1, removing all traders whose remaining duration is 0
     * @param simState
     */
    @Override
    public void step(SimState simState) {

        if(duration ==0)
            return;


        List<Fisher> toRemove = new LinkedList<>();
        for (Map.Entry<Fisher, AtomicInteger> penitent : penaltyBox.entrySet())
        {
            int remainingDuration = penitent.getValue().decrementAndGet();
            assert remainingDuration>=0;
            if(remainingDuration==0)
                toRemove.add(penitent.getKey());
        }
        penaltyBox.keySet().removeAll(toRemove);

    }
}
