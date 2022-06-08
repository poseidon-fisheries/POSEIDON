package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbundanceCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.BiomassCatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * generates a random dolphin deployment action
 */
public abstract class CatchSamplerPlannedActionGenerator<PA extends PlannedAction> extends
        DrawFromLocationValuePlannedActionGenerator<PA> {


    private final double additionalWaitTime;

    private final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator;

    private final CatchMaker<? extends LocalBiology> catchMaker;

    private final GlobalBiology biology;

    public CatchSamplerPlannedActionGenerator(SetLocationValues<? extends AbstractSetAction> originalLocationValues,
                                              NauticalMap map, MersenneTwisterFast random,
                                              double additionalWaitTime,
                                              CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                                              GlobalBiology biology) {
        super(originalLocationValues, map, random);
        this.additionalWaitTime = additionalWaitTime;
        this.howMuchWeCanFishOutGenerator = howMuchWeCanFishOutGenerator;
        this.biology = biology;
        if(FishStateUtilities.guessIfBiologyIsBiomassOnly(biology))
            catchMaker=new BiomassCatchMaker(biology);
        else
            catchMaker=new AbundanceCatchMaker(biology);
    }

    @Override
    public PA drawNewPlannedAction() {
        Preconditions.checkState(isReady(),"Did not start the deploy generator yet!");
        return
                turnSeatilePickedIntoAction(
                        howMuchWeCanFishOutGenerator,
                        drawNewLocation(),
                        additionalWaitTime,
                        catchMaker,
                        biology);
    }

    abstract public PA turnSeatilePickedIntoAction(
            CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            SeaTile tile,
            double additionalWaitTime,
            CatchMaker<? extends LocalBiology> catchMaker,
            GlobalBiology biology);


    public static class DolphinActionGenerator extends CatchSamplerPlannedActionGenerator<PlannedAction.DolphinSet> {


        public DolphinActionGenerator(SetLocationValues<? extends AbstractSetAction> originalLocationValues,
                                      NauticalMap map, MersenneTwisterFast random,
                                      double additionalWaitTime, CatchSampler<? extends LocalBiology>
                                              howMuchWeCanFishOutGenerator, final GlobalBiology globalBiology) {
            super(originalLocationValues, map, random, additionalWaitTime, howMuchWeCanFishOutGenerator,
                  globalBiology);
        }

        @Override
        public PlannedAction.DolphinSet turnSeatilePickedIntoAction(
                CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator, SeaTile tile,
                double additionalWaitTime, CatchMaker<? extends LocalBiology> catchMaker, GlobalBiology biology) {
            return new PlannedAction.DolphinSet(
                    tile,
                    howMuchWeCanFishOutGenerator,catchMaker ,
                    additionalWaitTime);

        }
    }

    public static class NonAssociatedActionGenerator extends CatchSamplerPlannedActionGenerator<PlannedAction.NonAssociatedSet> {

        private final boolean canPoachFads;

        public NonAssociatedActionGenerator(SetLocationValues<? extends AbstractSetAction> originalLocationValues,
                                            NauticalMap map, MersenneTwisterFast random,
                                            double additionalWaitTime, CatchSampler<? extends LocalBiology>
                                              howMuchWeCanFishOutGenerator,
                                            final GlobalBiology globalBiology,
                                            boolean canPoachFads) {
            super(originalLocationValues, map, random, additionalWaitTime, howMuchWeCanFishOutGenerator,
                  globalBiology);
            this.canPoachFads = canPoachFads;
        }

        @Override
        public PlannedAction.NonAssociatedSet turnSeatilePickedIntoAction(CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator, SeaTile tile, double additionalWaitTime,
                                                                          CatchMaker<? extends LocalBiology> catchMaker,
                                                                          GlobalBiology biology) {
            return new PlannedAction.NonAssociatedSet(
                    tile,
                    howMuchWeCanFishOutGenerator,catchMaker ,
                    additionalWaitTime,
                    canPoachFads);

        }
    }

}
