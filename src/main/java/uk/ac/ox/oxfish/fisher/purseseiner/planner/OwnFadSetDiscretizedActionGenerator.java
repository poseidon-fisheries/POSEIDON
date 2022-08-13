package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
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

    /**
     * if some bounds are provided, within them you will decide not to fish
     */
    private double[] bannedGridYBounds;

    /**
     * if some bounds are provided, within them you will decide not to fish
     */
    private double[] bannedGridXBounds;

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
            MersenneTwisterFast random,
            NauticalMap map){

        //if you haven't, discretize!
        if(!discretization.isActive())
            discretization.discretize(map);

        rankedFads = new PriorityQueue[discretization.getNumberOfGroups()];
        for (int i = 0; i < rankedFads.length; i++) {
            rankedFads[i] = new PriorityQueue<>(COMPARATOR);
        }
        //go through all your fads and rank them by profits
        for (Object fad : fadManager.getDeployedFads()) {
            AbstractFad deployedFad = (AbstractFad) fad;
            double value = deployedFad.valueOfFishFor(fadManager.getFisher());
            SeaTile location = ((AbstractFad<?, ?>) fad).getLocation();
            if(bannedGridYBounds != null &&
                    location.getGridY()>= bannedGridYBounds[0] &&
                    location.getGridY()<=bannedGridYBounds[1] &&
                    location.getGridX()>= bannedGridXBounds[0] &&
                    location.getGridX()<=bannedGridXBounds[1])
                continue;
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

    /**
     * returns a list of all fads, geographically discretized and each are has a queue sorting (descending) all fads in that area
     * by their monetary amount
     * @return
     */
    @Nonnull
    public List<Pair<PriorityQueue<ValuedFad>,Integer>> peekAllFads(){
        assert rankedFads != null : "not started";
        List<Pair<PriorityQueue<ValuedFad>,Integer>> toReturn = new LinkedList<>();
        //for each group retrieve the best
        for (int group = 0; group < rankedFads.length; group++) {
            if(rankedFads[group].size()>0)
                toReturn.add(new Pair<>(rankedFads[group],group));
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

    public static class ValuedFad extends Pair<AbstractFad,Double> {

        public ValuedFad(AbstractFad first, Double second) {
            super(first, second);
        }
    }

    public int getNumberOfGroups(){
        return rankedFads.length;
    }


    public double[] getBannedGridYBounds() {
        return bannedGridYBounds;
    }
    public void setBannedGridBounds(double[] bannedGridYBounds,
                                     double[] bannedGridXBounds) {
        Preconditions.checkArgument(bannedGridXBounds.length==2);
        Preconditions.checkArgument(bannedGridYBounds.length==2);
        Preconditions.checkArgument(bannedGridYBounds[0]<=bannedGridYBounds[1]);
        Preconditions.checkArgument(bannedGridXBounds[0]<=bannedGridXBounds[1]);
        this.bannedGridYBounds = bannedGridYBounds;
        this.bannedGridXBounds = bannedGridXBounds;
    }

    public double[] getBannedGridXBounds() {
        return bannedGridXBounds;
    }
}
