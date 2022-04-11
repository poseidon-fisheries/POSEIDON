package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.OpportunisticFadSetLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

public class FadStealingFromLocationValuePlanningModule extends LocationValuePlanningModule {

    public FadStealingFromLocationValuePlanningModule(
            OpportunisticFadSetLocationValues locationValues,
            FadStealingPlannedActionGenerator generator) {
        super(locationValues, generator);
    }

    public FadStealingFromLocationValuePlanningModule(
            OpportunisticFadSetLocationValues locationValues,
            NauticalMap map, MersenneTwisterFast random,
            double hoursItTakesToSet,
            double hoursWastedIfNoFadAround,
            double minimumFadValueToSteal) {
        this(locationValues,
                new FadStealingPlannedActionGenerator(
                        locationValues,
                        map,
                        random,
                        hoursItTakesToSet,
                        hoursWastedIfNoFadAround,
                        minimumFadValueToSteal
                ));
    }

    @Override
    public int maximumActionsInAPlan(FishState state, Fisher fisher) {
        return
                Math.min(
                        FadManager.getFadManager(fisher).
                                getNumberOfRemainingYearlyActions(
                                        OpportunisticFadSetAction.class),
                        1000);
    }
}
