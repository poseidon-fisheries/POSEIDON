package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ec.util.MersenneTwisterFast;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.LocationFisherValuesByActionCache;
import uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.DurationSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.*;
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.quantity.Mass;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Double.MAX_VALUE;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.model.scenario.TunaScenario.input;

public class PurseSeineGearFactory implements AlgorithmFactory<PurseSeineGear> {

    private static final LocationFisherValuesByActionCache locationValuesCache = new LocationFisherValuesByActionCache();

    private final SetDurationSamplersFactory setDurationSamplers = new SetDurationSamplersFactory();
    private final CacheByFishState<Map<Class<? extends AbstractSetAction>, DurationSampler>> setDurationSamplersCache =
        new CacheByFishState<>(setDurationSamplers);

    private final CatchSamplersFactory catchSamplers = new CatchSamplersFactory();
    private final CacheByFishState<Map<Class<? extends AbstractSetAction>, CatchSampler>> catchSamplersCache =
        new CacheByFishState<>(catchSamplers);

    private Set<Observer<FadDeploymentAction>> fadDeploymentObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<FadDeploymentAction>>> fadDeploymentObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(fadDeploymentObservers));
    private Set<Observer<AbstractFadSetAction>> fadSetObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<AbstractFadSetAction>>> fadSetObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(fadSetObservers));
    private Set<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<NonAssociatedSetAction>>> nonAssociatedSetObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(nonAssociatedSetObservers));
    private Set<Observer<DolphinSetAction>> dolphinSetObservers = new LinkedHashSet<>();
    private final CacheByFishState<Set<Observer<DolphinSetAction>>> dolphinSetObserversCache =
        new CacheByFishState<>(__ -> ImmutableSet.copyOf(dolphinSetObservers));
    private double decayRateOfOpportunisticFadSetLocationValues = 0.01;
    private double decayRateOfNonAssociatedSetLocationValues = 0.01;
    private double decayRateOfDolphinSetLocationValues = 0.01;
    private double decayRateOfDeploymentLocationValues = 0.01;
    private GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor;
    private List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations =
        ImmutableList.of(new ActiveFadLimitsFactory());
    private int initialNumberOfFads = 999999; // TODO: find plausible value and allow boats to refill
    private FadInitializerFactory fadInitializerFactory = new FadInitializerFactory();
    // See https://github.com/nicolaspayette/tuna/issues/8 re: successful set probability
    private DoubleParameter successfulSetProbability = new FixedDoubleParameter(0.9231701);
    private Path locationValuesFile = input("location_values.csv");
    private double pctHoldSpaceLeftLogisticMidpoint = 0.9;
    private double pctHoldSpaceLeftLogisticSteepness = MAX_VALUE;
    private double pctTravelTimeLeftLogisticMidpoint = 0.9;
    private double pctTravelTimeLeftLogisticSteepness = MAX_VALUE;
    private double fadSetPctHoldAvailableLogisticMidpoint = 0.5;
    private double fadSetPctHoldAvailableLogisticSteepness = 1;
    private double fadSetPctSetsRemainingLogisticMidpoint = 0.5;
    private double fadSetPctSetsRemainingLogisticSteepness = 1;
    private double opportunisticFadSetPctHoldAvailableLogisticMidpoint = 0.5;
    private double opportunisticFadSetPctHoldAvailableLogisticSteepness = 1;
    private double opportunisticFadSetPctSetsRemainingLogisticMidpoint = 0.5;
    private double opportunisticFadSetPctSetsRemainingLogisticSteepness = 1;
    private double opportunisticFadSetTimeSinceLastVisitLogisticMidpoint = 5;
    private double opportunisticFadSetTimeSinceLastVisitLogisticSteepness = 1;
    private double nonAssociatedSetPctHoldAvailableLogisticMidpoint = 0.5;
    private double nonAssociatedSetPctHoldAvailableLogisticSteepness = 1;
    private double nonAssociatedSetPctSetsRemainingLogisticMidpoint = 5;
    private double nonAssociatedSetPctSetsRemainingLogisticSteepness = 1;
    private double nonAssociatedSetTimeSinceLastVisitLogisticMidpoint = 5;
    private double nonAssociatedSetTimeSinceLastVisitLogisticSteepness = 1;
    private double dolphinSetPctHoldAvailableLogisticMidpoint = 0.5;
    private double dolphinSetPctHoldAvailableLogisticSteepness = 1;
    private double dolphinSetPctSetsRemainingLogisticMidpoint = 0.5;
    private double dolphinSetPctSetsRemainingLogisticSteepness = 1;
    private double dolphinSetTimeSinceLastVisitLogisticMidpoint = 5;
    private double dolphinSetTimeSinceLastVisitLogisticSteepness = 1;
    private double fadDeploymentPctActiveFadsLimitLogisticMidpoint = 0.5;
    private double fadDeploymentPctActiveFadsLimitLogisticSteepness = 1;

    public Set<Observer<DolphinSetAction>> getDolphinSetObservers() {
        return dolphinSetObservers;
    }

    public void setDolphinSetObservers(Set<Observer<DolphinSetAction>> dolphinSetObservers) {
        this.dolphinSetObservers = dolphinSetObservers;
    }

    public CatchSamplersFactory getCatchSamplers() {
        return catchSamplers;
    }

    @SuppressWarnings("unused")
    public double getPctHoldSpaceLeftLogisticMidpoint() { return pctHoldSpaceLeftLogisticMidpoint; }

    @SuppressWarnings("unused")
    public void setPctHoldSpaceLeftLogisticMidpoint(final double pctHoldSpaceLeftLogisticMidpoint) {
        this.pctHoldSpaceLeftLogisticMidpoint = pctHoldSpaceLeftLogisticMidpoint;
    }

    public double getPctHoldSpaceLeftLogisticSteepness() { return pctHoldSpaceLeftLogisticSteepness; }

    public void setPctHoldSpaceLeftLogisticSteepness(final double pctHoldSpaceLeftLogisticSteepness) {
        this.pctHoldSpaceLeftLogisticSteepness = pctHoldSpaceLeftLogisticSteepness;
    }

    public double getPctTravelTimeLeftLogisticMidpoint() { return pctTravelTimeLeftLogisticMidpoint; }

    @SuppressWarnings("unused")
    public void setPctTravelTimeLeftLogisticMidpoint(final double pctTravelTimeLeftLogisticMidpoint) {
        this.pctTravelTimeLeftLogisticMidpoint = pctTravelTimeLeftLogisticMidpoint;
    }

    public double getPctTravelTimeLeftLogisticSteepness() { return pctTravelTimeLeftLogisticSteepness; }

    public void setPctTravelTimeLeftLogisticSteepness(final double pctTravelTimeLeftLogisticSteepness) {
        this.pctTravelTimeLeftLogisticSteepness = pctTravelTimeLeftLogisticSteepness;
    }

    public double getFadDeploymentPctActiveFadsLimitLogisticMidpoint() { return fadDeploymentPctActiveFadsLimitLogisticMidpoint; }

    public void setFadDeploymentPctActiveFadsLimitLogisticMidpoint(final double fadDeploymentPctActiveFadsLimitLogisticMidpoint) {
        this.fadDeploymentPctActiveFadsLimitLogisticMidpoint = fadDeploymentPctActiveFadsLimitLogisticMidpoint;
    }

    public double getFadDeploymentPctActiveFadsLimitLogisticSteepness() { return fadDeploymentPctActiveFadsLimitLogisticSteepness; }

    public void setFadDeploymentPctActiveFadsLimitLogisticSteepness(final double fadDeploymentPctActiveFadsLimitLogisticSteepness) {
        this.fadDeploymentPctActiveFadsLimitLogisticSteepness = fadDeploymentPctActiveFadsLimitLogisticSteepness;
    }

    public double getOpportunisticFadSetPctHoldAvailableLogisticMidpoint() { return opportunisticFadSetPctHoldAvailableLogisticMidpoint; }

    public void setOpportunisticFadSetPctHoldAvailableLogisticMidpoint(final double opportunisticFadSetPctHoldAvailableLogisticMidpoint) {
        this.opportunisticFadSetPctHoldAvailableLogisticMidpoint = opportunisticFadSetPctHoldAvailableLogisticMidpoint;
    }

    public double getOpportunisticFadSetPctHoldAvailableLogisticSteepness() { return opportunisticFadSetPctHoldAvailableLogisticSteepness; }

    public void setOpportunisticFadSetPctHoldAvailableLogisticSteepness(final double opportunisticFadSetPctHoldAvailableLogisticSteepness) {
        this.opportunisticFadSetPctHoldAvailableLogisticSteepness =
            opportunisticFadSetPctHoldAvailableLogisticSteepness;
    }

    public double getOpportunisticFadSetPctSetsRemainingLogisticMidpoint() { return opportunisticFadSetPctSetsRemainingLogisticMidpoint; }

    public void setOpportunisticFadSetPctSetsRemainingLogisticMidpoint(final double opportunisticFadSetPctSetsRemainingLogisticMidpoint) {
        this.opportunisticFadSetPctSetsRemainingLogisticMidpoint = opportunisticFadSetPctSetsRemainingLogisticMidpoint;
    }

    public double getOpportunisticFadSetPctSetsRemainingLogisticSteepness() { return opportunisticFadSetPctSetsRemainingLogisticSteepness; }

    public void setOpportunisticFadSetPctSetsRemainingLogisticSteepness(final double opportunisticFadSetPctSetsRemainingLogisticSteepness) {
        this.opportunisticFadSetPctSetsRemainingLogisticSteepness =
            opportunisticFadSetPctSetsRemainingLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetTimeSinceLastVisitLogisticMidpoint() { return opportunisticFadSetTimeSinceLastVisitLogisticMidpoint; }

    public void setOpportunisticFadSetTimeSinceLastVisitLogisticMidpoint(final double opportunisticFadSetTimeSinceLastVisitLogisticMidpoint) {
        this.opportunisticFadSetTimeSinceLastVisitLogisticMidpoint =
            opportunisticFadSetTimeSinceLastVisitLogisticMidpoint;
    }

    public double getOpportunisticFadSetTimeSinceLastVisitLogisticSteepness() { return opportunisticFadSetTimeSinceLastVisitLogisticSteepness; }

    public void setOpportunisticFadSetTimeSinceLastVisitLogisticSteepness(final double opportunisticFadSetTimeSinceLastVisitLogisticSteepness) {
        this.opportunisticFadSetTimeSinceLastVisitLogisticSteepness =
            opportunisticFadSetTimeSinceLastVisitLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetPctHoldAvailableLogisticMidpoint() { return nonAssociatedSetPctHoldAvailableLogisticMidpoint; }

    public void setNonAssociatedSetPctHoldAvailableLogisticMidpoint(final double nonAssociatedSetPctHoldAvailableLogisticMidpoint) {
        this.nonAssociatedSetPctHoldAvailableLogisticMidpoint = nonAssociatedSetPctHoldAvailableLogisticMidpoint;
    }

    public double getNonAssociatedSetPctHoldAvailableLogisticSteepness() { return nonAssociatedSetPctHoldAvailableLogisticSteepness; }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetPctHoldAvailableLogisticSteepness(final double nonAssociatedSetPctHoldAvailableLogisticSteepness) {
        this.nonAssociatedSetPctHoldAvailableLogisticSteepness = nonAssociatedSetPctHoldAvailableLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetPctSetsRemainingLogisticMidpoint() { return nonAssociatedSetPctSetsRemainingLogisticMidpoint; }

    public void setNonAssociatedSetPctSetsRemainingLogisticMidpoint(final double nonAssociatedSetPctSetsRemainingLogisticMidpoint) {
        this.nonAssociatedSetPctSetsRemainingLogisticMidpoint = nonAssociatedSetPctSetsRemainingLogisticMidpoint;
    }

    public double getNonAssociatedSetPctSetsRemainingLogisticSteepness() { return nonAssociatedSetPctSetsRemainingLogisticSteepness; }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetPctSetsRemainingLogisticSteepness(final double nonAssociatedSetPctSetsRemainingLogisticSteepness) {
        this.nonAssociatedSetPctSetsRemainingLogisticSteepness = nonAssociatedSetPctSetsRemainingLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetTimeSinceLastVisitLogisticMidpoint() { return nonAssociatedSetTimeSinceLastVisitLogisticMidpoint; }

    public void setNonAssociatedSetTimeSinceLastVisitLogisticMidpoint(final double nonAssociatedSetTimeSinceLastVisitLogisticMidpoint) {
        this.nonAssociatedSetTimeSinceLastVisitLogisticMidpoint = nonAssociatedSetTimeSinceLastVisitLogisticMidpoint;
    }

    public double getNonAssociatedSetTimeSinceLastVisitLogisticSteepness() { return nonAssociatedSetTimeSinceLastVisitLogisticSteepness; }

    public void setNonAssociatedSetTimeSinceLastVisitLogisticSteepness(final double nonAssociatedSetTimeSinceLastVisitLogisticSteepness) {
        this.nonAssociatedSetTimeSinceLastVisitLogisticSteepness = nonAssociatedSetTimeSinceLastVisitLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetPctHoldAvailableLogisticMidpoint() { return dolphinSetPctHoldAvailableLogisticMidpoint; }

    @SuppressWarnings("unused")
    public void setDolphinSetPctHoldAvailableLogisticMidpoint(final double dolphinSetPctHoldAvailableLogisticMidpoint) {
        this.dolphinSetPctHoldAvailableLogisticMidpoint = dolphinSetPctHoldAvailableLogisticMidpoint;
    }

    public double getDolphinSetPctHoldAvailableLogisticSteepness() { return dolphinSetPctHoldAvailableLogisticSteepness; }

    public void setDolphinSetPctHoldAvailableLogisticSteepness(final double dolphinSetPctHoldAvailableLogisticSteepness) {
        this.dolphinSetPctHoldAvailableLogisticSteepness = dolphinSetPctHoldAvailableLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetPctSetsRemainingLogisticMidpoint() { return dolphinSetPctSetsRemainingLogisticMidpoint; }

    @SuppressWarnings("unused")
    public void setDolphinSetPctSetsRemainingLogisticMidpoint(final double dolphinSetPctSetsRemainingLogisticMidpoint) {
        this.dolphinSetPctSetsRemainingLogisticMidpoint = dolphinSetPctSetsRemainingLogisticMidpoint;
    }

    public double getDolphinSetPctSetsRemainingLogisticSteepness() { return dolphinSetPctSetsRemainingLogisticSteepness; }

    public void setDolphinSetPctSetsRemainingLogisticSteepness(final double dolphinSetPctSetsRemainingLogisticSteepness) {
        this.dolphinSetPctSetsRemainingLogisticSteepness = dolphinSetPctSetsRemainingLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetTimeSinceLastVisitLogisticMidpoint() { return dolphinSetTimeSinceLastVisitLogisticMidpoint; }

    @SuppressWarnings("unused")
    public void setDolphinSetTimeSinceLastVisitLogisticMidpoint(final double dolphinSetTimeSinceLastVisitLogisticMidpoint) {
        this.dolphinSetTimeSinceLastVisitLogisticMidpoint = dolphinSetTimeSinceLastVisitLogisticMidpoint;
    }

    public double getDolphinSetTimeSinceLastVisitLogisticSteepness() { return dolphinSetTimeSinceLastVisitLogisticSteepness; }

    public void setDolphinSetTimeSinceLastVisitLogisticSteepness(final double dolphinSetTimeSinceLastVisitLogisticSteepness) {
        this.dolphinSetTimeSinceLastVisitLogisticSteepness = dolphinSetTimeSinceLastVisitLogisticSteepness;
    }

    public double getFadSetPctHoldAvailableLogisticMidpoint() { return fadSetPctHoldAvailableLogisticMidpoint; }

    public void setFadSetPctHoldAvailableLogisticMidpoint(final double fadSetPctHoldAvailableLogisticMidpoint) {
        this.fadSetPctHoldAvailableLogisticMidpoint = fadSetPctHoldAvailableLogisticMidpoint;
    }

    public double getFadSetPctHoldAvailableLogisticSteepness() { return fadSetPctHoldAvailableLogisticSteepness; }

    public void setFadSetPctHoldAvailableLogisticSteepness(final double fadSetPctHoldAvailableLogisticSteepness) {
        this.fadSetPctHoldAvailableLogisticSteepness = fadSetPctHoldAvailableLogisticSteepness;
    }

    public double getFadSetPctSetsRemainingLogisticMidpoint() { return fadSetPctSetsRemainingLogisticMidpoint; }

    public void setFadSetPctSetsRemainingLogisticMidpoint(final double fadSetPctSetsRemainingLogisticMidpoint) {
        this.fadSetPctSetsRemainingLogisticMidpoint = fadSetPctSetsRemainingLogisticMidpoint;
    }

    public double getFadSetPctSetsRemainingLogisticSteepness() { return fadSetPctSetsRemainingLogisticSteepness; }

    public void setFadSetPctSetsRemainingLogisticSteepness(final double fadSetPctSetsRemainingLogisticSteepness) {
        this.fadSetPctSetsRemainingLogisticSteepness = fadSetPctSetsRemainingLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public GroupingMonitor<Species, BiomassLostEvent, Double, Mass> getBiomassLostMonitor() { return biomassLostMonitor; }

    public void setBiomassLostMonitor(GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor) {
        this.biomassLostMonitor = biomassLostMonitor;
    }

    @SuppressWarnings("unused")
    public List<AlgorithmFactory<? extends ActionSpecificRegulation>> getActionSpecificRegulations() {
        return actionSpecificRegulations;
    }

    public void setActionSpecificRegulations(List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations) {
        this.actionSpecificRegulations = actionSpecificRegulations;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getSuccessfulSetProbability() { return successfulSetProbability; }

    @SuppressWarnings("unused")
    public void setSuccessfulSetProbability(DoubleParameter successfulSetProbability) {
        this.successfulSetProbability = successfulSetProbability;
    }

    @SuppressWarnings("unused")
    public FadInitializerFactory getFadInitializerFactory() { return fadInitializerFactory; }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(
        FadInitializerFactory fadInitializerFactory
    ) { this.fadInitializerFactory = fadInitializerFactory; }

    @SuppressWarnings("unused")
    public int getInitialNumberOfFads() { return initialNumberOfFads; }

    @SuppressWarnings("unused")
    public void setInitialNumberOfFads(int initialNumberOfFads) {
        this.initialNumberOfFads = initialNumberOfFads;
    }

    @SuppressWarnings("unused")
    public Set<Observer<FadDeploymentAction>> getFadDeploymentObservers() { return fadDeploymentObservers; }

    @SuppressWarnings("unused")
    public void setFadDeploymentObservers(Set<Observer<FadDeploymentAction>> fadDeploymentObservers) {
        this.fadDeploymentObservers = fadDeploymentObservers;
    }

    @SuppressWarnings("unused")
    public Set<Observer<AbstractFadSetAction>> getFadSetObservers() { return fadSetObservers; }

    @SuppressWarnings("unused")
    public void setFadSetObservers(Set<Observer<AbstractFadSetAction>> fadSetObservers) {
        this.fadSetObservers = fadSetObservers;
    }

    @SuppressWarnings("unused")
    public Set<Observer<NonAssociatedSetAction>> getNonAssociatedSetObservers() { return nonAssociatedSetObservers; }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetObservers(Set<Observer<NonAssociatedSetAction>> nonAssociatedSetObservers) {
        this.nonAssociatedSetObservers = nonAssociatedSetObservers;
    }

    @Override
    public PurseSeineGear apply(FishState fishState) {

        final ActiveActionRegulations actionSpecificRegulations = new ActiveActionRegulations(
            this.actionSpecificRegulations.stream()
                .map(factory -> factory.apply(fishState))
                .collect(toList())
        );

        final FadManager fadManager = new FadManager(
            fishState.getFadMap(),
            fadInitializerFactory.apply(fishState),
            initialNumberOfFads,
            fadDeploymentObserversCache.get(fishState),
            fadSetObserversCache.get(fishState),
            nonAssociatedSetObserversCache.get(fishState),
            dolphinSetObserversCache.get(fishState),
            Optional.of(biomassLostMonitor),
            actionSpecificRegulations
        );

        final MersenneTwisterFast rng = fishState.getRandom();

        return new PurseSeineGear(
            fadManager,
            setDurationSamplersCache.get(fishState),
            catchSamplersCache.get(fishState),
            attractionFields()::iterator,
            successfulSetProbability.apply(rng)
        );
    }

    private Stream<AttractionField> attractionFields() {
        return Stream.of(
            new ActionAttractionField(
                new FadLocationValues(),
                new SetAttractionModulator(
                    FadSetAction.class,
                    fadSetPctHoldAvailableLogisticMidpoint,
                    fadSetPctHoldAvailableLogisticSteepness,
                    fadSetPctSetsRemainingLogisticMidpoint,
                    fadSetPctSetsRemainingLogisticSteepness
                ),
                FadSetAction.class
            ),
            new ActionAttractionField(
                new OpportunisticFadSetLocationValues(
                    fisher -> loadLocationValues(fisher, OpportunisticFadSetAction.class),
                    getDecayRateOfOpportunisticFadSetLocationValues()
                ),
                new SetAttractionModulator(
                    OpportunisticFadSetAction.class,
                    opportunisticFadSetPctHoldAvailableLogisticMidpoint,
                    opportunisticFadSetPctHoldAvailableLogisticSteepness,
                    opportunisticFadSetPctSetsRemainingLogisticMidpoint,
                    opportunisticFadSetPctSetsRemainingLogisticSteepness,
                    opportunisticFadSetTimeSinceLastVisitLogisticMidpoint,
                    opportunisticFadSetTimeSinceLastVisitLogisticSteepness
                ),
                OpportunisticFadSetAction.class
            ),
            new ActionAttractionField(
                new NonAssociatedSetLocationValues(
                    fisher -> loadLocationValues(fisher, NonAssociatedSetAction.class),
                    getDecayRateOfNonAssociatedSetLocationValues()
                ),
                new SetAttractionModulator(
                    NonAssociatedSetAction.class,
                    nonAssociatedSetPctHoldAvailableLogisticMidpoint,
                    nonAssociatedSetPctHoldAvailableLogisticSteepness,
                    nonAssociatedSetPctSetsRemainingLogisticMidpoint,
                    nonAssociatedSetPctSetsRemainingLogisticSteepness,
                    nonAssociatedSetTimeSinceLastVisitLogisticMidpoint,
                    nonAssociatedSetTimeSinceLastVisitLogisticSteepness
                ),
                NonAssociatedSetAction.class
            ),
            new ActionAttractionField(
                new DolphinSetLocationValues(
                    fisher -> loadLocationValues(fisher, DolphinSetAction.class),
                    getDecayRateOfDolphinSetLocationValues()
                ),
                new SetAttractionModulator(
                    DolphinSetAction.class,
                    dolphinSetPctHoldAvailableLogisticMidpoint,
                    dolphinSetPctHoldAvailableLogisticSteepness,
                    dolphinSetPctSetsRemainingLogisticMidpoint,
                    dolphinSetPctSetsRemainingLogisticSteepness,
                    dolphinSetTimeSinceLastVisitLogisticMidpoint,
                    dolphinSetTimeSinceLastVisitLogisticSteepness
                ),
                DolphinSetAction.class
            ),
            new ActionAttractionField(
                new DeploymentLocationValues(
                    fisher -> loadLocationValues(fisher, FadDeploymentAction.class),
                    getDecayRateOfDeploymentLocationValues()
                ),
                new DeploymentLocationsAttractionModulator(
                    fadDeploymentPctActiveFadsLimitLogisticMidpoint,
                    fadDeploymentPctActiveFadsLimitLogisticSteepness
                ),
                FadDeploymentAction.class
            ),
            new PortAttractionField(new PortAttractionModulator(
                pctHoldSpaceLeftLogisticMidpoint,
                pctHoldSpaceLeftLogisticSteepness,
                pctTravelTimeLeftLogisticMidpoint,
                pctTravelTimeLeftLogisticSteepness
            ))
        );
    }

    private Map<Int2D, Double> loadLocationValues(Fisher fisher, Class<? extends PurseSeinerAction> actionClass) {
        return locationValuesCache.getLocationValues(locationValuesFile, TARGET_YEAR, fisher, actionClass);
    }

    @SuppressWarnings("WeakerAccess")
    public double getDecayRateOfOpportunisticFadSetLocationValues() { return decayRateOfOpportunisticFadSetLocationValues; }

    @SuppressWarnings("unused")
    public void setDecayRateOfOpportunisticFadSetLocationValues(final double decayRateOfOpportunisticFadSetLocationValues) {
        this.decayRateOfOpportunisticFadSetLocationValues = decayRateOfOpportunisticFadSetLocationValues;
    }

    public double getDecayRateOfNonAssociatedSetLocationValues() { return decayRateOfNonAssociatedSetLocationValues; }

    public void setDecayRateOfNonAssociatedSetLocationValues(final double decayRateOfNonAssociatedSetLocationValues) {
        this.decayRateOfNonAssociatedSetLocationValues = decayRateOfNonAssociatedSetLocationValues;
    }

    public double getDecayRateOfDolphinSetLocationValues() { return decayRateOfDolphinSetLocationValues; }

    public void setDecayRateOfDolphinSetLocationValues(final double decayRateOfDolphinSetLocationValues) {
        this.decayRateOfDolphinSetLocationValues = decayRateOfDolphinSetLocationValues;
    }

    public double getDecayRateOfDeploymentLocationValues() { return decayRateOfDeploymentLocationValues; }

    public void setDecayRateOfDeploymentLocationValues(final double decayRateOfDeploymentLocationValues) {
        this.decayRateOfDeploymentLocationValues = decayRateOfDeploymentLocationValues;
    }

    public Path getLocationValuesFile() { return locationValuesFile; }

    public void setLocationValuesFile(final Path locationValuesFile) {
        this.locationValuesFile = locationValuesFile;
    }

}
