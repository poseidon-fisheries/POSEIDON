package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * generates a random dolphin deployment action
 */
public abstract class CatchSamplerPlannedActionGenerator<PA extends PlannedAction, B extends LocalBiology> extends
    DrawFromLocationValuePlannedActionGenerator<PA, B> {

    private final double additionalWaitTime;

    private final CatchSampler<B> howMuchWeCanFishOutGenerator;

    private final CatchMaker<B> catchMaker;

    private final GlobalBiology biology;
    private final Class<B> localBiologyClass;

    public CatchSamplerPlannedActionGenerator(
        final SetLocationValues<? extends AbstractSetAction> originalLocationValues,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final double additionalWaitTime,
        final CatchSampler<B> howMuchWeCanFishOutGenerator,
        final CatchMaker<B> catchMaker,
        final GlobalBiology biology,
        final Class<B> localBiologyClass
    ) {
        super(originalLocationValues, map, random);
        this.additionalWaitTime = additionalWaitTime;
        this.howMuchWeCanFishOutGenerator = howMuchWeCanFishOutGenerator;
        this.catchMaker = catchMaker;
        this.biology = biology;
        this.localBiologyClass = localBiologyClass;
    }

    @Override
    public PA drawNewPlannedAction() {
        Preconditions.checkState(isReady(), "Did not start the deploy generator yet!");
        return
            turnSeatilePickedIntoAction(
                howMuchWeCanFishOutGenerator,
                drawNewLocation(),
                additionalWaitTime,
                catchMaker,
                biology,
                localBiologyClass
            );
    }

    abstract public PA turnSeatilePickedIntoAction(
        CatchSampler<B> howMuchWeCanFishOutGenerator,
        SeaTile tile,
        double additionalWaitTime,
        CatchMaker<B> catchMaker,
        GlobalBiology biology,
        Class<B> localBiologyClass
    );


    public static class DolphinActionGenerator<B extends LocalBiology>
        extends CatchSamplerPlannedActionGenerator<PlannedAction.DolphinSet<B>, B> {

        private final int rangeInSeaTiles;


        public DolphinActionGenerator(
            final SetLocationValues<DolphinSetAction> originalLocationValues,
            final NauticalMap map,
            final MersenneTwisterFast random,
            final double additionalWaitTime,
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final CatchMaker<B> catchMaker,
            final GlobalBiology globalBiology,
            final Class<B> localBiologyClass,
            final int rangeInSeaTiles
        ) {
            super(
                originalLocationValues,
                map,
                random,
                additionalWaitTime,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                globalBiology,
                localBiologyClass
            );
            this.rangeInSeaTiles = rangeInSeaTiles;
        }

        @Override
        public PlannedAction.DolphinSet<B> turnSeatilePickedIntoAction(
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final SeaTile tile,
            final double additionalWaitTime,
            final CatchMaker<B> catchMaker,
            final GlobalBiology biology,
            final Class<B> localBiologyClass
        ) {
            return new PlannedAction.DolphinSet<>(
                tile,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                additionalWaitTime,
                false,
                rangeInSeaTiles,
                localBiologyClass
            );
        }
    }

    public static class NonAssociatedActionGenerator<B extends LocalBiology>
        extends CatchSamplerPlannedActionGenerator<PlannedAction.NonAssociatedSet<B>, B> {

        private final boolean canPoachFads;

        private final int rangeInSeaTiles;

        public NonAssociatedActionGenerator(
            final SetLocationValues<NonAssociatedSetAction> originalLocationValues,
            final NauticalMap map,
            final MersenneTwisterFast random,
            final double additionalWaitTime,
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final CatchMaker<B> catchMaker,
            final GlobalBiology globalBiology,
            final Class<B> localBiologyClass,
            final boolean canPoachFads,
            final int rangeInSeaTiles
        ) {
            super(
                originalLocationValues,
                map,
                random,
                additionalWaitTime,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                globalBiology,
                localBiologyClass
            );
            this.canPoachFads = canPoachFads;
            this.rangeInSeaTiles = rangeInSeaTiles;
        }

        @Override
        public PlannedAction.NonAssociatedSet<B> turnSeatilePickedIntoAction(
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final SeaTile tile,
            final double additionalWaitTime,
            final CatchMaker<B> catchMaker,
            final GlobalBiology biology,
            final Class<B> localBiologyClass
        ) {
            return new PlannedAction.NonAssociatedSet<>(
                tile,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                additionalWaitTime,
                canPoachFads,
                rangeInSeaTiles,
                localBiologyClass
            );
        }
    }
}
