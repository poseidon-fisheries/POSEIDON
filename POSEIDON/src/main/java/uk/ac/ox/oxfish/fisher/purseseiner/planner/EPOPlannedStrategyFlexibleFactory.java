package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplers;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerFishingStrategyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.oxfish.utility.parameters.BooleanParameter;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.FAD;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.OFS;

public class EPOPlannedStrategyFlexibleFactory implements AlgorithmFactory<PlannedStrategyProxy>, Dummyable {

    private IntegerParameter targetYear;
    /**
     * object used to draw catches for DEL and NOA
     */
    private CatchSamplersFactory<? extends LocalBiology> catchSamplers;

    private final CacheByFishState<CatchSamplers<? extends LocalBiology>> catchSamplersCache =
        new CacheByFishState<>(fishState -> catchSamplers.apply(fishState));

    /**
     * probability of any of these actions taking place next in a plan
     */
    private InputPath actionWeightsFile;
    private InputPath maxTripDurationFile; // boats.csv
    /**
     * hours wasted after each DEL set
     */
    private DoubleParameter additionalHourlyDelayDolphinSets =
        new CalibratedParameter(5, 15, 0, 24);
    /**
     * hours wasted after every DPL
     */
    private DoubleParameter additionalHourlyDelayDeployment =
        new CalibratedParameter(0.1, 0.2, 0, 1);
    /**
     * hours wasted after every NOA
     */
    private DoubleParameter additionalHourlyDelayNonAssociatedSets =
        new CalibratedParameter(5, 15, 0, 24);

    /**
     * To probability of finding another vessel's FAD when you search for some.
     */
    private DoubleParameter probabilityOfFindingOtherFads =
        new CalibratedParameter(0, 0.5, 0, 1);
    /**
     * if you tried to steal and failed, how many hours does it take for you to fish this out
     */
    private DoubleParameter hoursWastedOnFailedSearches =
        new CalibratedParameter(1, 8, 0, 24);
    /**
     * how many hours does it take for a plan to go stale and need replanning
     */
    private DoubleParameter planningHorizonInHours =
        new CalibratedParameter(24 * 7, 24 * 7 * 5, 24, 24 * 7 * 8);
    /**
     * a multiplier applied to the action weight of own fad (since it's quite low in the data)
     */
    private DoubleParameter ownFadActionWeightBias =
        new CalibratedParameter(0.25, 0.95, 0.0, 0.9999);
    /**
     * a multiplier applied to the action weight of DPL
     */
    private DoubleParameter deploymentBias =
        new CalibratedParameter(0.25, 0.95, 0.0, 0.9999);
    private DoubleParameter noaBias =
        new CalibratedParameter(0.25, 0.75, 0.0, 0.9999);
    private DoubleParameter delBias =
        new CalibratedParameter(0.25, 0.75, 0.0, 0.9999);
    private DoubleParameter ofsBias =
        new CalibratedParameter(0.25, 0.75, 0.0, 0.9999);
    private DoubleParameter minimumPercentageOfTripDurationAllowed =
        new CalibratedParameter(0.5, 1, 0, 1);
    private BooleanParameter noaSetsCanPoachFads = new BooleanParameter(false);
    private BooleanParameter purgeIllegalActionsImmediately = new BooleanParameter(true);
    private DoubleParameter noaSetsRangeInSeatiles =
        new CalibratedParameter(0, 2, 0, 5);
    private DoubleParameter delSetsRangeInSeatiles =
        new CalibratedParameter(0, 2, 0, 5);
    private BooleanParameter uniqueCatchSamplerForEachStrategy = new BooleanParameter(false);
    private AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> fadModule;
    private LocationValuesFactory locationValuesFactory;
    private AlgorithmFactory<MinimumSetValues> minimumSetValues;

    @SuppressWarnings("unused")
    public EPOPlannedStrategyFlexibleFactory() {
    }

    public EPOPlannedStrategyFlexibleFactory(
        final IntegerParameter targetYear,
        final LocationValuesFactory locationValuesFactory,
        final AlgorithmFactory<MinimumSetValues> minimumSetValues,
        final AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> fadModule,
        final CatchSamplersFactory<? extends LocalBiology> catchSamplers,
        final InputPath actionWeightsFile,
        final InputPath maxTripDurationFile
    ) {
        this.targetYear = targetYear;
        this.locationValuesFactory = locationValuesFactory;
        this.minimumSetValues = minimumSetValues;
        this.fadModule = fadModule;
        this.catchSamplers = catchSamplers;
        this.actionWeightsFile = actionWeightsFile;
        this.maxTripDurationFile = maxTripDurationFile;
    }

