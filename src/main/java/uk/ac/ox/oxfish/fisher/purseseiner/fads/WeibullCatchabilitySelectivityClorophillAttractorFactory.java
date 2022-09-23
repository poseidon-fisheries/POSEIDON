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
 * like the linear catchability weibull attractor, but this one sets catchability to 0 whenever clorophill is below a specific value
 */
public class WeibullCatchabilitySelectivityClorophillAttractorFactory implements
        AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceFad>>, PluggableSelectivity {


    private Map<Species, NonMutatingArrayFilter> selectivityFilters = ImmutableMap.of();


    private LinkedHashMap<String,Double> carryingCapacityShapeParameters = new LinkedHashMap<>();
    {
        carryingCapacityShapeParameters.put("Species 0", 0.5d);
    }
    private LinkedHashMap<String,Double> carryingCapacityScaleParameters = new LinkedHashMap<>();
    {
        carryingCapacityScaleParameters.put("Species 0", 100000d);
    }

    private LinkedHashMap<String,Double> catchabilities = new LinkedHashMap<>();
    {
        catchabilities.put("Species 0", 0.001d);
    }

    private DoubleParameter fadDudRate = new FixedDoubleParameter(0);


    private DoubleParameter daysInWaterBeforeAttraction = new FixedDoubleParameter(5);

    private DoubleParameter maximumDaysAttractions = new FixedDoubleParameter(30);

    private DoubleParameter fishReleaseProbabilityInPercent = new FixedDoubleParameter(0.0);

    private String clorophillMapPath = "inputs/tests/clorophill.csv";


    private DoubleParameter clorophillThreshold = new FixedDoubleParameter(0.15);

    private Locker<FishState, AbundanceFadInitializer> oneAttractorPerStateLocker = new Locker<>();



    public FadInitializer<AbundanceLocalBiology, AbundanceFad> apply(FishState fishState) {
        return oneAttractorPerStateLocker.presentKey(
                fishState,
                new Supplier<AbundanceFadInitializer>() {
                    @Override
                    public AbundanceFadInitializer get() {
                        //create the map
                        ClorophillMapFactory factory = new ClorophillMapFactory(clorophillMapPath);
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
                        double[] catchabilitiesHere = new double[fishState.getBiology().getSize()];
                        double[] cachabilityOtherwise = new double[fishState.getBiology().getSize()];
                        for (Species species : fishState.getBiology().getSpecies()) {
                            carryingCapacities[species.getIndex()] =
                                    carryingCapacityScaleParameters.containsKey(species.getName()) ?
                                            new WeibullDoubleParameter(
                                                    carryingCapacityShapeParameters.get(species.getName()),
                                                    carryingCapacityScaleParameters.get(species.getName())
                                            ) : new FixedDoubleParameter(-1);

                            catchabilitiesHere[species.getIndex()] =
                                    catchabilities.getOrDefault(species.getName(),0d);

                        }
                        Function<AbstractFad,double[]> catchabilitySupplier = abstractFad -> {
                            SeaTile fadLocation = abstractFad.getLocation();
                            DoubleGrid2D currentClorophill = fishState.getMap().getAdditionalMaps().get(ClorophillMapFactory.CLOROPHILL).get();
                            double currentHere = currentClorophill.get(
                                    fadLocation.getGridX(),
                                    fadLocation.getGridY());
                            if(currentHere>=clorophillThreshold.apply(fishState.getRandom()))
                                return catchabilitiesHere;
                            else
                                return cachabilityOtherwise;
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

    public LinkedHashMap<String, Double> getCarryingCapacityShapeParameters() {
        return carryingCapacityShapeParameters;
    }

    public void setCarryingCapacityShapeParameters(
            LinkedHashMap<String, Double> carryingCapacityShapeParameters) {
        this.carryingCapacityShapeParameters = carryingCapacityShapeParameters;
    }

    public LinkedHashMap<String, Double> getCarryingCapacityScaleParameters() {
        return carryingCapacityScaleParameters;
    }

    public void setCarryingCapacityScaleParameters(
            LinkedHashMap<String, Double> carryingCapacityScaleParameters) {
        this.carryingCapacityScaleParameters = carryingCapacityScaleParameters;
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
}
