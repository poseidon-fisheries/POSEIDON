package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.PluggableSelectivity;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.LinearEnvironmentalAttractorFactory.createEnvironmentalPenaltyAndStartEnvironmentalMaps;

/**
 * like the linear catchability weibull attractor, but this one sets catchability to 0 whenever clorophill is below a specific value
 */
public class WeibullCatchabilitySelectivityEnvironmentalAttractorFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>, PluggableSelectivity {

    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker =
        new Locker<>();
    private DoubleParameter fishValueCalculatorStandardDeviation = new FixedDoubleParameter(0);
    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();
    private LinkedHashMap<String, Double> carryingCapacityShapeParameters = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> carryingCapacityScaleParameters = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> catchabilities = new LinkedHashMap<>();
    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private LinkedList<AdditionalMapFactory> environmentalMaps = new LinkedList<>();
    private LinkedList<DoubleParameter> environmentalThresholds = new LinkedList<>();
    private LinkedList<DoubleParameter> environmentalPenalties = new LinkedList<>();

    {
        carryingCapacityShapeParameters.put("Species 0", 0.5d);
    }

    {
        carryingCapacityScaleParameters.put("Species 0", 100000d);
    }

    {
        catchabilities.put("Species 0", 0.001d);
    }

    {
        final AdditionalMapFactory e = new AdditionalMapFactory();
        environmentalMaps.add(e);
        environmentalThresholds.add(new FixedDoubleParameter(0.15));
        environmentalPenalties.add(new FixedDoubleParameter(2));
    }

    public WeibullCatchabilitySelectivityEnvironmentalAttractorFactory() {
    }