    public AlgorithmFactory<MinimumSetValues> getMinimumSetValues() {
        return minimumSetValues;
    }

    public void setMinimumSetValues(final AlgorithmFactory<MinimumSetValues> minimumSetValues) {
        this.minimumSetValues = minimumSetValues;
    }

    public DoubleParameter getProbabilityOfFindingOtherFads() {
        return probabilityOfFindingOtherFads;
    }

    public void setProbabilityOfFindingOtherFads(final DoubleParameter probabilityOfFindingOtherFads) {
        this.probabilityOfFindingOtherFads = probabilityOfFindingOtherFads;
    }

    public BooleanParameter getNoaSetsCanPoachFads() {
        return noaSetsCanPoachFads;
    }

    public void setNoaSetsCanPoachFads(final BooleanParameter noaSetsCanPoachFads) {
        this.noaSetsCanPoachFads = noaSetsCanPoachFads;
    }

    public BooleanParameter getPurgeIllegalActionsImmediately() {
        return purgeIllegalActionsImmediately;
    }

    public void setPurgeIllegalActionsImmediately(final BooleanParameter purgeIllegalActionsImmediately) {
        this.purgeIllegalActionsImmediately = purgeIllegalActionsImmediately;
    }

    public InputPath getActionWeightsFile() {
        return actionWeightsFile;
    }

    public void setActionWeightsFile(final InputPath actionWeightsFile) {
        this.actionWeightsFile = actionWeightsFile;
    }

    public InputPath getMaxTripDurationFile() {
        return maxTripDurationFile;
    }

    public void setMaxTripDurationFile(final InputPath maxTripDurationFile) {
        this.maxTripDurationFile = maxTripDurationFile;
    }

