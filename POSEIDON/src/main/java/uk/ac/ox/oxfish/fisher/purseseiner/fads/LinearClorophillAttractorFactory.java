package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculator;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * catchability is moduled by clorophill, but there is no max carrying capacity. Just keeps attracting
 * till it's time to stop
 */
public class LinearClorophillAttractorFactory implements
    AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>, PluggableSelectivity {


    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker =
        new Locker<>();
    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();
    private LinkedHashMap<String, Double> maximumCarryingCapacities = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> catchabilities = new LinkedHashMap<>();
    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);
    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);
    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);
    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);
    private DoubleParameter carryingCapacityMultiplier = new FixedDoubleParameter(1.0);
    private String clorophillMapPath = "inputs/tests/clorophill.csv";
    private int clorophillMapPeriodInDays = 365;
    private DoubleParameter clorophillThreshold = new FixedDoubleParameter(0.15);

    {
        maximumCarryingCapacities.put("Skipjack tuna", 135000d);
        maximumCarryingCapacities.put("Yellowfin tuna", 40000d);
        maximumCarryingCapacities.put("Bigeye tuna", 60000d);
    }

    {
        catchabilities.put("Species 0", 0.001d);
    }

    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(final FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
            fishState,
            new Supplier<AbundanceFadInitializer>() {
                @Override
                public AbundanceFadInitializer get() {
                    //create the map
                    final AdditionalMapFactory factory = new AdditionalMapFactory(clorophillMapPath);
                    factory.setMapPeriod(clorophillMapPeriodInDays);
                    fishState.registerStartable(factory.apply(fishState));

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
                            maximumCarryingCapacities.containsKey(species.getName()) ?
                                new FixedDoubleParameter(
                                    maximumCarryingCapacities.get(species.getName()) *
                                        carryingCapacityMultiplier.apply(rng)

                                ) :
                                new FixedDoubleParameter(-1);


                    }
                    final Function<AbstractFad, double[]> catchabilitySupplier = abstractFad -> {

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
                                Math.pow(Math.min(1d, currentHere / clorophillThreshold.apply(rng)), 2);
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

    public String getClorophillMapPath() {
        return clorophillMapPath;
    }

    public void setClorophillMapPath(final String clorophillMapPath) {
        this.clorophillMapPath = clorophillMapPath;
    }

    public DoubleParameter getClorophillThreshold() {
        return clorophillThreshold;
    }

    public void setClorophillThreshold(final DoubleParameter clorophillThreshold) {
        this.clorophillThreshold = clorophillThreshold;
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

    public int getClorophillMapPeriodInDays() {
        return clorophillMapPeriodInDays;
    }

    public void setClorophillMapPeriodInDays(final int clorophillMapPeriodInDays) {
        this.clorophillMapPeriodInDays = clorophillMapPeriodInDays;
    }

}