    public WeibullCatchabilitySelectivityEnvironmentalAttractorFactory(
        final LinkedHashMap<String, Double> carryingCapacityShapeParameters,
        final LinkedHashMap<String, Double> carryingCapacityScaleParameters,
        final LinkedHashMap<String, Double> catchabilities,
        final DoubleParameter fadDudRate,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter maximumDaysAttractions,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final LinkedList<AdditionalMapFactory> environmentalMaps,
        final LinkedList<DoubleParameter> environmentalThresholds,
        final LinkedList<DoubleParameter> environmentalPenalties
    ) {
        this.carryingCapacityShapeParameters = carryingCapacityShapeParameters;
        this.carryingCapacityScaleParameters = carryingCapacityScaleParameters;
        this.catchabilities = catchabilities;
        this.fadDudRate = fadDudRate;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.maximumDaysAttractions = maximumDaysAttractions;
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
        this.environmentalMaps = environmentalMaps;
        this.environmentalThresholds = environmentalThresholds;
        this.environmentalPenalties = environmentalPenalties;
    }

    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
            fishState,
            new Supplier<AbundanceFadInitializer>() {
                @Override
                public AbundanceFadInitializer get() {


                    //attractor:
                    final MersenneTwisterFast rng = fishState.getRandom();
                    final double probabilityOfFadBeingDud = fadDudRate.apply(rng);
                    final DoubleSupplier capacityGenerator;
                    if (Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud == 0)
                        capacityGenerator = () -> Double.MAX_VALUE;
                    else
                        capacityGenerator = () -> {
                            if (rng.nextFloat() <= probabilityOfFadBeingDud)
                                return 0;
                            else
                                return Double.MAX_VALUE;
                        };

                    final GlobalBiology globalBiology = fishState.getBiology();
                    final DoubleParameter[] carryingCapacities = new DoubleParameter[globalBiology.getSize()];
                    //double[] maxCatchability = new double[fishState.getBiology().getSize()];
                    // double[] cachabilityOtherwise = new double[fishState.getBiology().getSize()];
                    for (final Species species : globalBiology.getSpecies()) {
                        carryingCapacities[species.getIndex()] =
                            carryingCapacityScaleParameters.containsKey(species.getName()) ?
                                new WeibullDoubleParameter(
                                    carryingCapacityShapeParameters.get(species.getName()),
                                    carryingCapacityScaleParameters.get(species.getName())
                                ) : new FixedDoubleParameter(-1);


                    }
                    final Function<SeaTile, Double> finalCatchabilityPenaltyFunction = createEnvironmentalPenaltyAndStartEnvironmentalMaps(
                        environmentalMaps, environmentalPenalties, environmentalThresholds, fishState);

                    final Function<AbstractFad, double[]> catchabilitySupplier = abstractFad -> {

                        final double[] cachability = new double[globalBiology.getSize()];
                        final SeaTile fadLocation = abstractFad.getLocation();
                        final double penaltyHere = finalCatchabilityPenaltyFunction.apply(fadLocation);
                        if (penaltyHere <= 0 || !Double.isFinite(penaltyHere))
                            return cachability;

                        for (final Species species : globalBiology.getSpecies())
                            cachability[species.getIndex()] = catchabilities.getOrDefault(species.getName(), 0d) *
                                penaltyHere;
                        return cachability;
                    };


                    return new AbundanceFadInitializer(
                        globalBiology,
                        capacityGenerator,
                        new CatchabilitySelectivityFishAttractor(
                            carryingCapacities,
                            catchabilitySupplier,
                            daysInWaterBeforeAttraction.apply(rng).intValue(),
                            maximumDaysAttractions.apply(rng).intValue(),
                            fishState,
                            selectivityFilters

                        ),
                        fishReleaseProbabilityInPercent.apply(rng) / 100d,
                        fishState::getStep
                    );
                }
            }

        );


    }


    public Map<Species, NonMutatingArrayFilter> getSelectivityFilters() {
        return selectivityFilters;
    }

    @Override
    public void setSelectivityFilters(
        final Map<Species, NonMutatingArrayFilter> selectivityFilters
    ) {
        this.selectivityFilters = selectivityFilters;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityShapeParameters() {
        return carryingCapacityShapeParameters;
    }

    public void setCarryingCapacityShapeParameters(
        final LinkedHashMap<String, Double> carryingCapacityShapeParameters
    ) {
        this.carryingCapacityShapeParameters = carryingCapacityShapeParameters;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityScaleParameters() {
        return carryingCapacityScaleParameters;
    }

    public void setCarryingCapacityScaleParameters(
        final LinkedHashMap<String, Double> carryingCapacityScaleParameters
    ) {
        this.carryingCapacityScaleParameters = carryingCapacityScaleParameters;
    }

    public LinkedHashMap<String, Double> getCatchabilities() {
        return catchabilities;
    }

    public void setCatchabilities(final LinkedHashMap<String, Double> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(final DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getMaximumDaysAttractions() {
        return maximumDaysAttractions;
    }

    public void setMaximumDaysAttractions(final DoubleParameter maximumDaysAttractions) {
        this.maximumDaysAttractions = maximumDaysAttractions;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(
        final DoubleParameter fishReleaseProbabilityInPercent
    ) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(final DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }


    public LinkedList<AdditionalMapFactory> getEnvironmentalMaps() {
        return environmentalMaps;
    }

    public void setEnvironmentalMaps(final LinkedList<AdditionalMapFactory> environmentalMaps) {
        this.environmentalMaps = environmentalMaps;
    }

    public LinkedList<DoubleParameter> getEnvironmentalThresholds() {
        return environmentalThresholds;
    }

    public void setEnvironmentalThresholds(
        final LinkedList<DoubleParameter> environmentalThresholds
    ) {
        this.environmentalThresholds = environmentalThresholds;
    }

    public LinkedList<DoubleParameter> getEnvironmentalPenalties() {
        return environmentalPenalties;
    }

    public void setEnvironmentalPenalties(
        final LinkedList<DoubleParameter> environmentalPenalties
    ) {
        this.environmentalPenalties = environmentalPenalties;
    }

    public DoubleParameter getFishValueCalculatorStandardDeviation() {
        return fishValueCalculatorStandardDeviation;
    }

    public void setFishValueCalculatorStandardDeviation(final DoubleParameter fishValueCalculatorStandardDeviation) {
        this.fishValueCalculatorStandardDeviation = fishValueCalculatorStandardDeviation;
    }
}
