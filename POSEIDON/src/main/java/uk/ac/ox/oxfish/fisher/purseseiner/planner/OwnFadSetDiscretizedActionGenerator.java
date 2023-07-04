package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;

import javax.annotation.Nonnull;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

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

    private final static Comparator<ValuedFad> COMPARATOR =
        (o1, o2) -> -Double.compare(o1.getValue(), o2.getValue());

    private final MapDiscretization discretization;
    /**
     * if a FAD has less than this in value, just ignore it!
     */
    private final double minimumFadValue;
    private NauticalMap map;
    private PriorityQueue<ValuedFad>[] rankedFads;
    /**
     * when this is set to true, the generator will immediately remove all fads that are currently not
     * allowed to be caught. This is set to false because the standard procedure is to wait till the action
     * comes up in the plan before checking its validity.
     */
    private boolean filterOutCurrentlyInvalidFads = false;

    //todo add minimum soaktime

    public OwnFadSetDiscretizedActionGenerator(
        final MapDiscretization discretization,
        final double minimumFadValue
    ) {
        this.discretization = discretization;
        this.minimumFadValue = minimumFadValue;
    }

    /**
     * call this at the beginning of the planning
     *
     * @param fadManager
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void startOrReset(
        FadManager fadManager,
        MersenneTwisterFast random,
        NauticalMap map
    ) {

        this.map = map;
        //if you haven't, discretize!
        if (!discretization.isActive())
            discretization.discretize(map);

        rankedFads = new PriorityQueue[discretization.getNumberOfGroups()];
        for (int i = 0; i < rankedFads.length; i++) {
            rankedFads[i] = new PriorityQueue<>(COMPARATOR);
        }

        //go through all your fads and rank them by profits
        for (Fad fad : fadManager.getDeployedFads()) {
            Fisher fisher = fadManager.getFisher();
            final boolean fadSetAllowed = PlannedAction.FadSet.isFadSetAllowed(
                fisher,
                fadManager,
                fad
            );
            if (filterOutCurrentlyInvalidFads && !fadSetAllowed) {
                continue;
            }
            final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();
            double value = fadManager.getFishValueCalculator().valueOf(fad.getBiology(), prices);

            SeaTile location = fad.getLocation();

            final boolean actionIsSafe =
                fisher.getGear().isSafe(new FadSetAction(fad, fisher, 0));

            if (actionIsSafe && value >= minimumFadValue)
                rankedFads[discretization.getGroup(fad.getLocation())].
                    add(new ValuedFad(fad, value));
        }

    }


    /**
     * returns a list of possible fads so that you may select which to fish on next (the fads are returned with their monetary value,
     * and the integer they are paired with is the group area of the map discretization)
     *
     * @return
     */
    @Nonnull
    public List<Entry<ValuedFad, Integer>> generateBestFadOpportunities() {

        //you may be here asking: "why isn't this a map?".
        //the answer, my friend, is blowing in the wind

        assert rankedFads != null : "not started";
        List<Entry<ValuedFad, Integer>> toReturn = new LinkedList<>();
        //for each group retrieve the best
        for (int group = 0; group < rankedFads.length; group++) {
            if (rankedFads[group].size() > 0)
                toReturn.add(entry(rankedFads[group].peek(), group));
        }
        return toReturn;
    }


    /**
     * returns the value of the "best" fad at this area
     */
    public double getValueOfThisOption(int groupID) {
        if (rankedFads[groupID].size() > 0)
            return rankedFads[groupID].peek().getValue();
        return Double.NaN;
    }

    /**
     * returns a list of all fads, geographically discretized and each are has a queue sorting (descending) all fads in that area
     * by their monetary amount
     *
     * @return
     */
    @Nonnull
    public List<Entry<PriorityQueue<ValuedFad>, Integer>> peekAllFads() {
        assert rankedFads != null : "not started";
        List<Entry<PriorityQueue<ValuedFad>, Integer>> toReturn = new LinkedList<>();
        //for each group retrieve the best
        for (int group = 0; group < rankedFads.length; group++) {
            if (rankedFads[group].size() > 0)
                toReturn.add(entry(rankedFads[group], group));
        }
        return toReturn;
    }


    public PlannedAction.FadSet chooseFad(Integer discretizationGroup) {
        ValuedFad selectedFad = rankedFads[discretizationGroup].poll();
        Preconditions.checkState(selectedFad != null);
        return new PlannedAction.FadSet(selectedFad.getKey());

    }

    public boolean isStarted() {
        return rankedFads != null;
    }

    public int getNumberOfGroups() {
        return rankedFads.length;
    }

    public boolean isFilterOutCurrentlyInvalidFads() {
        return filterOutCurrentlyInvalidFads;
    }

    public void setFilterOutCurrentlyInvalidFads(boolean filterOutCurrentlyInvalidFads) {
        this.filterOutCurrentlyInvalidFads = filterOutCurrentlyInvalidFads;
    }

    public double getMinimumFadValue() {
        return minimumFadValue;
    }

    public static class ValuedFad extends SimpleImmutableEntry<Fad, Double> {
        private static final long serialVersionUID = 1L;

        public ValuedFad(Fad first, Double second) {
            super(first, second);
        }
    }

}
