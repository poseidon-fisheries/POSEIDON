package uk.ac.ox.oxfish.geography.fads;

import ec.util.MersenneTwisterFast;
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

import java.util.Map;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.processSpeciesNameToDoubleParameterMap;

public class AbundanceLinearIntervalInitializerFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad>> {

    private CarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> carryingCapacityInitializerFactory;

    private AbundanceFiltersFactory abundanceFiltersFactory;
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter daysItTakesToFillUp = new FixedDoubleParameter(30);
    private DoubleParameter minAbundanceThreshold = new FixedDoubleParameter(100);
    private Map<String, DoubleParameter> fishReleaseProbabilities;

    public AbundanceLinearIntervalInitializerFactory() {
    }

    public AbundanceLinearIntervalInitializerFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Map<String, DoubleParameter> fishReleaseProbabilities
    ) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
        this.fishReleaseProbabilities = fishReleaseProbabilities;
    }

    public Map<String, DoubleParameter> getFishReleaseProbabilities() {
        return fishReleaseProbabilities;
    }

    public void setFishReleaseProbabilities(final Map<String, DoubleParameter> fishReleaseProbabilities) {
        this.fishReleaseProbabilities = fishReleaseProbabilities;
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
        final MersenneTwisterFast rng = fishState.getRandom();
        final AbundanceLinearIntervalAttractor abundanceLinearIntervalAttractor = new AbundanceLinearIntervalAttractor(
            (int) daysInWaterBeforeAttraction.applyAsDouble(rng),
            (int) daysItTakesToFillUp.applyAsDouble(rng),
            carryingCapacityInitializer.apply(rng).getCarryingCapacities(),
            minAbundanceThreshold.applyAsDouble(rng),
            abundanceFiltersFactory.apply(fishState).get(FadSetAction.class),
            fishState
        );
        return new AbundanceAggregatingFadInitializer(
            fishState.getBiology(),
            abundanceLinearIntervalAttractor,
            fishState::getStep,
            carryingCapacityInitializer,
            processSpeciesNameToDoubleParameterMap(
                getFishReleaseProbabilities(),
                fishState.getBiology(),
                rng
            )
        );
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