    @Override
    public PlannedStrategyProxy apply(final FishState state) {
        final MersenneTwisterFast rng = state.getRandom();
        final MinimumSetValues minSetValues = minimumSetValues.apply(state);
        final Integer targetYear = getTargetYear().getValue();
        return new PlannedStrategyProxy(
            uniqueCatchSamplerForEachStrategy.getValue()
                ? catchSamplers.apply(state)
                : catchSamplersCache.get(state),
            PurseSeinerFishingStrategyFactory.loadActionWeights(this.targetYear.getValue(), actionWeightsFile.get()),
            GravityDestinationStrategyFactory.loadMaxTripDuration(
                this.targetYear.getValue(),
                maxTripDurationFile.get()
            ),
            additionalHourlyDelayDolphinSets.applyAsDouble(rng),
            additionalHourlyDelayDeployment.applyAsDouble(rng),
            additionalHourlyDelayNonAssociatedSets.applyAsDouble(rng),
            ownFadActionWeightBias.applyAsDouble(rng),
            deploymentBias.applyAsDouble(rng),
            noaBias.applyAsDouble(rng),
            delBias.applyAsDouble(rng),
            ofsBias.applyAsDouble(rng),
            minSetValues.getMinimumSetValue(targetYear, OFS),
            minSetValues.getMinimumSetValue(targetYear, FAD),
            probabilityOfFindingOtherFads.applyAsDouble(rng),
            hoursWastedOnFailedSearches.applyAsDouble(rng),
            planningHorizonInHours.applyAsDouble(rng),
            minimumPercentageOfTripDurationAllowed.applyAsDouble(rng),
            noaSetsCanPoachFads.getValue(),
            purgeIllegalActionsImmediately.getValue(),
            (int) noaSetsRangeInSeatiles.applyAsDouble(rng),
            (int) delSetsRangeInSeatiles.applyAsDouble(rng),
            fadModule,
            locationValuesFactory.apply(state).asMap()
        );
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    public CatchSamplersFactory<? extends LocalBiology> getCatchSamplers() {
        return catchSamplers;
    }

    public void setCatchSamplers(final CatchSamplersFactory<? extends LocalBiology> catchSamplers) {
        this.catchSamplers = catchSamplers;
    }

    public DoubleParameter getAdditionalHourlyDelayDolphinSets() {
        return additionalHourlyDelayDolphinSets;
    }

    public void setAdditionalHourlyDelayDolphinSets(final DoubleParameter additionalHourlyDelayDolphinSets) {
        this.additionalHourlyDelayDolphinSets = additionalHourlyDelayDolphinSets;
    }

    public DoubleParameter getAdditionalHourlyDelayDeployment() {
        return additionalHourlyDelayDeployment;
    }

    public void setAdditionalHourlyDelayDeployment(final DoubleParameter additionalHourlyDelayDeployment) {
        this.additionalHourlyDelayDeployment = additionalHourlyDelayDeployment;
    }

    public DoubleParameter getAdditionalHourlyDelayNonAssociatedSets() {
        return additionalHourlyDelayNonAssociatedSets;
    }

    public void setAdditionalHourlyDelayNonAssociatedSets(final DoubleParameter additionalHourlyDelayNonAssociatedSets) {
        this.additionalHourlyDelayNonAssociatedSets = additionalHourlyDelayNonAssociatedSets;
    }

    public DoubleParameter getHoursWastedOnFailedSearches() {
        return hoursWastedOnFailedSearches;
    }

    public void setHoursWastedOnFailedSearches(final DoubleParameter hoursWastedOnFailedSearches) {
        this.hoursWastedOnFailedSearches = hoursWastedOnFailedSearches;
    }

    public DoubleParameter getPlanningHorizonInHours() {
        return planningHorizonInHours;
    }

    public void setPlanningHorizonInHours(final DoubleParameter planningHorizonInHours) {
        this.planningHorizonInHours = planningHorizonInHours;
    }

    public DoubleParameter getOwnFadActionWeightBias() {
        return ownFadActionWeightBias;
    }

    public void setOwnFadActionWeightBias(final DoubleParameter ownFadActionWeightBias) {
        this.ownFadActionWeightBias = ownFadActionWeightBias;
    }

    public DoubleParameter getDeploymentBias() {
        return deploymentBias;
    }

    public void setDeploymentBias(final DoubleParameter deploymentBias) {
        this.deploymentBias = deploymentBias;
    }

    public DoubleParameter getMinimumPercentageOfTripDurationAllowed() {
        return minimumPercentageOfTripDurationAllowed;
    }

    public void setMinimumPercentageOfTripDurationAllowed(final DoubleParameter minimumPercentageOfTripDurationAllowed) {
        this.minimumPercentageOfTripDurationAllowed = minimumPercentageOfTripDurationAllowed;
    }

    public DoubleParameter getNoaSetsRangeInSeatiles() {
        return noaSetsRangeInSeatiles;
    }

    public void setNoaSetsRangeInSeatiles(final DoubleParameter noaSetsRangeInSeatiles) {
        this.noaSetsRangeInSeatiles = noaSetsRangeInSeatiles;
    }

    public DoubleParameter getNoaBias() {
        return noaBias;
    }

    public void setNoaBias(final DoubleParameter noaBias) {
        this.noaBias = noaBias;
    }

    public DoubleParameter getDelBias() {
        return delBias;
    }

    public void setDelBias(final DoubleParameter delBias) {
        this.delBias = delBias;
    }

    public DoubleParameter getOfsBias() {
        return ofsBias;
    }

    public void setOfsBias(final DoubleParameter ofsBias) {
        this.ofsBias = ofsBias;
    }

    public DoubleParameter getDelSetsRangeInSeatiles() {
        return delSetsRangeInSeatiles;
    }

    public void setDelSetsRangeInSeatiles(final DoubleParameter delSetsRangeInSeatiles) {
        this.delSetsRangeInSeatiles = delSetsRangeInSeatiles;
    }

    public BooleanParameter getUniqueCatchSamplerForEachStrategy() {
        return uniqueCatchSamplerForEachStrategy;
    }

    public void setUniqueCatchSamplerForEachStrategy(final BooleanParameter uniqueCatchSamplerForEachStrategy) {
        this.uniqueCatchSamplerForEachStrategy = uniqueCatchSamplerForEachStrategy;
    }

    public AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> getFadModule() {
        return fadModule;
    }

    public void setFadModule(final AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> fadModule) {
        this.fadModule = fadModule;
    }

    public LocationValuesFactory getLocationValuesSupplier() {
        return locationValuesFactory;
    }

    @SuppressWarnings("unused")
    public void setLocationValuesSupplier(final LocationValuesFactory locationValuesFactory) {
        this.locationValuesFactory = locationValuesFactory;
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        locationValuesFactory.setLocationValuesFile(dummyDataFolder.path("dummy_location_values.csv"));
        actionWeightsFile = dummyDataFolder.path("dummy_action_weights.csv");
        maxTripDurationFile = dummyDataFolder.path("dummy_boats.csv");
    }
}
