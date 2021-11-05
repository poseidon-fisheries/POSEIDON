package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import static java.lang.Double.MAX_VALUE;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.measure.quantity.Mass;
import org.jetbrains.annotations.NotNull;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.ActionAttractionField;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionField;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DeploymentLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.DolphinSetLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.FadLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.GlobalDeploymentAttractionModulator;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.GlobalSetAttractionModulator;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocalCanFishThereAttractionModulator;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocalSetAttractionModulator;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.NonAssociatedSetLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.OpportunisticFadSetLocationValues;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.PortAttractionField;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.PortAttractionModulator;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

@SuppressWarnings("unused")
public abstract class PurseSeineGearFactory<B extends LocalBiology, F extends Fad<B, F>>
    implements AlgorithmFactory<PurseSeineGear<B, F>> {

    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();

    private Set<Observer<FadDeploymentAction>> fadDeploymentObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<FadDeploymentAction>>> fadDeploymentObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(fadDeploymentObservers));

    @SuppressWarnings("rawtypes")
    private Set<Observer<AbstractFadSetAction>> fadSetObservers = new LinkedHashSet<>();

    @SuppressWarnings("rawtypes")
    private final CacheByFishState<Set<Observer<AbstractFadSetAction>>>
        fadSetObserversCache = new CacheByFishState<>(__ -> ImmutableSet.copyOf(fadSetObservers));

    @SuppressWarnings("rawtypes")
    private Set<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers =
        new LinkedHashSet<>();

    @SuppressWarnings("rawtypes")
    private final CacheByFishState<Set<Observer<NonAssociatedSetAction>>>
        nonAssociatedSetObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(nonAssociatedSetObservers));

    @SuppressWarnings("rawtypes")
    private Set<Observer<DolphinSetAction>> dolphinSetObservers = new LinkedHashSet<>();

    @SuppressWarnings("rawtypes")
    private final CacheByFishState<Set<Observer<DolphinSetAction>>> dolphinSetObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(dolphinSetObservers));

    private double decayRateOfOpportunisticFadSetLocationValues = 0.01;
    private double decayRateOfNonAssociatedSetLocationValues = 0.01;
    private double decayRateOfDolphinSetLocationValues = 0.01;
    private double decayRateOfDeploymentLocationValues = 0.01;
    private GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor;
    private List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations =
        ImmutableList.of(new ActiveFadLimitsFactory());
    private AlgorithmFactory<? extends FadInitializer<B, F>> fadInitializerFactory;
    // See https://github.com/nicolaspayette/tuna/issues/8 re: successful set probability
    private DoubleParameter successfulSetProbability = new FixedDoubleParameter(0.9231701);
    private Path locationValuesFile = INPUT_PATH.resolve("location_values.csv");
    private double pctHoldSpaceLeftLogisticMidpoint = 0.9;
    private double pctHoldSpaceLeftLogisticSteepness = MAX_VALUE;
    private double pctTravelTimeLeftLogisticMidpoint = 0.9;
    private double pctTravelTimeLeftLogisticSteepness = MAX_VALUE;
    private double pctSetsRemainingLogisticMidpoint = 0.5; // not calibrated for now
    private double pctSetsRemainingLogisticSteepness = 1; // not calibrated for now
    private double opportunisticFadSetTimeSinceLastVisitLogisticMidpoint = 5;
    private double opportunisticFadSetTimeSinceLastVisitLogisticSteepness = 1;
    private double nonAssociatedSetTimeSinceLastVisitLogisticMidpoint = 5;
    private double nonAssociatedSetTimeSinceLastVisitLogisticSteepness = 1;
    private double dolphinSetTimeSinceLastVisitLogisticMidpoint = 5;
    private double dolphinSetTimeSinceLastVisitLogisticSteepness = 1;
    private double fadDeploymentPctActiveFadsLimitLogisticMidpoint = 0.5;
    private double fadDeploymentPctActiveFadsLimitLogisticSteepness = 1;
    private double actionDistanceExponent = 1;
    private double destinationDistanceExponent = 1;
    private double numFadsInStockLogisticMidpoint = 5;
    private double numFadsInStockLogisticSteepness = 1;

    PurseSeineGearFactory(
        final AlgorithmFactory<? extends FadInitializer<B, F>> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    public double getNumFadsInStockLogisticMidpoint() {
        return numFadsInStockLogisticMidpoint;
    }

    public void setNumFadsInStockLogisticMidpoint(final double numFadsInStockLogisticMidpoint) {
        this.numFadsInStockLogisticMidpoint = numFadsInStockLogisticMidpoint;
    }

    public double getNumFadsInStockLogisticSteepness() {
        return numFadsInStockLogisticSteepness;
    }

    public void setNumFadsInStockLogisticSteepness(final double numFadsInStockLogisticSteepness) {
        this.numFadsInStockLogisticSteepness = numFadsInStockLogisticSteepness;
    }

    public double getActionDistanceExponent() {
        return actionDistanceExponent;
    }

    public void setActionDistanceExponent(final double actionDistanceExponent) {
        this.actionDistanceExponent = actionDistanceExponent;
    }

    public double getDestinationDistanceExponent() {
        return destinationDistanceExponent;
    }

    public void setDestinationDistanceExponent(final double destinationDistanceExponent) {
        this.destinationDistanceExponent = destinationDistanceExponent;
    }

    public double getPctSetsRemainingLogisticMidpoint() {
        return pctSetsRemainingLogisticMidpoint;
    }

    public void setPctSetsRemainingLogisticMidpoint(final double pctSetsRemainingLogisticMidpoint) {
        this.pctSetsRemainingLogisticMidpoint = pctSetsRemainingLogisticMidpoint;
    }

    public double getPctSetsRemainingLogisticSteepness() {
        return pctSetsRemainingLogisticSteepness;
    }

    public void setPctSetsRemainingLogisticSteepness(
        final double pctSetsRemainingLogisticSteepness
    ) {
        this.pctSetsRemainingLogisticSteepness = pctSetsRemainingLogisticSteepness;
    }

    @SuppressWarnings("rawtypes")
    public Set<Observer<DolphinSetAction>> getDolphinSetObservers() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return dolphinSetObservers;
    }

    public void setDolphinSetObservers(
        @SuppressWarnings("rawtypes") final Set<Observer<DolphinSetAction>> dolphinSetObservers
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.dolphinSetObservers = dolphinSetObservers;
    }

    @SuppressWarnings("unused")
    public double getPctHoldSpaceLeftLogisticMidpoint() {
        return pctHoldSpaceLeftLogisticMidpoint;
    }

    @SuppressWarnings("unused")
    public void setPctHoldSpaceLeftLogisticMidpoint(final double pctHoldSpaceLeftLogisticMidpoint) {
        this.pctHoldSpaceLeftLogisticMidpoint = pctHoldSpaceLeftLogisticMidpoint;
    }

    public double getPctHoldSpaceLeftLogisticSteepness() {
        return pctHoldSpaceLeftLogisticSteepness;
    }

    public void setPctHoldSpaceLeftLogisticSteepness(
        final double pctHoldSpaceLeftLogisticSteepness
    ) {
        this.pctHoldSpaceLeftLogisticSteepness = pctHoldSpaceLeftLogisticSteepness;
    }

    public double getPctTravelTimeLeftLogisticMidpoint() {
        return pctTravelTimeLeftLogisticMidpoint;
    }

    @SuppressWarnings("unused")
    public void setPctTravelTimeLeftLogisticMidpoint(
        final double pctTravelTimeLeftLogisticMidpoint
    ) {
        this.pctTravelTimeLeftLogisticMidpoint = pctTravelTimeLeftLogisticMidpoint;
    }

    public double getPctTravelTimeLeftLogisticSteepness() {
        return pctTravelTimeLeftLogisticSteepness;
    }

    public void setPctTravelTimeLeftLogisticSteepness(
        final double pctTravelTimeLeftLogisticSteepness
    ) {
        this.pctTravelTimeLeftLogisticSteepness = pctTravelTimeLeftLogisticSteepness;
    }

    public double getFadDeploymentPctActiveFadsLimitLogisticMidpoint() {
        return fadDeploymentPctActiveFadsLimitLogisticMidpoint;
    }

    public void setFadDeploymentPctActiveFadsLimitLogisticMidpoint(
        final double fadDeploymentPctActiveFadsLimitLogisticMidpoint
    ) {
        this.fadDeploymentPctActiveFadsLimitLogisticMidpoint =
            fadDeploymentPctActiveFadsLimitLogisticMidpoint;
    }

    public double getFadDeploymentPctActiveFadsLimitLogisticSteepness() {
        return fadDeploymentPctActiveFadsLimitLogisticSteepness;
    }

    public void setFadDeploymentPctActiveFadsLimitLogisticSteepness(
        final double fadDeploymentPctActiveFadsLimitLogisticSteepness
    ) {
        this.fadDeploymentPctActiveFadsLimitLogisticSteepness =
            fadDeploymentPctActiveFadsLimitLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetTimeSinceLastVisitLogisticMidpoint() {
        return opportunisticFadSetTimeSinceLastVisitLogisticMidpoint;
    }

    public void setOpportunisticFadSetTimeSinceLastVisitLogisticMidpoint(
        final double opportunisticFadSetTimeSinceLastVisitLogisticMidpoint
    ) {
        this.opportunisticFadSetTimeSinceLastVisitLogisticMidpoint =
            opportunisticFadSetTimeSinceLastVisitLogisticMidpoint;
    }

    public double getOpportunisticFadSetTimeSinceLastVisitLogisticSteepness() {
        return opportunisticFadSetTimeSinceLastVisitLogisticSteepness;
    }

    public void setOpportunisticFadSetTimeSinceLastVisitLogisticSteepness(
        final double opportunisticFadSetTimeSinceLastVisitLogisticSteepness
    ) {
        this.opportunisticFadSetTimeSinceLastVisitLogisticSteepness =
            opportunisticFadSetTimeSinceLastVisitLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetTimeSinceLastVisitLogisticMidpoint() {
        return nonAssociatedSetTimeSinceLastVisitLogisticMidpoint;
    }

    public void setNonAssociatedSetTimeSinceLastVisitLogisticMidpoint(
        final double nonAssociatedSetTimeSinceLastVisitLogisticMidpoint
    ) {
        this.nonAssociatedSetTimeSinceLastVisitLogisticMidpoint =
            nonAssociatedSetTimeSinceLastVisitLogisticMidpoint;
    }

    public double getNonAssociatedSetTimeSinceLastVisitLogisticSteepness() {
        return nonAssociatedSetTimeSinceLastVisitLogisticSteepness;
    }

    public void setNonAssociatedSetTimeSinceLastVisitLogisticSteepness(
        final double nonAssociatedSetTimeSinceLastVisitLogisticSteepness
    ) {
        this.nonAssociatedSetTimeSinceLastVisitLogisticSteepness =
            nonAssociatedSetTimeSinceLastVisitLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetTimeSinceLastVisitLogisticMidpoint() {
        return dolphinSetTimeSinceLastVisitLogisticMidpoint;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetTimeSinceLastVisitLogisticMidpoint(
        final double dolphinSetTimeSinceLastVisitLogisticMidpoint
    ) {
        this.dolphinSetTimeSinceLastVisitLogisticMidpoint =
            dolphinSetTimeSinceLastVisitLogisticMidpoint;
    }

    public double getDolphinSetTimeSinceLastVisitLogisticSteepness() {
        return dolphinSetTimeSinceLastVisitLogisticSteepness;
    }

    public void setDolphinSetTimeSinceLastVisitLogisticSteepness(
        final double dolphinSetTimeSinceLastVisitLogisticSteepness
    ) {
        this.dolphinSetTimeSinceLastVisitLogisticSteepness =
            dolphinSetTimeSinceLastVisitLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public GroupingMonitor<Species, BiomassLostEvent, Double, Mass> getBiomassLostMonitor() {
        return biomassLostMonitor;
    }

    public void setBiomassLostMonitor(
        final GroupingMonitor<Species, BiomassLostEvent, Double,
            Mass> biomassLostMonitor
    ) {
        this.biomassLostMonitor = biomassLostMonitor;
    }

    @SuppressWarnings("unused")
    public List<
        AlgorithmFactory<? extends ActionSpecificRegulation>
        > getActionSpecificRegulations() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return actionSpecificRegulations;
    }

    public void setActionSpecificRegulations(
        final List<AlgorithmFactory<?
            extends ActionSpecificRegulation>> actionSpecificRegulations
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.actionSpecificRegulations = actionSpecificRegulations;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getSuccessfulSetProbability() {
        return successfulSetProbability;
    }

    @SuppressWarnings("unused")
    public void setSuccessfulSetProbability(final DoubleParameter successfulSetProbability) {
        this.successfulSetProbability = successfulSetProbability;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends FadInitializer<B, F>> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(
        final AlgorithmFactory<? extends FadInitializer<B, F>> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public Set<Observer<FadDeploymentAction>> getFadDeploymentObservers() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return fadDeploymentObservers;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentObservers(
        final Set<Observer<FadDeploymentAction>> fadDeploymentObservers
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.fadDeploymentObservers = fadDeploymentObservers;
    }

    @SuppressWarnings({"unused", "rawtypes"})
    public Set<Observer<AbstractFadSetAction>> getFadSetObservers() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return fadSetObservers;
    }

    @SuppressWarnings("unused")
    public void setFadSetObservers(
        @SuppressWarnings("rawtypes") final Set<Observer<AbstractFadSetAction>> fadSetObservers
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.fadSetObservers = fadSetObservers;
    }

    @SuppressWarnings({"unused", "rawtypes"})
    public Set<Observer<NonAssociatedSetAction>> getNonAssociatedSetObservers() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return nonAssociatedSetObservers;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetObservers(
        @SuppressWarnings("rawtypes")
        final Set<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.nonAssociatedSetObservers = nonAssociatedSetObservers;
    }

    @NotNull
    FadManager<B, F> makeFadManager(final FishState fishState) {
        final ActiveActionRegulations actionSpecificRegulations = new ActiveActionRegulations(
            this.actionSpecificRegulations.stream()
                .map(factory -> factory.apply(fishState))
                .collect(toList())
        );

        @SuppressWarnings("unchecked") final FadManager<B, F> fadManager = new FadManager<>(
            (FadMap<B, F>) fishState.getFadMap(),
            fadInitializerFactory.apply(fishState),
            fadDeploymentObserversCache.get(fishState),
            fadSetObserversCache.get(fishState),
            nonAssociatedSetObserversCache.get(fishState),
            dolphinSetObserversCache.get(fishState),
            Optional.of(biomassLostMonitor),
            actionSpecificRegulations
        );
        return fadManager;
    }

    Stream<AttractionField> attractionFields() {
        final GlobalSetAttractionModulator globalSetAttractionModulator =
            new GlobalSetAttractionModulator(
                pctHoldSpaceLeftLogisticMidpoint,
                pctHoldSpaceLeftLogisticSteepness,
                pctSetsRemainingLogisticMidpoint,
                pctSetsRemainingLogisticSteepness
            );
        return Stream.of(
            new ActionAttractionField(
                new FadLocationValues(),
                LocalCanFishThereAttractionModulator.INSTANCE,
                globalSetAttractionModulator,
                FadSetAction.class,
                actionDistanceExponent,
                destinationDistanceExponent
            ),
            new ActionAttractionField(
                new OpportunisticFadSetLocationValues(
                    fisher -> loadLocationValues(fisher, OpportunisticFadSetAction.class),
                    getDecayRateOfOpportunisticFadSetLocationValues()
                ),
                new LocalSetAttractionModulator(
                    opportunisticFadSetTimeSinceLastVisitLogisticMidpoint,
                    opportunisticFadSetTimeSinceLastVisitLogisticSteepness
                ),
                globalSetAttractionModulator,
                OpportunisticFadSetAction.class,
                actionDistanceExponent,
                destinationDistanceExponent
            ),
            new ActionAttractionField(
                new NonAssociatedSetLocationValues(
                    fisher -> loadLocationValues(fisher, NonAssociatedSetAction.class),
                    getDecayRateOfNonAssociatedSetLocationValues()
                ),
                new LocalSetAttractionModulator(
                    nonAssociatedSetTimeSinceLastVisitLogisticMidpoint,
                    nonAssociatedSetTimeSinceLastVisitLogisticSteepness
                ),
                globalSetAttractionModulator,
                NonAssociatedSetAction.class,
                actionDistanceExponent,
                destinationDistanceExponent
            ),
            new ActionAttractionField(
                new DolphinSetLocationValues(
                    fisher -> loadLocationValues(fisher, DolphinSetAction.class),
                    getDecayRateOfDolphinSetLocationValues()
                ),
                new LocalSetAttractionModulator(
                    dolphinSetTimeSinceLastVisitLogisticMidpoint,
                    dolphinSetTimeSinceLastVisitLogisticSteepness
                ),
                globalSetAttractionModulator,
                DolphinSetAction.class,
                actionDistanceExponent,
                destinationDistanceExponent
            ),
            new ActionAttractionField(
                new DeploymentLocationValues(
                    fisher -> loadLocationValues(fisher, FadDeploymentAction.class),
                    getDecayRateOfDeploymentLocationValues()
                ),
                LocalCanFishThereAttractionModulator.INSTANCE,
                new GlobalDeploymentAttractionModulator(
                    fadDeploymentPctActiveFadsLimitLogisticMidpoint,
                    fadDeploymentPctActiveFadsLimitLogisticSteepness,
                    numFadsInStockLogisticMidpoint,
                    numFadsInStockLogisticSteepness
                ),
                FadDeploymentAction.class,
                actionDistanceExponent,
                destinationDistanceExponent
            ),
            new PortAttractionField(
                new PortAttractionModulator(
                    pctHoldSpaceLeftLogisticMidpoint,
                    pctHoldSpaceLeftLogisticSteepness,
                    pctTravelTimeLeftLogisticMidpoint,
                    pctTravelTimeLeftLogisticSteepness
                ),
                actionDistanceExponent,
                destinationDistanceExponent
            )
        );
    }

    private Map<Int2D, Double> loadLocationValues(
        final Fisher fisher,
        final Class<? extends PurseSeinerAction> actionClass
    ) {
        return locationValuesCache.getLocationValues(
            locationValuesFile,
            TARGET_YEAR,
            fisher,
            actionClass
        );
    }

    @SuppressWarnings("WeakerAccess")
    public double getDecayRateOfOpportunisticFadSetLocationValues() {
        return decayRateOfOpportunisticFadSetLocationValues;
    }

    @SuppressWarnings("unused")
    public void setDecayRateOfOpportunisticFadSetLocationValues(
        final double decayRateOfOpportunisticFadSetLocationValues
    ) {
        this.decayRateOfOpportunisticFadSetLocationValues =
            decayRateOfOpportunisticFadSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public double getDecayRateOfNonAssociatedSetLocationValues() {
        return decayRateOfNonAssociatedSetLocationValues;
    }

    public void setDecayRateOfNonAssociatedSetLocationValues(
        final double decayRateOfNonAssociatedSetLocationValues
    ) {
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public double getDecayRateOfDolphinSetLocationValues() {
        return decayRateOfDolphinSetLocationValues;
    }

    public void setDecayRateOfDolphinSetLocationValues(
        final double decayRateOfDolphinSetLocationValues
    ) {
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
    }

    @SuppressWarnings("WeakerAccess")
    public double getDecayRateOfDeploymentLocationValues() {
        return decayRateOfDeploymentLocationValues;
    }

    public void setDecayRateOfDeploymentLocationValues(
        final double decayRateOfDeploymentLocationValues
    ) {
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

    public Path getLocationValuesFile() {
        return locationValuesFile;
    }

    public void setLocationValuesFile(final Path locationValuesFile) {
        this.locationValuesFile = locationValuesFile;
    }

}
