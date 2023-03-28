package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.utility.Pair;

import javax.annotation.Nonnull;
import java.util.*;

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


    private final static Comparator<ValuedFad> COMPARATOR = new Comparator<ValuedFad>() {
        @Override
        public int compare(ValuedFad o1, ValuedFad o2) {
            return -Double.compare(o1.getSecond(), o2.getSecond());
        }
    };
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

    //Set this value higher to allow vessels to set closer to the equator
    private double maxAllowableShear = 0.90;

    //todo add minimum soaktime

    public OwnFadSetDiscretizedActionGenerator(MapDiscretization discretization, double minimumFadValue) {
        this.discretization = discretization;
        this.minimumFadValue = minimumFadValue;
    }

    /**
     * call this at the beginning of the planning
     *
     * @param fadManager
     */
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

        final Optional<DoubleGrid2D> maxShearMap = Optional
            .ofNullable(map.getAdditionalMaps().get("Shear"))
            .map(Supplier::get);

        //go through all your fads and rank them by profits
        for (Object fad : fadManager.getDeployedFads()) {
            AbstractFad deployedFad = (AbstractFad) fad;
            Fisher fisher = fadManager.getFisher();
            final boolean fadSetAllowed = PlannedAction.FadSet.isFadSetAllowed(
                fisher,
                fadManager,
                deployedFad
            );
            if (filterOutCurrentlyInvalidFads && !fadSetAllowed) {
                continue;
            }
            final double[] prices = fisher.getHomePort().getMarketMap(fisher).getPrices();
            double value = fadManager.getFishValueCalculator().valueOf(deployedFad.getBiology(), prices);

            SeaTile location = ((AbstractFad<?, ?>) fad).getLocation();

            // Check that the shear at this location is not too high, otherwise we'll have to skip the FAD
            final boolean shearIsSafe = maxShearMap
                .map(msm -> msm.get(location.getGridX(), location.getGridY()) <= maxAllowableShear)
                .orElse(true);

            //This is where we want to artificially inflate the value to minimumFadValue
            //if they make a bad reading. Why was this removed before?
            if (shearIsSafe && value >= minimumFadValue)
                rankedFads[discretization.getGroup(deployedFad.getLocation())].
                    add(new ValuedFad(deployedFad, value));
        }

    }


    /**
     * returns a list of possible fads so that you may select which to fish on next (the fads are returned with their monetary value,
     * and the integer they are paired with is the group area of the map discretization)
     *
     * @return
     */
    @Nonnull
    public List<Pair<ValuedFad, Integer>> generateBestFadOpportunities() {

        //you may be here asking: "why isn't this a map?".
        //the answer, my friend, is blowing in the wind

        assert rankedFads != null : "not started";
        List<Pair<ValuedFad, Integer>> toReturn = new LinkedList<>();
        //for each group retrieve the best
        for (int group = 0; group < rankedFads.length; group++) {
            if (rankedFads[group].size() > 0)
                toReturn.add(new Pair<>(rankedFads[group].peek(), group));
        }
        return toReturn;
    }


    /**
     * returns the value of the "best" fad at this area
     */
    public double getValueOfThisOption(int groupID) {
        if (rankedFads[groupID].size() > 0)
            return rankedFads[groupID].peek().getSecond();
        return Double.NaN;
    }

    /**
     * returns a list of all fads, geographically discretized and each are has a queue sorting (descending) all fads in that area
     * by their monetary amount
     *
     * @return
     */
    @Nonnull
    public List<Pair<PriorityQueue<ValuedFad>, Integer>> peekAllFads() {
        assert rankedFads != null : "not started";
        List<Pair<PriorityQueue<ValuedFad>, Integer>> toReturn = new LinkedList<>();
        //for each group retrieve the best
        for (int group = 0; group < rankedFads.length; group++) {
            if (rankedFads[group].size() > 0)
                toReturn.add(new Pair<>(rankedFads[group], group));
        }
        return toReturn;
    }


    public PlannedAction.FadSet chooseFad(Integer discretizationGroup) {
        ValuedFad selectedFad = rankedFads[discretizationGroup].poll();
        Preconditions.checkState(selectedFad != null);
        return new PlannedAction.FadSet(selectedFad.getFirst());

    }

    public boolean isStarted() {
        return rankedFads != null;
    }

    public int getNumberOfGroups() {
        return rankedFads.length;
    }

    public double getMaxAllowableShear() {
        return maxAllowableShear;
    }

    public void setMaxAllowableShear(double maxAllowableShear) {
        this.maxAllowableShear = maxAllowableShear;
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

    public static class ValuedFad extends Pair<AbstractFad, Double> {

        public ValuedFad(AbstractFad first, Double second) {
            super(first, second);
        }
    }

}
