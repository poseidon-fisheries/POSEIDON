package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.NonAssociatedSetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

public class NonAssociatedSetFromLocationValuePlanningModule<B extends LocalBiology>
    extends LocationValuePlanningModule<B> {

    public NonAssociatedSetFromLocationValuePlanningModule(
        final NonAssociatedSetLocationValues locationValues,
        final CatchSamplerPlannedActionGenerator.NonAssociatedActionGenerator<B> generator
    ) {
        super(locationValues, generator);
    }

    public NonAssociatedSetFromLocationValuePlanningModule(
        final NonAssociatedSetLocationValues locationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double additionalWaitTime,
        final CatchSampler<B> sampler,
        final CatchMaker<B> catchMaker,
        final GlobalBiology globalBiology,
        final Class<B> localBiologyClass,
        final boolean canPoachFads,
        final int rangeInSeaTiles
    ) {
        this(
            locationValues,
            new CatchSamplerPlannedActionGenerator.NonAssociatedActionGenerator<B>(
                locationValues,
                map,
                random,
                additionalWaitTime,
                sampler,
                catchMaker,
                globalBiology,
                localBiologyClass,
                canPoachFads,
                rangeInSeaTiles
            )
        );
    }

    @Override
    public int maximumActionsInAPlan(final FishState state, final Fisher fisher) {
        return
            Math.min(
                FadManager.getFadManager(fisher).
                    getNumberOfRemainingYearlyActions(NonAssociatedSetAction.class),
                1000
            );
    }

}