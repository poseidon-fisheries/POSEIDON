package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.NonAssociatedSetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

public class NonAssociatedSetFromLocationValuePlanningModule extends LocationValuePlanningModule {

    public NonAssociatedSetFromLocationValuePlanningModule(
            NonAssociatedSetLocationValues locationValues,
            CatchSamplerPlannedActionGenerator.NonAssociatedActionGenerator generator) {
        super(locationValues, generator);
    }

    public NonAssociatedSetFromLocationValuePlanningModule(
            NonAssociatedSetLocationValues locationValues,
            NauticalMap map,
            MersenneTwisterFast random,
            double additionalWaitTime,
            CatchSampler<? extends LocalBiology> sampler, final GlobalBiology globalBiology
    ) {
        this(locationValues,
                new CatchSamplerPlannedActionGenerator.NonAssociatedActionGenerator(
                        locationValues,
                        map,
                        random,
                        additionalWaitTime,
                        sampler, globalBiology
                ));
    }

    @Override
    public int maximumActionsInAPlan(FishState state, Fisher fisher) {
        return
                Math.min(
                        FadManager.getFadManager(fisher).
                                getNumberOfRemainingYearlyActions(NonAssociatedSetAction.class),
                        1000);

    }


}
