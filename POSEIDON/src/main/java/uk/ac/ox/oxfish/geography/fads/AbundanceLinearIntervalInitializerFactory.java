package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceAggregatingFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceLinearIntervalAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.CarryingCapacityInitializer;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.PerSpeciesCarryingCapacity;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class AbundanceLinearIntervalInitializerFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad>> {

    private CarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> carryingCapacityInitializerFactory;

    private AbundanceFiltersFactory abundanceFiltersFactory;
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter daysItTakesToFillUp = new FixedDoubleParameter(30);
    private DoubleParameter minAbundanceThreshold = new FixedDoubleParameter(100);


    public AbundanceLinearIntervalInitializerFactory() {
    }

    public AbundanceLinearIntervalInitializerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory
    ) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> apply(final FishState fishState) {
        // This is all a bit awkward because the AbundanceLinearIntervalAttractor wants
        // to know the carrying capacities up-front, but the AbundanceAggregatingFadInitializer
        // also wants a CarryingCapacityInitializer to init its carrying capacities, which will
        // be gleefully ignored by the AbundanceLinearIntervalAttractor. Needs more cleanup.
        final CarryingCapacityInitializer<PerSpeciesCarryingCapacity> carryingCapacityInitializer =
            carryingCapacityInitializerFactory.apply(fishState);
        final AbundanceLinearIntervalAttractor abundanceLinearIntervalAttractor = new AbundanceLinearIntervalAttractor(
            (int) daysInWaterBeforeAttraction.applyAsDouble(fishState.getRandom()),
            (int) daysItTakesToFillUp.applyAsDouble(fishState.getRandom()),
            carryingCapacityInitializer.apply(fishState.getRandom()).getCarryingCapacities(),
            minAbundanceThreshold.applyAsDouble(fishState.getRandom()),
            abundanceFiltersFactory.apply(fishState).get(FadSetAction.class),
            fishState
        );
        return new AbundanceAggregatingFadInitializer(
            fishState.getBiology(),
            abundanceLinearIntervalAttractor,
            fishReleaseProbabilityInPercent.applyAsDouble(fishState.getRandom()) / 100d,
            fishState::getStep,
            carryingCapacityInitializer
        );
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(final DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getDaysItTakesToFillUp() {
        return daysItTakesToFillUp;
    }

    public void setDaysItTakesToFillUp(final DoubleParameter daysItTakesToFillUp) {
        this.daysItTakesToFillUp = daysItTakesToFillUp;
    }

    public DoubleParameter getMinAbundanceThreshold() {
        return minAbundanceThreshold;
    }

    public void setMinAbundanceThreshold(final DoubleParameter minAbundanceThreshold) {
        this.minAbundanceThreshold = minAbundanceThreshold;
    }
}
