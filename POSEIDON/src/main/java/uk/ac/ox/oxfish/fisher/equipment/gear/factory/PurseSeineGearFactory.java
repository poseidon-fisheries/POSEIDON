package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.FishValueCalculator;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.Monitors;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.oxfish.utility.parameters.ParameterTable;
import uk.ac.ox.poseidon.agents.core.AtomicLongMapYearlyActionCounter;
import uk.ac.ox.poseidon.common.api.Observer;

import javax.measure.quantity.Mass;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
public abstract class PurseSeineGearFactory implements AlgorithmFactory<PurseSeineGear> {

    private static final LocationFisherValuesByActionCache locationValuesCache =
        new LocationFisherValuesByActionCache();

    private Set<Observer<FadDeploymentAction>> fadDeploymentObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<FadDeploymentAction>>> fadDeploymentObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(fadDeploymentObservers));
    private Set<Observer<AbstractSetAction>> allSetsObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<AbstractSetAction>>>
        allSetsObserversCache = new CacheByFishState<>(__ -> ImmutableSet.copyOf(allSetsObservers));
    private Set<Observer<AbstractFadSetAction>> fadSetObservers = new LinkedHashSet<>();
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
    private IntegerParameter targetYear;
    private AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializer;
    private AlgorithmFactory<? extends FishValueCalculator> fishValueCalculator;
    private AlgorithmFactory<? extends ParameterTable> otherParameters;

    @SuppressWarnings("WeakerAccess")
    public PurseSeineGearFactory() {
    }

    @SuppressWarnings("WeakerAccess")
    public PurseSeineGearFactory(
        final IntegerParameter targetYear,
        final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializer,
        final AlgorithmFactory<? extends FishValueCalculator> fishValueCalculator,
        final AlgorithmFactory<? extends ParameterTable> otherParameters
    ) {
        this.targetYear = targetYear;
        this.fadInitializer = fadInitializer;
        this.fishValueCalculator = fishValueCalculator;
        this.otherParameters = otherParameters;
    }

    public AlgorithmFactory<? extends FishValueCalculator> getFishValueCalculator() {
        return fishValueCalculator;
    }

    public void setFishValueCalculator(final AlgorithmFactory<? extends FishValueCalculator> fishValueCalculator) {
        this.fishValueCalculator = fishValueCalculator;
    }

    public AlgorithmFactory<? extends FadInitializer<?, ?>> getFadInitializer() {
        return fadInitializer;
    }

    public void setFadInitializer(final AlgorithmFactory<? extends FadInitializer<?, ?>> fadInitializer) {
        this.fadInitializer = fadInitializer;
    }

    @SuppressWarnings("unused")
    public GroupingMonitor<Species, BiomassLostEvent, Double, Mass> grabBiomassLostMonitor() {
        return biomassLostMonitor;
    }

    public void addMonitors(final Monitors monitors) {
        grabFadDeploymentObservers()
            .addAll(monitors.grabFadDeploymentMonitors());
        grabAllSetsObservers()
            .addAll(monitors.grabAllSetsMonitors());
        grabFadSetObservers()
            .addAll(monitors.grabFadSetMonitors());
        grabNonAssociatedSetObservers()
            .addAll(monitors.grabNonAssociatedSetMonitors());
        grabDolphinSetObservers()
            .addAll(monitors.grabDolphinSetMonitors());
        setBiomassLostMonitor(monitors.grabBiomassLostMonitor());
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Set<Observer<FadDeploymentAction>> grabFadDeploymentObservers() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return fadDeploymentObservers;
    }

    @SuppressWarnings("WeakerAccess")
    public Set<Observer<AbstractSetAction>> grabAllSetsObservers() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return allSetsObservers;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Set<Observer<AbstractFadSetAction>> grabFadSetObservers() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return fadSetObservers;
    }

    @SuppressWarnings({"unused", "rawtypes", "WeakerAccess"})
    public Set<Observer<NonAssociatedSetAction>> grabNonAssociatedSetObservers() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return nonAssociatedSetObservers;
    }

    @SuppressWarnings({"rawtypes", "WeakerAccess"})
    public Set<Observer<DolphinSetAction>> grabDolphinSetObservers() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return dolphinSetObservers;
    }

    @SuppressWarnings("WeakerAccess")
    public void setBiomassLostMonitor(
        final GroupingMonitor<Species, BiomassLostEvent, Double,
            Mass> biomassLostMonitor
    ) {
        this.biomassLostMonitor = biomassLostMonitor;
    }

    public void setAllSetsObservers(final Set<Observer<AbstractSetAction>> allSetsObservers) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.allSetsObservers = allSetsObservers;
    }

    @SuppressWarnings("unused")
    public void setFadSetObservers(
        final Set<Observer<AbstractFadSetAction>> fadSetObservers
    ) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.fadSetObservers = fadSetObservers;
    }

    public void setDolphinSetObservers(
        @SuppressWarnings("rawtypes") final Set<Observer<DolphinSetAction>> dolphinSetObservers
    ) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.dolphinSetObservers = dolphinSetObservers;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetObservers(
        @SuppressWarnings("rawtypes") final Set<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers
    ) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.nonAssociatedSetObservers = nonAssociatedSetObservers;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentObservers(
        final Set<Observer<FadDeploymentAction>> fadDeploymentObservers
    ) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.fadDeploymentObservers = fadDeploymentObservers;
    }

    @Override
    public PurseSeineGear apply(final FishState fishState) {
        final Map<String, ? extends DoubleParameter> parameters =
            getOtherParameters()
                .apply(fishState)
                .getParameters(getTargetYear().getIntValue());
        final MersenneTwisterFast rng = fishState.getRandom();
        return makeGear(
            makeFadManager(fishState),
            parameters.get("successful_set_probability").applyAsDouble(rng),
            parameters.get("max_allowable_shear").applyAsDouble(rng)
        );
    }

    public AlgorithmFactory<? extends ParameterTable> getOtherParameters() {
        return otherParameters;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    protected abstract PurseSeineGear makeGear(
        FadManager fadManager,
        double successfulSetProbability,
        double maxAllowableShear
    );

    FadManager makeFadManager(final FishState fishState) {
        checkNotNull(fadInitializer);
        final MersenneTwisterFast rng = fishState.getRandom();
        final GlobalBiology globalBiology = fishState.getBiology();
        return new FadManager(
            fishState.getFadMap(),
            fadInitializer.apply(fishState),
            AtomicLongMapYearlyActionCounter.create(),
            fadDeploymentObserversCache.get(fishState),
            allSetsObserversCache.get(fishState),
            fadSetObserversCache.get(fishState),
            nonAssociatedSetObserversCache.get(fishState),
            dolphinSetObserversCache.get(fishState),
            Optional.of(biomassLostMonitor),
            fishValueCalculator.apply(fishState)
        );
    }

    public void setOtherParameters(final AlgorithmFactory<? extends ParameterTable> otherParameters) {
        this.otherParameters = otherParameters;
    }
}
