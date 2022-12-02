package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceLinearIntervalAttractor;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class AbundanceLinearIntervalInitializerFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>, PluggableSelectivity {

    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();
    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);

    private LinkedHashMap<String, Double> carryingCapacityPerSpecies = new LinkedHashMap<>();
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter daysItTakesToFillUp = new FixedDoubleParameter(30);
    private DoubleParameter minAbundanceThreshold = new FixedDoubleParameter(100);

    {
        carryingCapacityPerSpecies.put("Species 0", 100000d);
    }

    @Override
    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        final double probabilityOfFadBeingDud = fadDudRate.apply(fishState.getRandom());
        final DoubleSupplier capacityGenerator;
        if (Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud == 0)
            capacityGenerator = () -> Double.MAX_VALUE;
        else
            capacityGenerator = () -> {
                if (fishState.getRandom().nextFloat() <= probabilityOfFadBeingDud)
                    return 0;
                else
                    return Double.MAX_VALUE;
            };

        final double[] carryingCapacities = new double[fishState.getBiology().getSize()];
        for (final Map.Entry<String, Double> carrying : carryingCapacityPerSpecies.entrySet()) {
            carryingCapacities[fishState.getSpecies(carrying.getKey()).getIndex()] = carrying.getValue();
        }

        return new AbundanceFadInitializer(
            fishState.getBiology(),
            capacityGenerator,
            new AbundanceLinearIntervalAttractor(
                daysInWaterBeforeAttraction.apply(fishState.getRandom()).intValue(),
                daysItTakesToFillUp.apply(fishState.getRandom()).intValue(),
                carryingCapacities,
                minAbundanceThreshold.apply(fishState.getRandom()),
                selectivityFilters,
                fishState

            ),
            fishReleaseProbabilityInPercent.apply(fishState.getRandom()) / 100d,
            fishState::getStep
        );
    }

    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        return selectivityFilters;
    }

    public void setSelectivityFilters(final Map<Species, NonMutatingArrayFilter> selectivityFilters) {
        this.selectivityFilters = selectivityFilters;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(final DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(final DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityPerSpecies() {
        return carryingCapacityPerSpecies;
    }

    public void setCarryingCapacityPerSpecies(final LinkedHashMap<String, Double> carryingCapacityPerSpecies) {
        this.carryingCapacityPerSpecies = carryingCapacityPerSpecies;
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
