package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceAggregatingFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.LinkedHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * catchability is moduled by clorophill, but there is no max carrying capacity. Just keeps attracting
 * till it's time to stop
 */
public class LinearClorophillAttractorFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad>> {

    private final Locker<FishState, AbundanceAggregatingFadInitializer> oneAttractorPerStateLocker =
        new Locker<>();
    private AbundanceFiltersFactory abundanceFiltersFactory;
    private LinkedHashMap<String, Double> maximumCarryingCapacities = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> catchabilities = new LinkedHashMap<>();
    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private DoubleParameter carryingCapacityMultiplier = new FixedDoubleParameter(1.0);
    private InputPath chlorophyllMapPath = InputPath.of(
        "inputs",
        "epo_inputs",
        "environmental_maps",
        "chlorophyll.csv"
    );
    private int chlorophyllMapPeriodInDays = 365;
    private DoubleParameter chlorophyllThreshold = new FixedDoubleParameter(0.15);

    {
        maximumCarryingCapacities.put("Skipjack tuna", 135000d);
        maximumCarryingCapacities.put("Yellowfin tuna", 40000d);
        maximumCarryingCapacities.put("Bigeye tuna", 60000d);
    }

    {
        catchabilities.put("Species 0", 0.001d);
    }

    public LinearClorophillAttractorFactory() {
    }

    public LinearClorophillAttractorFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    public FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> apply(final FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
            fishState,
            new Supplier<AbundanceAggregatingFadInitializer>() {
                @Override
                public AbundanceAggregatingFadInitializer get() {
                    //create the map
                    final AdditionalMapFactory factory = new AdditionalMapFactory(chlorophyllMapPath);
                    factory.setMapPeriod(chlorophyllMapPeriodInDays);
                    fishState.registerStartable(factory.apply(fishState));

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
                            maximumCarryingCapacities.containsKey(species.getName()) ?
                                new FixedDoubleParameter(
                                    maximumCarryingCapacities.get(species.getName()) *
                                        carryingCapacityMultiplier.applyAsDouble(rng)

                                ) :
                                new FixedDoubleParameter(-1);


                    }
                    final Function<Fad, double[]> catchabilitySupplier = abstractFad -> {

                        final double[] cachability = new double[globalBiology.getSize()];
                        final SeaTile fadLocation = abstractFad.getLocation();
                        final DoubleGrid2D currentClorophill = fishState.getMap()
                            .getAdditionalMaps()
                            .get(factory.getMapVariableName())
                            .get();
                        final double currentHere = currentClorophill.get(
                            fadLocation.getGridX(),
                            fadLocation.getGridY()
                        );
                        for (final Species species : globalBiology.getSpecies())
                            cachability[species.getIndex()] = catchabilities.getOrDefault(species.getName(), 0d) *
                                Math.pow(Math.min(1d, currentHere / chlorophyllThreshold.applyAsDouble(rng)), 2);
                        return cachability;
                    };


                    return new AbundanceAggregatingFadInitializer(
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
            }

        );


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

    public InputPath getChlorophyllMapPath() {
        return chlorophyllMapPath;
    }

    public void setChlorophyllMapPath(final InputPath chlorophyllMapPath) {
        this.chlorophyllMapPath = chlorophyllMapPath;
    }

    public DoubleParameter getChlorophyllThreshold() {
        return chlorophyllThreshold;
    }

    public void setChlorophyllThreshold(final DoubleParameter chlorophyllThreshold) {
        this.chlorophyllThreshold = chlorophyllThreshold;
    }

    public LinkedHashMap<String, Double> getMaximumCarryingCapacities() {
        return maximumCarryingCapacities;
    }

    public void setMaximumCarryingCapacities(final LinkedHashMap<String, Double> maximumCarryingCapacities) {
        this.maximumCarryingCapacities = maximumCarryingCapacities;
    }

    public DoubleParameter getCarryingCapacityMultiplier() {
        return carryingCapacityMultiplier;
    }

    public void setCarryingCapacityMultiplier(final DoubleParameter carryingCapacityMultiplier) {
        this.carryingCapacityMultiplier = carryingCapacityMultiplier;
    }

    public int getChlorophyllMapPeriodInDays() {
        return chlorophyllMapPeriodInDays;
    }

    public void setChlorophyllMapPeriodInDays(final int chlorophyllMapPeriodInDays) {
        this.chlorophyllMapPeriodInDays = chlorophyllMapPeriodInDays;
    }

}
