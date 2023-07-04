package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.OpportunisticFadSetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

@SuppressWarnings("rawtypes")
public class FadStealingFromLocationValuePlanningModule<B extends LocalBiology>
    extends LocationValuePlanningModule<B> {

    public FadStealingFromLocationValuePlanningModule(
        final OpportunisticFadSetLocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double hoursItTakesToSet,
        final double hoursWastedIfNoFadAround,
        final double minimumFadValueToSteal
    ) {
        this(
            locationValues,
            new FadStealingPlannedActionGenerator<>(
                locationValues,
                map,
                random,
                hoursItTakesToSet,
                hoursWastedIfNoFadAround,
                minimumFadValueToSteal
            )
        );
    }

    public FadStealingFromLocationValuePlanningModule(
        final OpportunisticFadSetLocationValues locationValues,
        final FadStealingPlannedActionGenerator<B> generator
    ) {
        super(locationValues, generator);
    }

    @Override
    public int maximumActionsInAPlan(final FishState state, final Fisher fisher) {
        return
            Math.min(
                FadManager.getFadManager(fisher).
                    getNumberOfRemainingYearlyActions(
                        OpportunisticFadSetAction.class),
                1000
            );
    }
}
