package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalMapFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * like the linear catchability weibull attractor, but this one sets catchability to 0 whenever clorophill is below a specific value
 */
public class WeibullCatchabilitySelectivityEnvironmentalAttractorFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>> {
    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker =
        new Locker<>();
    private AbundanceFiltersFactory abundanceFiltersFactory;
    private Map<String, SpeciesParameters> speciesParameters;
    private Map<String, EnvironmentalMapFactory> environmentalMapFactories;
    private DoubleParameter fishValueCalculatorStandardDeviation =
        new CalibratedParameter(0, 0.5, 0, 1, 0);
    private DoubleParameter fadDudRate =
        new CalibratedParameter(0, 0.35, 0, 1, 0.001);
    private DoubleParameter daysInWaterBeforeAttraction =
        new CalibratedParameter(0, 20, 0);
    private DoubleParameter maximumDaysAttractions =
        new FixedDoubleParameter(Integer.MAX_VALUE);
    private DoubleParameter fishReleaseProbabilityInPercent =
        new CalibratedParameter(0.0, 3.5, 0, 10, 3.3);

    public WeibullCatchabilitySelectivityEnvironmentalAttractorFactory() {
    }

    public WeibullCatchabilitySelectivityEnvironmentalAttractorFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Map<String, SpeciesParameters> speciesParameters,
        final Map<String, EnvironmentalMapFactory> environmentalMapFactories,
        final DoubleParameter fadDudRate,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter maximumDaysAttractions,
        final DoubleParameter fishReleaseProbabilityInPercent
    ) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
        this.speciesParameters = speciesParameters;
        this.environmentalMapFactories = environmentalMapFactories;
        this.fadDudRate = fadDudRate;
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
        this.maximumDaysAttractions = maximumDaysAttractions;
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public WeibullCatchabilitySelectivityEnvironmentalAttractorFactory(
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Map<String, SpeciesParameters> speciesParameters,
        final Map<String, EnvironmentalMapFactory> environmentalMapFactories
    ) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
        this.speciesParameters = speciesParameters;
        this.environmentalMapFactories = environmentalMapFactories;
    }

    public Map<String, EnvironmentalMapFactory> getEnvironmentalMapFactories() {
        return environmentalMapFactories;
    }

    public void setEnvironmentalMapFactories(final Map<String, EnvironmentalMapFactory> environmentalMapFactories) {
        this.environmentalMapFactories = environmentalMapFactories;
    }

    public Map<String, SpeciesParameters> getSpeciesParameters() {
        return speciesParameters;
    }

    public void setSpeciesParameters(final Map<String, SpeciesParameters> speciesParameters) {
        this.speciesParameters = speciesParameters;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
            fishState,
            () -> {


                //attractor:
                final MersenneTwisterFast rng = fishState.getRandom();
                final double probabilityOfFadBeingDud = fadDudRate.applyAsDouble(rng);
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
                        speciesParameters.containsKey(species.getName()) ?
                            new WeibullDoubleParameter(
                                speciesParameters.get(species.getName())
                                    .getCarryingCapacityShape()
                                    .applyAsDouble(rng),
                                speciesParameters.get(species.getName())
                                    .getCarryingCapacityShape()
                                    .applyAsDouble(rng)
                            ) : new FixedDoubleParameter(-1);
                }
                final Function<SeaTile, Double> finalCatchabilityPenaltyFunction =
                    this.createEnvironmentalPenaltyAndStartEnvironmentalMaps(fishState);

                final Function<AbstractFad, double[]> catchabilitySupplier = abstractFad -> {
                    final double[] catchability = new double[globalBiology.getSize()];
                    final SeaTile fadLocation = abstractFad.getLocation();
                    final double penaltyHere = finalCatchabilityPenaltyFunction.apply(fadLocation);
                    if (penaltyHere <= 0 || !Double.isFinite(penaltyHere))
                        return catchability;
                    speciesParameters.forEach((name, parameters) ->
                        catchability[globalBiology.getSpecie(name).getIndex()] =
                            parameters.getCatchability().applyAsDouble(rng) * penaltyHere
                    );
                    return catchability;
                };

                return new AbundanceFadInitializer(
                    globalBiology,
                    capacityGenerator,
                    new CatchabilitySelectivityFishAttractor(
                        carryingCapacities,
                        catchabilitySupplier,
                        (int) daysInWaterBeforeAttraction.applyAsDouble(rng),
                        (int) maximumDaysAttractions.applyAsDouble(rng),
                        fishState,
                        abundanceFiltersFactory.apply(fishState).get(FadSetAction.class)
                    ),
                    fishReleaseProbabilityInPercent.applyAsDouble(rng) / 100d,
                    fishState::getStep
                );
            }

        );


    }

    private Function<SeaTile, Double> createEnvironmentalPenaltyAndStartEnvironmentalMaps(
        final FishState fishState
    ) {
        Function<SeaTile, Double> catchabilityPenaltyFunction = null;

        for (final EnvironmentalMapFactory environmentalMapFactory : environmentalMapFactories.values()) {
            final AdditionalStartable newMap = environmentalMapFactory.apply(fishState);
            fishState.registerStartable(newMap);
            final String mapName = environmentalMapFactory.getMapVariableName();
            final double threshold = environmentalMapFactory.getThreshold().applyAsDouble(fishState.getRandom());
            final double penalty = environmentalMapFactory.getPenalty().applyAsDouble(fishState.getRandom());

            final Function<SeaTile, Double> penaltyMultiplier = seaTile -> {
                final double valueHere =
                    fishState.getMap()
                        .getAdditionalMaps()
                        .get(mapName)
                        .get()
                        .get(seaTile.getGridX(), seaTile.getGridY());
                return Math.pow(Math.min(1d, valueHere / threshold), penalty);
            };

            if (catchabilityPenaltyFunction == null)
                catchabilityPenaltyFunction = penaltyMultiplier;
            else {
                final Function<SeaTile, Double> oldPenalty = catchabilityPenaltyFunction;
                catchabilityPenaltyFunction = seaTile -> oldPenalty.apply(seaTile) * penaltyMultiplier.apply(seaTile);
            }
        }
        return catchabilityPenaltyFunction;
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

    public DoubleParameter getFishValueCalculatorStandardDeviation() {
        return fishValueCalculatorStandardDeviation;
    }

    public void setFishValueCalculatorStandardDeviation(final DoubleParameter fishValueCalculatorStandardDeviation) {
        this.fishValueCalculatorStandardDeviation = fishValueCalculatorStandardDeviation;
    }

    public static class SpeciesParameters {
        private DoubleParameter carryingCapacityShape;
        private DoubleParameter carryingCapacityScale;
        private DoubleParameter catchability;

        public SpeciesParameters() {
            this(
                new CalibratedParameter(),
                new CalibratedParameter(),
                new CalibratedParameter()
            );
        }

        public SpeciesParameters(
            final DoubleParameter carryingCapacityShape,
            final DoubleParameter carryingCapacityScale,
            final DoubleParameter catchability
        ) {
            this.carryingCapacityShape = carryingCapacityShape;
            this.carryingCapacityScale = carryingCapacityScale;
            this.catchability = catchability;
        }

        public DoubleParameter getCarryingCapacityShape() {
            return carryingCapacityShape;
        }

        public void setCarryingCapacityShape(final DoubleParameter carryingCapacityShape) {
            this.carryingCapacityShape = carryingCapacityShape;
        }

        public DoubleParameter getCarryingCapacityScale() {
            return carryingCapacityScale;
        }

        public void setCarryingCapacityScale(final DoubleParameter carryingCapacityScale) {
            this.carryingCapacityScale = carryingCapacityScale;
        }

        public DoubleParameter getCatchability() {
            return catchability;
        }

        public void setCatchability(final DoubleParameter catchability) {
            this.catchability = catchability;
        }
    }

}
