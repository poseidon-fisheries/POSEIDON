package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.SetLocationValues;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * generates a random dolphin deployment action
 */
public abstract class CatchSamplerPlannedActionGenerator<PA extends PlannedAction> extends
        DrawFromLocationValuePlannedActionGenerator<PA> {


    private final double additionalWaitTime;

    private final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator;

    public CatchSamplerPlannedActionGenerator(SetLocationValues<? extends AbstractSetAction> originalLocationValues,
                                              NauticalMap map, MersenneTwisterFast random,
                                              double additionalWaitTime,
                                              CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator) {
        super(originalLocationValues, map, random);
        this.additionalWaitTime = additionalWaitTime;
        this.howMuchWeCanFishOutGenerator = howMuchWeCanFishOutGenerator;
    }

    @Override
    public PA drawNewPlannedAction() {
        Preconditions.checkState(isReady(),"Did not start the deploy generator yet!");
        return
                turnSeatilePickedIntoAction(
                        howMuchWeCanFishOutGenerator,
                        drawNewLocation(),
                        additionalWaitTime
                );
    }

    abstract public PA turnSeatilePickedIntoAction(
            CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            SeaTile tile,
            double additionalWaitTime
    );


    public static class DolphinActionGenerator extends CatchSamplerPlannedActionGenerator<PlannedAction.DolphinSet> {


        public DolphinActionGenerator(SetLocationValues<? extends AbstractSetAction> originalLocationValues,
                                      NauticalMap map, MersenneTwisterFast random,
                                      double additionalWaitTime, CatchSampler<? extends LocalBiology>
                                              howMuchWeCanFishOutGenerator) {
            super(originalLocationValues, map, random, additionalWaitTime, howMuchWeCanFishOutGenerator);
        }

        @Override
        public PlannedAction.DolphinSet turnSeatilePickedIntoAction(
                CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator, SeaTile tile,
                double additionalWaitTime) {
            return new PlannedAction.DolphinSet(
                    tile,
                    howMuchWeCanFishOutGenerator,
                    additionalWaitTime
            );

        }
    }

    public static class NonAssociatedActionGenerator extends CatchSamplerPlannedActionGenerator<PlannedAction.NonAssociatedSet> {


        public NonAssociatedActionGenerator(SetLocationValues<? extends AbstractSetAction> originalLocationValues,
                                      NauticalMap map, MersenneTwisterFast random,
                                            double additionalWaitTime, CatchSampler<? extends LocalBiology>
                                              howMuchWeCanFishOutGenerator) {
            super(originalLocationValues, map, random, additionalWaitTime, howMuchWeCanFishOutGenerator);
        }

        @Override
        public PlannedAction.NonAssociatedSet turnSeatilePickedIntoAction(CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator, SeaTile tile, double additionalWaitTime) {
            return new PlannedAction.NonAssociatedSet(
                    tile,
                    howMuchWeCanFishOutGenerator,
                    additionalWaitTime
            );

        }
    }

}
