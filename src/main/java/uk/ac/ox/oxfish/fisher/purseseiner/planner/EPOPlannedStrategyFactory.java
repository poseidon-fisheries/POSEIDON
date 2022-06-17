package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;


import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;

public class EPOPlannedStrategyFactory implements AlgorithmFactory<PlannedStrategyProxy> {



    /**
     * object used to draw catches for DEL and NOA
     */
//    private final Map<Class<? extends AbstractSetAction<?>>,
//            CatchSampler<? extends LocalBiology>> catchSamplers;
    private CatchSamplersFactory<? extends LocalBiology> catchSamplersFactory = new AbundanceCatchSamplersFactory();
    /**
     * probability of any of these actions taking place next in a plan
     */
    private Path attractionWeightsFile = INPUT_PATH.resolve("location_values.csv");
    // PurseSeinerFishingStrategyFactory.loadAttractionWeights(..)

    /**
     * function that returns max travel time
     */
//    private final ToDoubleFunction<Fisher> maxTravelTimeLoader;
    private Path maxTripDurationFile = EpoScenario.INPUT_PATH.resolve("boats.csv");

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
     *  $ a fad needs to have accumulated before we even try to target it
     */
    private DoubleParameter minimumValueFadSets = new FixedDoubleParameter(5000);
    /**
     *  $ a stolen fad needs to have accumulated before we even try to target it
     */
    private DoubleParameter minimumValueOpportunisticFadSets = new FixedDoubleParameter(5000);
    /**
     * higher this is, the more a fisher will prefer to target FADs closer to the centroid
     * of the path
     */
    private DoubleParameter distancePenaltyFadSets = new FixedDoubleParameter(1);

    /**
     * discretizes map so that when it is time to target FADs you just
     * go through a few relevant ones
     */
    private AlgorithmFactory<? extends MapDiscretizer> mapDiscretizationFadSets =
            new SquaresMapDiscretizerFactory(6,3);

    /**
     * if you tried to steal and failed, how many hours does it take for you to fish this out
     */
    private DoubleParameter hoursWastedOnFailedSearches = new FixedDoubleParameter(20);

    /**
     * how many hours does it take for a plan to go stale and need replanning
     */
    private DoubleParameter planningHorizonInHours = new FixedDoubleParameter(24*7 );

    /**
     * a multiplier applied to the action weight of own fad (since it's quite low in the data)
     */
    private DoubleParameter ownFadActionWeightBias = new FixedDoubleParameter(1 );

    /**
     * a multiplier applied to the action weight of DPL
     */
    private DoubleParameter deploymentBias = new FixedDoubleParameter(1 );

    /**
     * a multiplier applied to the action weight of DPL
     */
    private DoubleParameter noaBias = new FixedDoubleParameter(1 );
    /**
     * a multiplier applied to the action weight of own fad (since it's quite low in the data)
     */
    private DoubleParameter minimumPercentageOfTripDurationAllowed = new FixedDoubleParameter(1 );

    private boolean noaSetsCanPoachFads = false;

    private boolean purgeIllegalActionsImmediately = true;

    private DoubleParameter noaSetsRangeInSeatiles = new FixedDoubleParameter(-1);

    @Override
    public PlannedStrategyProxy apply(FishState state) {
        return new PlannedStrategyProxy(
                catchSamplersFactory.apply(state),
                PurseSeinerFishingStrategyFactory.loadAttractionWeights(attractionWeightsFile),
                GravityDestinationStrategyFactory.loadMaxTripDuration(maxTripDurationFile),
                additionalHourlyDelayDolphinSets.apply(state.getRandom()),
                additionalHourlyDelayDeployment.apply(state.getRandom()),
                additionalHourlyDelayNonAssociatedSets.apply(state.getRandom()),
                minimumValueFadSets.apply(state.getRandom()),
                ownFadActionWeightBias.apply(state.getRandom()),
                deploymentBias.apply(state.getRandom()),
                noaBias.apply(state.getRandom()),
                minimumValueOpportunisticFadSets.apply(state.getRandom()),
                distancePenaltyFadSets.apply(state.getRandom()),
                new MapDiscretization(
                        mapDiscretizationFadSets.apply(state)
                ),
                hoursWastedOnFailedSearches.apply(state.getRandom()),
                planningHorizonInHours.apply(state.getRandom()),
                minimumPercentageOfTripDurationAllowed.apply(state.getRandom()),
                noaSetsCanPoachFads,
                purgeIllegalActionsImmediately,
                noaSetsRangeInSeatiles.apply(state.getRandom()).intValue());
    }

    public CatchSamplersFactory<? extends LocalBiology> getCatchSamplersFactory() {
        return catchSamplersFactory;
    }

    public void setCatchSamplersFactory(CatchSamplersFactory<? extends LocalBiology> catchSamplersFactory) {
        this.catchSamplersFactory = catchSamplersFactory;
    }



    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    public void setAttractionWeightsFile(Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
    }

    public Path getMaxTripDurationFile() {
        return maxTripDurationFile;
    }

