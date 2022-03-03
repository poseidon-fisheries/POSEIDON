package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
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
    // See https://github.com/nicolaspayette/tuna/issues/8 re: successful set probability
    private DoubleParameter successfulSetProbability = new FixedDoubleParameter(0.9231701);
    private Path locationValuesFile = INPUT_PATH.resolve("location_values.csv");
    private double pctHoldSpaceLeftCoefficient = 1E-6;
    private double pctHoldSpaceLeftExponent = 2;
    private double pctTravelTimeLeftCoefficient = 1E-6;
    private double pctTravelTimeLeftExponent = 2;
    private double pctSetsRemainingCoefficient = 2; // not calibrated for now
    private double pctSetsRemainingExponent = 1000; // not calibrated for now
    private double opportunisticFadSetTimeSinceLastVisitCoefficient = 1E-6;
    private double opportunisticFadSetTimeSinceLastVisitExponent = 2;
    private double nonAssociatedSetTimeSinceLastVisitCoefficient = 1E-6;
    private double nonAssociatedSetTimeSinceLastVisitExponent = 2;
    private double dolphinSetTimeSinceLastVisitCoefficient = 1E-6;
    private double dolphinSetTimeSinceLastVisitExponent = 2;
    private double fadDeploymentPctActiveFadsLimitCoefficient = 1E-6;
    private double fadDeploymentPctActiveFadsLimitExponent = 2;
    private double actionDistanceExponent = 1;
    private double destinationDistanceExponent = 1;
    private double numFadsInStockCoefficient = 1E-6;
    private double numFadsInStockExponent = 2;

    private FadInitializerFactory<B, F> fadInitializerFactory;

    public FadInitializerFactory<B, F> getFadInitializer() {
        return fadInitializerFactory;
    }

    public void setFadInitializerFactory(final FadInitializerFactory<B, F> fadInitializerFactory) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    public double getNumFadsInStockCoefficient() {
        return numFadsInStockCoefficient;
    }

    public void setNumFadsInStockCoefficient(final double numFadsInStockCoefficient) {
        this.numFadsInStockCoefficient = numFadsInStockCoefficient;
    }

    public double getNumFadsInStockExponent() {
        return numFadsInStockExponent;
    }

    public void setNumFadsInStockExponent(final double numFadsInStockExponent) {
        this.numFadsInStockExponent = numFadsInStockExponent;
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

    public double getPctSetsRemainingCoefficient() {
        return pctSetsRemainingCoefficient;
    }

    public void setPctSetsRemainingCoefficient(final double pctSetsRemainingCoefficient) {
        this.pctSetsRemainingCoefficient = pctSetsRemainingCoefficient;
    }

    public double getPctSetsRemainingExponent() {
        return pctSetsRemainingExponent;
    }

    public void setPctSetsRemainingExponent(
        final double pctSetsRemainingExponent
    ) {
        this.pctSetsRemainingExponent = pctSetsRemainingExponent;
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
    public double getPctHoldSpaceLeftCoefficient() {
        return pctHoldSpaceLeftCoefficient;
    }

    @SuppressWarnings("unused")
    public void setPctHoldSpaceLeftCoefficient(final double pctHoldSpaceLeftCoefficient) {
        this.pctHoldSpaceLeftCoefficient = pctHoldSpaceLeftCoefficient;
    }

    public double getPctHoldSpaceLeftExponent() {
        return pctHoldSpaceLeftExponent;
    }

    public void setPctHoldSpaceLeftExponent(
        final double pctHoldSpaceLeftExponent
    ) {
        this.pctHoldSpaceLeftExponent = pctHoldSpaceLeftExponent;
    }

    public double getPctTravelTimeLeftCoefficient() {
        return pctTravelTimeLeftCoefficient;
    }

    @SuppressWarnings("unused")
    public void setPctTravelTimeLeftCoefficient(
        final double pctTravelTimeLeftCoefficient
    ) {
        this.pctTravelTimeLeftCoefficient = pctTravelTimeLeftCoefficient;
    }

    public double getPctTravelTimeLeftExponent() {
        return pctTravelTimeLeftExponent;
    }

    public void setPctTravelTimeLeftExponent(
        final double pctTravelTimeLeftExponent
    ) {
        this.pctTravelTimeLeftExponent = pctTravelTimeLeftExponent;
    }

    public double getFadDeploymentPctActiveFadsLimitCoefficient() {
        return fadDeploymentPctActiveFadsLimitCoefficient;
    }

    public void setFadDeploymentPctActiveFadsLimitCoefficient(
        final double fadDeploymentPctActiveFadsLimitCoefficient
    ) {
        this.fadDeploymentPctActiveFadsLimitCoefficient =
            fadDeploymentPctActiveFadsLimitCoefficient;
    }

    public double getFadDeploymentPctActiveFadsLimitExponent() {
        return fadDeploymentPctActiveFadsLimitExponent;
    }

    public void setFadDeploymentPctActiveFadsLimitExponent(
        final double fadDeploymentPctActiveFadsLimitExponent
    ) {
        this.fadDeploymentPctActiveFadsLimitExponent =
            fadDeploymentPctActiveFadsLimitExponent;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetTimeSinceLastVisitCoefficient() {
        return opportunisticFadSetTimeSinceLastVisitCoefficient;
    }

    public void setOpportunisticFadSetTimeSinceLastVisitCoefficient(
        final double opportunisticFadSetTimeSinceLastVisitCoefficient
    ) {
        this.opportunisticFadSetTimeSinceLastVisitCoefficient =
            opportunisticFadSetTimeSinceLastVisitCoefficient;
    }

    public double getOpportunisticFadSetTimeSinceLastVisitExponent() {
        return opportunisticFadSetTimeSinceLastVisitExponent;
    }

    public void setOpportunisticFadSetTimeSinceLastVisitExponent(
        final double opportunisticFadSetTimeSinceLastVisitExponent
    ) {
        this.opportunisticFadSetTimeSinceLastVisitExponent =
            opportunisticFadSetTimeSinceLastVisitExponent;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetTimeSinceLastVisitCoefficient() {
        return nonAssociatedSetTimeSinceLastVisitCoefficient;
    }

    public void setNonAssociatedSetTimeSinceLastVisitCoefficient(
        final double nonAssociatedSetTimeSinceLastVisitCoefficient
    ) {
        this.nonAssociatedSetTimeSinceLastVisitCoefficient =
            nonAssociatedSetTimeSinceLastVisitCoefficient;
    }

    public double getNonAssociatedSetTimeSinceLastVisitExponent() {
        return nonAssociatedSetTimeSinceLastVisitExponent;
    }

    public void setNonAssociatedSetTimeSinceLastVisitExponent(
        final double nonAssociatedSetTimeSinceLastVisitExponent
    ) {
        this.nonAssociatedSetTimeSinceLastVisitExponent =
            nonAssociatedSetTimeSinceLastVisitExponent;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetTimeSinceLastVisitCoefficient() {
        return dolphinSetTimeSinceLastVisitCoefficient;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetTimeSinceLastVisitCoefficient(
        final double dolphinSetTimeSinceLastVisitCoefficient
    ) {
        this.dolphinSetTimeSinceLastVisitCoefficient =
            dolphinSetTimeSinceLastVisitCoefficient;
    }

    public double getDolphinSetTimeSinceLastVisitExponent() {
        return dolphinSetTimeSinceLastVisitExponent;
    }

    public void setDolphinSetTimeSinceLastVisitExponent(
        final double dolphinSetTimeSinceLastVisitExponent
    ) {
        this.dolphinSetTimeSinceLastVisitExponent =
            dolphinSetTimeSinceLastVisitExponent;
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

    @SuppressWarnings({"unused", "WeakerAccess"})
    public DoubleParameter getSuccessfulSetProbability() {
        return successfulSetProbability;
    }

    @SuppressWarnings("unused")
    public void setSuccessfulSetProbability(final DoubleParameter successfulSetProbability) {
        this.successfulSetProbability = successfulSetProbability;
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
        checkNotNull(fadInitializerFactory);
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
                pctHoldSpaceLeftCoefficient,
                pctHoldSpaceLeftExponent,
                pctSetsRemainingCoefficient,
                pctSetsRemainingExponent
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
                    opportunisticFadSetTimeSinceLastVisitCoefficient,
                    opportunisticFadSetTimeSinceLastVisitExponent
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
                    nonAssociatedSetTimeSinceLastVisitCoefficient,
                    nonAssociatedSetTimeSinceLastVisitExponent
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
                    dolphinSetTimeSinceLastVisitCoefficient,
                    dolphinSetTimeSinceLastVisitExponent
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
                    fadDeploymentPctActiveFadsLimitCoefficient,
                    fadDeploymentPctActiveFadsLimitExponent,
                    numFadsInStockCoefficient,
                    numFadsInStockExponent
                ),
                FadDeploymentAction.class,
                actionDistanceExponent,
                destinationDistanceExponent
            ),
            new PortAttractionField(
                new PortAttractionModulator(
                    pctHoldSpaceLeftCoefficient,
                    pctHoldSpaceLeftExponent,
                    pctTravelTimeLeftCoefficient,
                    pctTravelTimeLeftExponent
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
