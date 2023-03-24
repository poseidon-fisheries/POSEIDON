package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.Monitors;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculator;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.quantity.Mass;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

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
    private GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor;
    private List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations =
        ImmutableList.of(new ActiveFadLimitsFactory());
    // See https://github.com/nicolaspayette/tuna/issues/8 re: successful set probability
    private DoubleParameter successfulSetProbability = new FixedDoubleParameter(0.9231701);
    private InputPath locationValuesFile;
    private AlgorithmFactory<? extends FadInitializer> fadInitializerFactory;
    private DoubleParameter fishValueCalculatorStandardDeviation = new FixedDoubleParameter(0);

    public PurseSeineGearFactory() {
    }

    public PurseSeineGearFactory(
        final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory
    ) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    public DoubleParameter getFishValueCalculatorStandardDeviation() {
        return fishValueCalculatorStandardDeviation;
    }

    public void setFishValueCalculatorStandardDeviation(final DoubleParameter fishValueCalculatorStandardDeviation) {
        this.fishValueCalculatorStandardDeviation = fishValueCalculatorStandardDeviation;
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

    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    public void setFadInitializerFactory(final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory) {
        this.fadInitializerFactory = fadInitializerFactory;
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

    public void addMonitors(final Monitors monitors) {
        getFadDeploymentObservers()
            .addAll(monitors.getFadDeploymentMonitors());
        getAllSetsObservers()
            .addAll(monitors.getAllSetsMonitors());
        getFadSetObservers()
            .addAll(monitors.getFadSetMonitors());
        getNonAssociatedSetObservers()
            .addAll(monitors.getNonAssociatedSetMonitors());
        getDolphinSetObservers()
            .addAll(monitors.getDolphinSetMonitors());
        setBiomassLostMonitor(monitors.getBiomassLostMonitor());
    }
}
