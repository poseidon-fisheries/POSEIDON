package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.OpportunisticFadSetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.OFS;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadStealingFromLocationValuePlanningModule<B extends LocalBiology>
    extends LocationValuePlanningModule<B> {

    FadStealingFromLocationValuePlanningModule(
        final OpportunisticFadSetLocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double hoursItTakesToSet,
        final double hoursWastedIfNoFadAround,
        final double minimumFadValueToSteal,
        final double probabilityOfFindingOtherFads
    ) {
        this(
            locationValues,
            new FadStealingPlannedActionGenerator(
                locationValues,
                map,
                random,
                hoursItTakesToSet,
                hoursWastedIfNoFadAround,
                minimumFadValueToSteal,
                probabilityOfFindingOtherFads
            )
        );
    }

    private FadStealingFromLocationValuePlanningModule(
        final OpportunisticFadSetLocationValues locationValues,
        final FadStealingPlannedActionGenerator generator
    ) {
        super(locationValues, generator);
    }

    @Override
    public int maximumActionsInAPlan(
        final FishState state,
        final Fisher fisher
    ) {
        return getFadManager(fisher).numberOfPermissibleActions(
            OFS, 1000,
            state.getRegulations()
        );
    }
}
