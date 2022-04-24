package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.utility.Pair;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * generates a list of possible fads to set on.
 * The way it works is that it discretizes space, and returns the best in each group.
 * That way we limit the number of actions to plan for <br>
 * The idea is that the planner starts this at the beginning (which gets all Fads sorted),
 * then look for the best in each group (through generateBestFadOpportunities) and then tell the generator which
 * was the selected one (through chooseFad)
 */
@SuppressWarnings("ALL")
public class OwnFadSetDiscretizedActionGenerator {



    private final MapDiscretization discretization;

    private PriorityQueue<ValuedFad>[] rankedFads;

    /**
     * if a FAD has less than this in value, just ignore it!
     */
    private final double minimumFadValue;

    public OwnFadSetDiscretizedActionGenerator(MapDiscretization discretization, double minimumFadValue) {
        this.discretization = discretization;
        this.minimumFadValue = minimumFadValue;
    }


    //todo add minimum soaktime

    private final static Comparator<ValuedFad> COMPARATOR = new Comparator<ValuedFad>() {
        @Override
        public int compare(ValuedFad o1, ValuedFad o2) {
            return - Double.compare(o1.getSecond(),o2.getSecond());
        }
    };


    /**
     * call this at the beginning of the planning
     * @param fadManager
     */
    public void startOrReset(
            FadManager fadManager,
            MersenneTwisterFast random){

        rankedFads = new PriorityQueue[discretization.getNumberOfGroups()];
        for (int i = 0; i < rankedFads.length; i++) {
            rankedFads[i] = new PriorityQueue<>(COMPARATOR);
        }
        //go through all your fads and rank them by profits
        for (Object fad : fadManager.getDeployedFads()) {
            Fad deployedFad = (Fad) fad;
            double value = deployedFad.valueOfFishFor(fadManager.getFisher());
            if(value>=minimumFadValue)
                rankedFads[discretization.getGroup(deployedFad.getLocation())].
                        add(new ValuedFad(deployedFad,value));
        }

    }


    /**
     * returns a list of possible fads so that you may select which to fish on next (the fads are returned with their monetary value,
     * and the integer they are paired with is the group area of the map discretization)
     * @return
     */
    @Nonnull
    public List<Pair<ValuedFad,Integer>> generateBestFadOpportunities(){
        assert rankedFads != null : "not started";
        List<Pair<ValuedFad,Integer>> toReturn = new LinkedList<>();
        //for each group retrieve the best
        for (int group = 0; group < rankedFads.length; group++) {
            if(rankedFads[group].size()>0)
                toReturn.add(new Pair<>(rankedFads[group].peek(),group));
        }
        return toReturn;
    }

    public PlannedAction.FadSet chooseFad(Integer discretizationGroup){
        ValuedFad selectedFad = rankedFads[discretizationGroup].poll();
        Preconditions.checkState(selectedFad!=null);
        return new PlannedAction.FadSet(selectedFad.getFirst());

    }
    public boolean isStarted(){
        return rankedFads != null;
    }

    public static class ValuedFad extends Pair<Fad,Double> {

        public ValuedFad(Fad first, Double second) {
            super(first, second);
        }
    }

    public int getNumberOfGroups(){
        return rankedFads.length;
    }
}