    public DoubleParameter getAdditionalHourlyDelayDolphinSets() {
        return additionalHourlyDelayDolphinSets;
    }

    public void setAdditionalHourlyDelayDolphinSets(DoubleParameter additionalHourlyDelayDolphinSets) {
        this.additionalHourlyDelayDolphinSets = additionalHourlyDelayDolphinSets;
    }

    public DoubleParameter getAdditionalHourlyDelayDeployment() {
        return additionalHourlyDelayDeployment;
    }

    public void setAdditionalHourlyDelayDeployment(DoubleParameter additionalHourlyDelayDeployment) {
        this.additionalHourlyDelayDeployment = additionalHourlyDelayDeployment;
    }

    public DoubleParameter getAdditionalHourlyDelayNonAssociatedSets() {
        return additionalHourlyDelayNonAssociatedSets;
    }

    public void setAdditionalHourlyDelayNonAssociatedSets(DoubleParameter additionalHourlyDelayNonAssociatedSets) {
        this.additionalHourlyDelayNonAssociatedSets = additionalHourlyDelayNonAssociatedSets;
    }

    public DoubleParameter getMinimumValueFadSets() {
        return minimumValueFadSets;
    }

    public void setMinimumValueFadSets(DoubleParameter minimumValueFadSets) {
        this.minimumValueFadSets = minimumValueFadSets;
    }

    public DoubleParameter getDistancePenaltyFadSets() {
        return distancePenaltyFadSets;
    }

    public void setDistancePenaltyFadSets(DoubleParameter distancePenaltyFadSets) {
        this.distancePenaltyFadSets = distancePenaltyFadSets;
    }

    public AlgorithmFactory<? extends MapDiscretizer> getMapDiscretizationFadSets() {
        return mapDiscretizationFadSets;
    }

    public void setMapDiscretizationFadSets(AlgorithmFactory<? extends MapDiscretizer> mapDiscretizationFadSets) {
        this.mapDiscretizationFadSets = mapDiscretizationFadSets;
    }

    public DoubleParameter getHoursWastedOnFailedSearches() {
        return hoursWastedOnFailedSearches;
    }

    public void setHoursWastedOnFailedSearches(DoubleParameter hoursWastedOnFailedSearches) {
        this.hoursWastedOnFailedSearches = hoursWastedOnFailedSearches;
    }

    public DoubleParameter getPlanningHorizonInHours() {
        return planningHorizonInHours;
    }

    public void setPlanningHorizonInHours(DoubleParameter planningHorizonInHours) {
        this.planningHorizonInHours = planningHorizonInHours;
    }

    public void setMaxTripDurationFile(Path maxTripDurationFile) {
        this.maxTripDurationFile = maxTripDurationFile;
    }

    public DoubleParameter getMinimumValueOpportunisticFadSets() {
        return minimumValueOpportunisticFadSets;
    }

    public void setMinimumValueOpportunisticFadSets(DoubleParameter minimumValueOpportunisticFadSets) {
        this.minimumValueOpportunisticFadSets = minimumValueOpportunisticFadSets;
    }

    public DoubleParameter getOwnFadActionWeightBias() {
        return ownFadActionWeightBias;
    }

    public void setOwnFadActionWeightBias(DoubleParameter ownFadActionWeightBias) {
        this.ownFadActionWeightBias = ownFadActionWeightBias;
    }

    public DoubleParameter getDeploymentBias() {
        return deploymentBias;
    }

    public void setDeploymentBias(DoubleParameter deploymentBias) {
        this.deploymentBias = deploymentBias;
    }

    public DoubleParameter getMinimumPercentageOfTripDurationAllowed() {
        return minimumPercentageOfTripDurationAllowed;
    }

    public void setMinimumPercentageOfTripDurationAllowed(DoubleParameter minimumPercentageOfTripDurationAllowed) {
        this.minimumPercentageOfTripDurationAllowed = minimumPercentageOfTripDurationAllowed;
    }

    public boolean isNoaSetsCanPoachFads() {
        return noaSetsCanPoachFads;
    }

    public void setNoaSetsCanPoachFads(boolean noaSetsCanPoachFads) {
        this.noaSetsCanPoachFads = noaSetsCanPoachFads;
    }

    public boolean isPurgeIllegalActionsImmediately() {
        return purgeIllegalActionsImmediately;
    }

    public void setPurgeIllegalActionsImmediately(boolean purgeIllegalActionsImmediately) {
        this.purgeIllegalActionsImmediately = purgeIllegalActionsImmediately;
    }

    public DoubleParameter getNoaSetsRangeInSeatiles() {
        return noaSetsRangeInSeatiles;
    }

    public void setNoaSetsRangeInSeatiles(DoubleParameter noaSetsRangeInSeatiles) {
        this.noaSetsRangeInSeatiles = noaSetsRangeInSeatiles;
    }

    public DoubleParameter getNoaBias() {
        return noaBias;
    }

    public void setNoaBias(DoubleParameter noaBias) {
        this.noaBias = noaBias;
    }
}
