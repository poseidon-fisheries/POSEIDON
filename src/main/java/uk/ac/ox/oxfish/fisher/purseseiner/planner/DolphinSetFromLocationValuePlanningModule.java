package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DolphinSetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

public class DolphinSetFromLocationValuePlanningModule<B extends LocalBiology>
    extends LocationValuePlanningModule<B> {

    public DolphinSetFromLocationValuePlanningModule(
        final DolphinSetLocationValues locationValues,
        final CatchSamplerPlannedActionGenerator.DolphinActionGenerator generator
    ) {
        super(locationValues, generator);
    }

    public DolphinSetFromLocationValuePlanningModule(
        final DolphinSetLocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double additionalWaitTime,
        final CatchSampler<B> sampler,
        final CatchMaker<B> catchMaker,
        final GlobalBiology globalBiology,
        final Class<B> localBiologyClass,
        final int rangeInSeatiles
    ) {
        super(
            locationValues,
            new CatchSamplerPlannedActionGenerator.DolphinActionGenerator<>(
                locationValues,
                map,
                random,
                additionalWaitTime,
                sampler,
                catchMaker,
                globalBiology,
                localBiologyClass,
                rangeInSeatiles
            )
        );
    }

    @Override
    public int maximumActionsInAPlan(final FishState state, final Fisher fisher) {
        return
            Math.min(
                FadManager.getFadManager(fisher).
                    getNumberOfRemainingYearlyActions(DolphinSetAction.class),
                1000
            );

    }
}
