package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DolphinSetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

public class DolphinSetFromLocationValuePlanningModule extends LocationValuePlanningModule {


    public DolphinSetFromLocationValuePlanningModule(
            DolphinSetLocationValues locationValues,
            CatchSamplerPlannedActionGenerator.DolphinActionGenerator generator) {
        super(locationValues, generator);
    }

    public DolphinSetFromLocationValuePlanningModule(
            DolphinSetLocationValues locationValues,
            NauticalMap map,
            MersenneTwisterFast random,
            double additionalWaitTime,
            CatchSampler<? extends LocalBiology> sampler,
            final GlobalBiology globalBiology,
            int rangeInSeatiles) {
        super(locationValues,
                new CatchSamplerPlannedActionGenerator.DolphinActionGenerator(
                        locationValues,
                        map,
                        random,
                        additionalWaitTime,
                        sampler, globalBiology,
                        rangeInSeatiles
                        ));
    }

    @Override
    public int maximumActionsInAPlan(FishState state, Fisher fisher) {
       return
                Math.min(
                        FadManager.getFadManager(fisher).
                                getNumberOfRemainingYearlyActions(DolphinSetAction.class),
                        1000);

    }
}
