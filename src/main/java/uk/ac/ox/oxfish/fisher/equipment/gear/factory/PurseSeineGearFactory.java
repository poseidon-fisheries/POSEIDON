package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.*;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.PurseSeinerActionClassToDouble;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.operators.LogisticFunctionFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.quantity.Mass;
import java.nio.file.Path;
import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

@SuppressWarnings("unused")
public abstract class PurseSeineGearFactory<B extends LocalBiology, F extends Fad<B, F>>
    implements AlgorithmFactory<PurseSeineGear<B, F>> {

    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();

    private Set<Observer<FadDeploymentAction>> fadDeploymentObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<FadDeploymentAction>>> fadDeploymentObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(fadDeploymentObservers));
    @SuppressWarnings("rawtypes")
    private Set<Observer<AbstractSetAction>> allSetsObservers = new LinkedHashSet<>();
    @SuppressWarnings("rawtypes")
    private final CacheByFishState<Set<Observer<AbstractSetAction>>>
        allSetsObserversCache = new CacheByFishState<>(__ -> ImmutableSet.copyOf(allSetsObservers));
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
    private double decayRateOfOpportunisticFadSetLocationValues = 0.6563603233600155;
    private double decayRateOfNonAssociatedSetLocationValues = 0.0;
    private double decayRateOfDolphinSetLocationValues = 1.2499749999999998;
    private double decayRateOfDeploymentLocationValues = 1.1709955387012643;
    private GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor;
    private List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations =
        ImmutableList.of(new ActiveFadLimitsFactory());
    // See https://github.com/nicolaspayette/tuna/issues/8 re: successful set probability
    private DoubleParameter successfulSetProbability = new FixedDoubleParameter(0.9231701);
    private Path locationValuesFile = INPUT_PATH.resolve("location_values.csv");
    private AlgorithmFactory<? extends FadInitializer> fadInitializerFactory;
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        pctHoldSpaceLeftModulationFunction =
        new LogisticFunctionFactory(0.15670573908905225, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        pctSetsRemainingModulationFunction =
        new LogisticFunctionFactory(EPSILON, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        numFadsInStockModulationFunction =
        new LogisticFunctionFactory(465.76938287575837, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        fadDeploymentPctActiveFadsLimitModulationFunction =
        new LogisticFunctionFactory(0.817463635675281, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        pctTravelTimeLeftModulationFunction =
        new LogisticFunctionFactory(0.10183241937374361, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        opportunisticFadSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionFactory(73.32224086132372, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        nonAssociatedSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionFactory(51.91162666081563, 5);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        dolphinSetTimeSinceLastVisitModulationFunction =
        new LogisticFunctionFactory(72.28852668100924, 5);
    private double actionDistanceExponent = 10;
    private double destinationDistanceExponent = 2;
    private Path maxCurrentSpeedsFile = INPUT_PATH.resolve("max_current_speeds.csv");
    private DoubleParameter fishValueCalculatorStandardDeviation = new FixedDoubleParameter(0);

    public DoubleParameter getFishValueCalculatorStandardDeviation() {
        return fishValueCalculatorStandardDeviation;
    }

    public void setFishValueCalculatorStandardDeviation(final DoubleParameter fishValueCalculatorStandardDeviation) {
        this.fishValueCalculatorStandardDeviation = fishValueCalculatorStandardDeviation;
    }

    public Path getMaxCurrentSpeedsFile() {
        return maxCurrentSpeedsFile;
    }

    public void setMaxCurrentSpeedsFile(Path maxCurrentSpeedsFile) {
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
    }

    @SuppressWarnings("rawtypes")
    public Set<Observer<AbstractSetAction>> getAllSetsObservers() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return allSetsObservers;
    }

    @SuppressWarnings("rawtypes")
    public void setAllSetsObservers(final Set<Observer<AbstractSetAction>> allSetsObservers) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.allSetsObservers = allSetsObservers;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getOpportunisticFadSetTimeSinceLastVisitModulationFunction() {
        return opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    public void setOpportunisticFadSetTimeSinceLastVisitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> opportunisticFadSetTimeSinceLastVisitModulationFunction
    ) {
        this.opportunisticFadSetTimeSinceLastVisitModulationFunction =
            opportunisticFadSetTimeSinceLastVisitModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getNonAssociatedSetTimeSinceLastVisitModulationFunction() {
        return nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    public void setNonAssociatedSetTimeSinceLastVisitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> nonAssociatedSetTimeSinceLastVisitModulationFunction
    ) {
        this.nonAssociatedSetTimeSinceLastVisitModulationFunction =
            nonAssociatedSetTimeSinceLastVisitModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getDolphinSetTimeSinceLastVisitModulationFunction() {
        return dolphinSetTimeSinceLastVisitModulationFunction;
    }

    public void setDolphinSetTimeSinceLastVisitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> dolphinSetTimeSinceLastVisitModulationFunction
    ) {
        this.dolphinSetTimeSinceLastVisitModulationFunction =
            dolphinSetTimeSinceLastVisitModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctHoldSpaceLeftModulationFunction() {
        return pctHoldSpaceLeftModulationFunction;
    }

    public void setPctHoldSpaceLeftModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> pctHoldSpaceLeftModulationFunction
    ) {
        this.pctHoldSpaceLeftModulationFunction = pctHoldSpaceLeftModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctTravelTimeLeftModulationFunction() {
        return pctTravelTimeLeftModulationFunction;
    }

    public void setPctTravelTimeLeftModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> pctTravelTimeLeftModulationFunction
    ) {
        this.pctTravelTimeLeftModulationFunction = pctTravelTimeLeftModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getPctSetsRemainingModulationFunction() {
        return pctSetsRemainingModulationFunction;
    }

    public void setPctSetsRemainingModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> pctSetsRemainingModulationFunction
    ) {
        this.pctSetsRemainingModulationFunction = pctSetsRemainingModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getFadDeploymentPctActiveFadsLimitModulationFunction() {
        return fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    public void setFadDeploymentPctActiveFadsLimitModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> fadDeploymentPctActiveFadsLimitModulationFunction
    ) {
        this.fadDeploymentPctActiveFadsLimitModulationFunction =
            fadDeploymentPctActiveFadsLimitModulationFunction;
    }

    public AlgorithmFactory<? extends DoubleUnaryOperator> getNumFadsInStockModulationFunction() {
        return numFadsInStockModulationFunction;
    }

    public void setNumFadsInStockModulationFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> numFadsInStockModulationFunction
    ) {
        this.numFadsInStockModulationFunction = numFadsInStockModulationFunction;
    }

    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    public void setFadInitializerFactory(final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory) {
        this.fadInitializerFactory = fadInitializerFactory;
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
        @SuppressWarnings("rawtypes") final Set<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers
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

        final MersenneTwisterFast rng = fishState.getRandom();
        final GlobalBiology globalBiology = fishState.getBiology();
        @SuppressWarnings("unchecked") final FadManager<B, F> fadManager = new FadManager<>(
            (FadMap<B, F>) fishState.getFadMap(),
            fadInitializerFactory.apply(fishState),
            fadDeploymentObserversCache.get(fishState),
            allSetsObserversCache.get(fishState),
            fadSetObserversCache.get(fishState),
            nonAssociatedSetObserversCache.get(fishState),
            dolphinSetObserversCache.get(fishState),
            Optional.of(biomassLostMonitor),
            actionSpecificRegulations,
            new UnreliableFishValueCalculator(globalBiology, rng, fishValueCalculatorStandardDeviation.apply(rng))
        );
        return fadManager;
    }

    Stream<AttractionField> attractionFields(final FishState fishState) {

        final PurseSeinerActionClassToDouble maxCurrentSpeed =
            PurseSeinerActionClassToDouble.fromFile(
                getMaxCurrentSpeedsFile(), "action", "speed"
            );

        final GlobalSetAttractionModulator globalSetAttractionModulator =
            new GlobalSetAttractionModulator(
                pctHoldSpaceLeftModulationFunction.apply(fishState),
                pctSetsRemainingModulationFunction.apply(fishState)
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
                    opportunisticFadSetTimeSinceLastVisitModulationFunction.apply(fishState),
                    maxCurrentSpeed.applyAsDouble(OpportunisticFadSetAction.class)
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
                    nonAssociatedSetTimeSinceLastVisitModulationFunction.apply(fishState),
                    maxCurrentSpeed.applyAsDouble(NonAssociatedSetAction.class)
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
                    dolphinSetTimeSinceLastVisitModulationFunction.apply(fishState),
                    maxCurrentSpeed.applyAsDouble(DolphinSetAction.class)
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
                    fadDeploymentPctActiveFadsLimitModulationFunction.apply(fishState),
                    numFadsInStockModulationFunction.apply(fishState)
                ),
                FadDeploymentAction.class,
                actionDistanceExponent,
                destinationDistanceExponent
            ),
            new PortAttractionField(
                new PortAttractionModulator(
                    pctHoldSpaceLeftModulationFunction.apply(fishState),
                    pctTravelTimeLeftModulationFunction.apply(fishState)
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
