package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import com.google.common.collect.ImmutableList;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceAggregator;
import uk.ac.ox.oxfish.biology.tuna.Aggregator;
import uk.ac.ox.oxfish.biology.tuna.BiomassAggregator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.*;

public class TargetBiologiesGrabber {

    private final boolean canPoachFromFads;
    private final int rangeInSeaTiles;

    public TargetBiologiesGrabber(final boolean canPoachFromFads, final int rangeInSeaTiles) {
        this.canPoachFromFads = canPoachFromFads;
        this.rangeInSeaTiles = rangeInSeaTiles;
    }

    /**
     * grabs all fad biologies in the area + the local biology
     */
    private static <B extends LocalBiology> List<B> getAllBiologiesHere(final SeaTile tile, final Fisher fisher) {
        final LinkedList<B> biologies = new LinkedList<>();
        biologies.add((B) tile.getBiology());
        final Bag fads = fisher.grabState().getFadMap().fadsAt(tile);
        for (final Object fad : fads) {
            biologies.add((B) ((AbstractFad) fad).getBiology());
        }
        return biologies;
    }

    /**
     * get all moore neighbors's biology if they match the local one at least in type of class
     *
     * @param tile
     * @param fisher
     * @return
     */
    private Collection<? extends LocalBiology> getAllBiologiesInRange(final SeaTile tile, final Fisher fisher) {
        final LocalBiology local = tile.getBiology();

        if (rangeInSeaTiles <= 0 || local instanceof EmptyLocalBiology) {
            return ImmutableList.of(local);
        } else {
            final Set toReturn = new HashSet<>();
            final Bag mooreNeighbors = fisher.grabState().getMap().getMooreNeighbors(tile, rangeInSeaTiles);
            for (final Object mooreNeighbor : mooreNeighbors) {
                final LocalBiology neighborBiology = ((SeaTile) mooreNeighbor).getBiology();
                if (local.getClass().isAssignableFrom(neighborBiology.getClass())) {
                    toReturn.add(neighborBiology);
                }
            }
            return toReturn;

        }
    }

    /**
     * starts with the local biologies; if you can poach from local fads, add those too to the list;
     * if you can poach in range, adds those biologies too.
     *
     * @param fisher
     * @return
     */
    public Collection<LocalBiology> buildListOfCatchableAreas(
        final SeaTile location,
        final Fisher fisher
    ) {
        final Set<LocalBiology> targetsSet =
            new HashSet<>(
                canPoachFromFads ? getAllBiologiesHere(location, fisher) :
                    ImmutableList.of(location.getBiology()));
        if (rangeInSeaTiles > 0)
            targetsSet.addAll(getAllBiologiesInRange(location, fisher));
        final ArrayList<LocalBiology> targets = new ArrayList<>(targetsSet);
        if (targets.size() >= 1)
            Collections.shuffle(targets, new Random(fisher.grabRandomizer().nextLong()));
        return targets;
    }

    /**
     * grabs local biology or local biologies of all the surrounding areas and aggregate them; this way it can
     * target larger stocks in a wider area
     */
    public LocalBiology getLocalBiologiesAndAggregateThem(final SeaTile location, final Fisher fisher) {
        final LocalBiology local = location.getBiology();
        if (rangeInSeaTiles <= 0 || local instanceof EmptyLocalBiology) {
            return local;
        } else {
            final Collection<? extends LocalBiology> toAggregate = getAllBiologiesInRange(location, fisher);
            final Aggregator aggregator =
                local instanceof VariableBiomassBasedBiology ? new BiomassAggregator() : new AbundanceAggregator();
            return (LocalBiology) aggregator.apply(
                fisher.grabState().getBiology(),
                toAggregate
            );


        }
    }


}
