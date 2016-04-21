package uk.ac.ox.oxfish.model.market.itq;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.HashMap;

/**
 * A utility class that keeps track of who has traded and forbids him to trade for a fixed number of days
 * Created by carrknight on 12/16/15.
 */
public class PenaltyBox implements Steppable{


    /**
     * we keep here all the fishers that need to stay out of trading
     */
    private final HashMap<Fisher,Integer> penaltyBox = new HashMap<>();


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
            penaltyBox.put(trader,duration);
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

        //List<Fisher> toRemove = new LinkedList<>();
        for (Fisher fisher : penaltyBox.keySet())
        {
            penaltyBox.merge(fisher,0,(c,one)-> c - 1);
        }
        penaltyBox.entrySet().removeIf(entry -> entry.getValue()<=0);

    }
}
