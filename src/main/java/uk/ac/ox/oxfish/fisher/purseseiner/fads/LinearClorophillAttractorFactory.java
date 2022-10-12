package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableMap;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.PluggableSelectivity;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.ClorophillMapFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.WeibullDoubleParameter;

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



    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();


    private LinkedHashMap<String,Double> maximumCarryingCapacities = new LinkedHashMap<>();
    {
        maximumCarryingCapacities.put("Skipjack tuna",135000d);
        maximumCarryingCapacities.put("Yellowfin tuna",40000d);
        maximumCarryingCapacities.put("Bigeye tuna",60000d);
    }

    private LinkedHashMap<String,Double> catchabilities = new LinkedHashMap<>();
    {
        catchabilities.put("Species 0", 0.001d);
    }

    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);


    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);

    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);

    private DoubleParameter carryingCapacityMultiplier = new FixedDoubleParameter(1.0);

    private String clorophillMapPath = "inputs/tests/clorophill.csv";

    private int clorophillMapPeriodInDays = 365;

    private DoubleParameter clorophillThreshold = new FixedDoubleParameter(0.15);

    private final Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker =
            new Locker<>();



    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
                fishState,
                new Supplier<AbundanceFadInitializer>() {
                    @Override
                    public AbundanceFadInitializer get() {
                        //create the map
                        ClorophillMapFactory factory = new ClorophillMapFactory(clorophillMapPath);
                        factory.setMapPeriod(clorophillMapPeriodInDays);
                        fishState.registerStartable(factory.apply(fishState));

                        //attractor:
                        final double probabilityOfFadBeingDud = fadDudRate.apply(fishState.getRandom());
                        DoubleSupplier capacityGenerator;
                        if(Double.isNaN(probabilityOfFadBeingDud) || probabilityOfFadBeingDud==0)
                            capacityGenerator = () -> Double.MAX_VALUE;
                        else
                            capacityGenerator = () -> {
                                if(fishState.getRandom().nextFloat()<=probabilityOfFadBeingDud)
                                    return 0;
                                else
                                    return Double.MAX_VALUE;
                            };

                        DoubleParameter[] carryingCapacities = new DoubleParameter[fishState.getBiology().getSize()];
                        //double[] maxCatchability = new double[fishState.getBiology().getSize()];
                        // double[] cachabilityOtherwise = new double[fishState.getBiology().getSize()];
                        for (Species species : fishState.getBiology().getSpecies()) {
                            carryingCapacities[species.getIndex()] =
                                    maximumCarryingCapacities.containsKey(species.getName()) ?
                                            new FixedDoubleParameter(
                                                    maximumCarryingCapacities.get(species.getName()) *
                                                            carryingCapacityMultiplier.apply(fishState.getRandom())

                                            ) :
                                            new FixedDoubleParameter(-1);


                        }
                        Function<AbstractFad,double[]> catchabilitySupplier = abstractFad -> {

                            double[] cachability = new double[fishState.getBiology().getSize()];
                            SeaTile fadLocation = abstractFad.getLocation();
                            DoubleGrid2D currentClorophill = fishState.getMap().getAdditionalMaps().get(ClorophillMapFactory.CLOROPHILL).get();
                            double currentHere = currentClorophill.get(
                                    fadLocation.getGridX(),
                                    fadLocation.getGridY());
                            for (Species species : fishState.getBiology().getSpecies())
                                cachability[species.getIndex()] = catchabilities.getOrDefault(species.getName(),0d) *
                                        Math.pow(Math.min(1d,currentHere/clorophillThreshold.apply(fishState.getRandom())),2);
                            return cachability;
                        };


                        return new AbundanceFadInitializer(
                                fishState.getBiology(),
                                capacityGenerator,
                                new CatchabilitySelectivityFishAttractor(
                                        carryingCapacities,
                                        catchabilitySupplier,
                                        daysInWaterBeforeAttraction.apply(fishState.getRandom()).intValue(),
                                        maximumDaysAttractions.apply(fishState.getRandom()).intValue(),
                                        fishState,
                                        selectivityFilters

                                ),
                                fishReleaseProbabilityInPercent.apply(fishState.getRandom()) / 100d,
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
            Map<Species, NonMutatingArrayFilter> selectivityFilters) {
        this.selectivityFilters = selectivityFilters;
    }


    public LinkedHashMap<String, Double> getCatchabilities() {
        return catchabilities;
    }

    public void setCatchabilities(LinkedHashMap<String, Double> catchabilities) {
        this.catchabilities = catchabilities;
    }

    public DoubleParameter getDaysInWaterBeforeAttraction() {
        return daysInWaterBeforeAttraction;
    }

    public void setDaysInWaterBeforeAttraction(DoubleParameter daysInWaterBeforeAttraction) {
        this.daysInWaterBeforeAttraction = daysInWaterBeforeAttraction;
    }

    public DoubleParameter getMaximumDaysAttractions() {
        return maximumDaysAttractions;
    }

    public void setMaximumDaysAttractions(DoubleParameter maximumDaysAttractions) {
        this.maximumDaysAttractions = maximumDaysAttractions;
    }

    public DoubleParameter getFishReleaseProbabilityInPercent() {
        return fishReleaseProbabilityInPercent;
    }

    public void setFishReleaseProbabilityInPercent(
            DoubleParameter fishReleaseProbabilityInPercent) {
        this.fishReleaseProbabilityInPercent = fishReleaseProbabilityInPercent;
    }

    public DoubleParameter getFadDudRate() {
        return fadDudRate;
    }

    public void setFadDudRate(DoubleParameter fadDudRate) {
        this.fadDudRate = fadDudRate;
    }

    public String getClorophillMapPath() {
        return clorophillMapPath;
    }

    public void setClorophillMapPath(String clorophillMapPath) {
        this.clorophillMapPath = clorophillMapPath;
    }

    public DoubleParameter getClorophillThreshold() {
        return clorophillThreshold;
    }

    public void setClorophillThreshold(DoubleParameter clorophillThreshold) {
        this.clorophillThreshold = clorophillThreshold;
    }

    public LinkedHashMap<String, Double> getMaximumCarryingCapacities() {
        return maximumCarryingCapacities;
    }

    public void setMaximumCarryingCapacities(LinkedHashMap<String, Double> maximumCarryingCapacities) {
        this.maximumCarryingCapacities = maximumCarryingCapacities;
    }

    public DoubleParameter getCarryingCapacityMultiplier() {
        return carryingCapacityMultiplier;
    }

    public void setCarryingCapacityMultiplier(DoubleParameter carryingCapacityMultiplier) {
        this.carryingCapacityMultiplier = carryingCapacityMultiplier;
    }

    public int getClorophillMapPeriodInDays() {
        return clorophillMapPeriodInDays;
    }

    public void setClorophillMapPeriodInDays(int clorophillMapPeriodInDays) {
        this.clorophillMapPeriodInDays = clorophillMapPeriodInDays;
    }
}
