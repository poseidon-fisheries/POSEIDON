package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.OpportunisticFadSetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;

public class FadStealingPlannedActionGenerator<B extends LocalBiology> extends
    DrawFromLocationValuePlannedActionGenerator<PlannedAction.OpportunisticFadSet, B> {

    /**
     * the time it takes to set if something is found
     */
    private final double hoursItTakesToSet;

    /**
     * time spent looking fruitlessly for a FAD when there is none around!
     */
    private final double hoursWastedIfNoFadAround;

    private final double minimumFadValueToSteal;

    public FadStealingPlannedActionGenerator(
        final OpportunisticFadSetLocationValues originalLocationValues,
        final NauticalMap map, final MersenneTwisterFast random,
        final double hoursItTakesToSet,
        final double hoursWastedIfNoFadAround,
        final double minimumFadValueToSteal
    ) {
        super(originalLocationValues, map, random);
        this.hoursItTakesToSet = hoursItTakesToSet;
        this.hoursWastedIfNoFadAround = hoursWastedIfNoFadAround;
        this.minimumFadValueToSteal = minimumFadValueToSteal;
    }

    @Override
    public PlannedAction.OpportunisticFadSet drawNewPlannedAction() {
        return new PlannedAction.OpportunisticFadSet(
            drawNewLocation(),
            hoursItTakesToSet,
            hoursWastedIfNoFadAround,
            minimumFadValueToSteal
        );
    }
}
