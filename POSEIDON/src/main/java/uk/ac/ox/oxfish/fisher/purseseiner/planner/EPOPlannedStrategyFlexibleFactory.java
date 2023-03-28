package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.DiscretizedOwnFadPlanningFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Map;

public class EPOPlannedStrategyFlexibleFactory implements AlgorithmFactory<PlannedStrategyProxy>, Dummyable {

    private int targetYear;

    public int getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final int targetYear) {
        this.targetYear = targetYear;
    }

    private final Locker<FishState, Map> catchSamplerLocker = new Locker<>();
    /**
     * object used to draw catches for DEL and NOA
     */
    private CatchSamplersFactory<? extends LocalBiology> catchSamplersFactory;
    /**
     * probability of any of these actions taking place next in a plan
     */
    private InputPath actionWeightsFile;
    private InputPath maxTripDurationFile; // boats.csv
    /**
     * hours wasted after each DEL set
     */
    private DoubleParameter additionalHourlyDelayDolphinSets = new FixedDoubleParameter(5);
    /**
     * hours wasted after every DPL
     */
    private DoubleParameter additionalHourlyDelayDeployment = new FixedDoubleParameter(0.1);
    /**
     * hours wasted after every NOA
     */
    private DoubleParameter additionalHourlyDelayNonAssociatedSets = new FixedDoubleParameter(5);
    /**
     * $ a stolen fad needs to have accumulated before we even try to target it
     */
    private DoubleParameter minimumValueOpportunisticFadSets = new FixedDoubleParameter(5000);
    /**
     * if you tried to steal and failed, how many hours does it take for you to fish this out
     */
    private DoubleParameter hoursWastedOnFailedSearches = new FixedDoubleParameter(20);
    /**
     * how many hours does it take for a plan to go stale and need replanning
     */
    private DoubleParameter planningHorizonInHours = new FixedDoubleParameter(24 * 7);
    /**
     * a multiplier applied to the action weight of own fad (since it's quite low in the data)
     */
    private DoubleParameter ownFadActionWeightBias = new FixedDoubleParameter(1);
    /**
     * a multiplier applied to the action weight of DPL
     */
    private DoubleParameter deploymentBias = new FixedDoubleParameter(1);
    /**
     * a multiplier applied to the action weight of DPL
     */
    private DoubleParameter noaBias = new FixedDoubleParameter(1);
    /**
     * a multiplier applied to the action weight of own fad (since it's quite low in the data)
     */
    private DoubleParameter minimumPercentageOfTripDurationAllowed = new FixedDoubleParameter(1);
    private boolean noaSetsCanPoachFads = false;
    private boolean purgeIllegalActionsImmediately = true;
    private DoubleParameter noaSetsRangeInSeatiles = new FixedDoubleParameter(0);
    private DoubleParameter delSetsRangeInSeatiles = new FixedDoubleParameter(0);
    private boolean uniqueCatchSamplerForEachStrategy = false;
    private AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> fadModule = new DiscretizedOwnFadPlanningFactory();

    {
        //old default values
        ((DiscretizedOwnFadPlanningFactory) fadModule).setDiscretization(new SquaresMapDiscretizerFactory(6, 3));
        ((DiscretizedOwnFadPlanningFactory) fadModule).setDistancePenalty(new FixedDoubleParameter(1));
        ((DiscretizedOwnFadPlanningFactory) fadModule).setMinimumValueFadSets(new FixedDoubleParameter(5000));
    }

    private LocationValuesSupplier locationValuesSupplier;

    public EPOPlannedStrategyFlexibleFactory() {
    }
    public EPOPlannedStrategyFlexibleFactory(
        final int targetYear,
        final LocationValuesSupplier locationValuesSupplier,
        final CatchSamplersFactory<? extends LocalBiology> catchSamplersFactory,
        final InputPath actionWeightsFile,
        final InputPath maxTripDurationFile
    ) {
        this.targetYear = targetYear;
        this.locationValuesSupplier = locationValuesSupplier;
        this.catchSamplersFactory = catchSamplersFactory;
        this.actionWeightsFile = actionWeightsFile;
        this.maxTripDurationFile = maxTripDurationFile;
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


        final PlannedStrategyProxy proxy = new PlannedStrategyProxy(
            uniqueCatchSamplerForEachStrategy ? catchSamplersFactory.apply(state) :
                catchSamplerLocker.presentKey(
                    state,
                    () -> catchSamplersFactory.apply(state)
                )
            ,
            PurseSeinerFishingStrategyFactory.loadActionWeights(targetYear, actionWeightsFile.get()),
            GravityDestinationStrategyFactory.loadMaxTripDuration(targetYear, maxTripDurationFile.get()),
            additionalHourlyDelayDolphinSets.apply(state.getRandom()),
            additionalHourlyDelayDeployment.apply(state.getRandom()),
            additionalHourlyDelayNonAssociatedSets.apply(state.getRandom()),
            ownFadActionWeightBias.apply(state.getRandom()),
            deploymentBias.apply(state.getRandom()),
            noaBias.apply(state.getRandom()),
            minimumValueOpportunisticFadSets.apply(state.getRandom()),
            hoursWastedOnFailedSearches.apply(state.getRandom()),
            planningHorizonInHours.apply(state.getRandom()),
            minimumPercentageOfTripDurationAllowed.apply(state.getRandom()),
            noaSetsCanPoachFads,
            purgeIllegalActionsImmediately,
            noaSetsRangeInSeatiles.apply(state.getRandom()).intValue(),
            delSetsRangeInSeatiles.apply(state.getRandom()).intValue(),
            fadModule,
            locationValuesSupplier.get()
        );

        return proxy;
    }

    public CatchSamplersFactory<? extends LocalBiology> getCatchSamplersFactory() {
        return catchSamplersFactory;
    }

    public void setCatchSamplersFactory(final CatchSamplersFactory<? extends LocalBiology> catchSamplersFactory) {
        this.catchSamplersFactory = catchSamplersFactory;
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

    public DoubleParameter getMinimumValueOpportunisticFadSets() {
        return minimumValueOpportunisticFadSets;
    }

    public void setMinimumValueOpportunisticFadSets(final DoubleParameter minimumValueOpportunisticFadSets) {
        this.minimumValueOpportunisticFadSets = minimumValueOpportunisticFadSets;
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

    public boolean isNoaSetsCanPoachFads() {
        return noaSetsCanPoachFads;
    }

    public void setNoaSetsCanPoachFads(final boolean noaSetsCanPoachFads) {
        this.noaSetsCanPoachFads = noaSetsCanPoachFads;
    }

    public boolean isPurgeIllegalActionsImmediately() {
        return purgeIllegalActionsImmediately;
    }

    public void setPurgeIllegalActionsImmediately(final boolean purgeIllegalActionsImmediately) {
        this.purgeIllegalActionsImmediately = purgeIllegalActionsImmediately;
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

    public DoubleParameter getDelSetsRangeInSeatiles() {
        return delSetsRangeInSeatiles;
    }

    public void setDelSetsRangeInSeatiles(final DoubleParameter delSetsRangeInSeatiles) {
        this.delSetsRangeInSeatiles = delSetsRangeInSeatiles;
    }

    public boolean isUniqueCatchSamplerForEachStrategy() {
        return uniqueCatchSamplerForEachStrategy;
    }

    public void setUniqueCatchSamplerForEachStrategy(final boolean uniqueCatchSamplerForEachStrategy) {
        this.uniqueCatchSamplerForEachStrategy = uniqueCatchSamplerForEachStrategy;
    }

    public AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> getFadModule() {
        return fadModule;
    }

    public void setFadModule(final AlgorithmFactory<? extends DiscretizedOwnFadPlanningModule> fadModule) {
        this.fadModule = fadModule;
    }

    public LocationValuesSupplier getLocationValuesSupplier() {
        return locationValuesSupplier;
    }

    @SuppressWarnings("unused")
    public void setLocationValuesSupplier(final LocationValuesSupplier locationValuesSupplier) {
        this.locationValuesSupplier = locationValuesSupplier;
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        locationValuesSupplier.setLocationValuesFile(dummyDataFolder.path("dummy_location_values.csv"));
        actionWeightsFile = dummyDataFolder.path("dummy_action_weights.csv");
        maxTripDurationFile = dummyDataFolder.path("dummy_boats.csv");
    }
}
