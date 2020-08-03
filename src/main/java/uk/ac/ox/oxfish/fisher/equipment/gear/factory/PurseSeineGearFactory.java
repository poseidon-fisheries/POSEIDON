package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.purseseiner.MakeUnassociatedSet;
import uk.ac.ox.oxfish.fisher.equipment.fads.BiomassLostEvent;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.CatchSampler;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.GroupingMonitor;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveActionRegulations;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.model.scenario.TunaScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import javax.measure.quantity.Mass;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSortedMap.toImmutableSortedMap;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class PurseSeineGearFactory implements AlgorithmFactory<PurseSeineGear> {

    private final WeakHashMap<FishState, Set<Observer<DeployFad>>> fadDeploymentObserversCache = new WeakHashMap<>();
    private final WeakHashMap<FishState, Set<Observer<MakeFadSet>>> fadSetObserversCache = new WeakHashMap<>();
    private final WeakHashMap<FishState, Set<Observer<MakeUnassociatedSet>>> unassociatedSetObserversCache =
        new WeakHashMap<>();
    private final WeakHashMap<FishState, CatchSampler> unassociatedCatchSamples = new WeakHashMap<>();
    private Set<Observer<DeployFad>> fadDeploymentObservers = new LinkedHashSet<>();
    private Set<Observer<MakeFadSet>> fadSetObservers = new LinkedHashSet<>();
    private Set<Observer<MakeUnassociatedSet>> unassociatedSetObservers = new LinkedHashSet<>();
    private GroupingMonitor<Species, BiomassLostEvent, Double, Mass> biomassLostMonitor;
    private List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations =
        ImmutableList.of(new ActiveFadLimitsFactory());
    private int initialNumberOfFads = 999999; // TODO: find plausible value and allow boats to refill
    private FadInitializerFactory fadInitializerFactory = new FadInitializerFactory();

    // see https://github.com/poseidon-fisheries/tuna/issues/114 re: set duration
    private DoubleParameter minimumFadSetDurationInHours = new FixedDoubleParameter(1.066667);
    private DoubleParameter averageFadSetDurationInHours = new FixedDoubleParameter(2.799212);
    private DoubleParameter stdDevOfFadSetDurationInHours = new FixedDoubleParameter(0.7325816);
    private DoubleParameter minimumUnassociatedSetDurationInHours = new FixedDoubleParameter(0.900000);
    private DoubleParameter averageUnassociatedSetDurationInHours = new FixedDoubleParameter(2.301648);
    private DoubleParameter stdDevOfUnassociatedSetDurationInHours = new FixedDoubleParameter(1.0158160);
    // See https://github.com/nicolaspayette/tuna/issues/8 re: successful set probability
    private DoubleParameter successfulSetProbability = new FixedDoubleParameter(0.9231701);
    private Path unassociatedCatchSampleFile;

    public DoubleParameter getMinimumUnassociatedSetDurationInHours() { return minimumUnassociatedSetDurationInHours; }

    public void setMinimumUnassociatedSetDurationInHours(final DoubleParameter minimumUnassociatedSetDurationInHours) {
        this.minimumUnassociatedSetDurationInHours = minimumUnassociatedSetDurationInHours;
    }

    public DoubleParameter getAverageUnassociatedSetDurationInHours() { return averageUnassociatedSetDurationInHours; }

    public void setAverageUnassociatedSetDurationInHours(final DoubleParameter averageUnassociatedSetDurationInHours) {
        this.averageUnassociatedSetDurationInHours = averageUnassociatedSetDurationInHours;
    }

    public DoubleParameter getStdDevOfUnassociatedSetDurationInHours() { return stdDevOfUnassociatedSetDurationInHours; }

    public void setStdDevOfUnassociatedSetDurationInHours(final DoubleParameter stdDevOfUnassociatedSetDurationInHours) {
        this.stdDevOfUnassociatedSetDurationInHours = stdDevOfUnassociatedSetDurationInHours;
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
    public DoubleParameter getMinimumFadSetDurationInHours() { return minimumFadSetDurationInHours; }

    @SuppressWarnings("unused")
    public void setMinimumFadSetDurationInHours(DoubleParameter minimumFadSetDurationInHours) {
        this.minimumFadSetDurationInHours = minimumFadSetDurationInHours;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getAverageFadSetDurationInHours() { return averageFadSetDurationInHours; }

    @SuppressWarnings("unused")
    public void setAverageFadSetDurationInHours(DoubleParameter averageFadSetDurationInHours) {
        this.averageFadSetDurationInHours = averageFadSetDurationInHours;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getStdDevOfFadSetDurationInHours() { return stdDevOfFadSetDurationInHours; }

    @SuppressWarnings("unused")
    public void setStdDevOfFadSetDurationInHours(DoubleParameter stdDevOfFadSetDurationInHours) {
        this.stdDevOfFadSetDurationInHours = stdDevOfFadSetDurationInHours;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getSuccessfulSetProbability() { return successfulSetProbability; }

    @SuppressWarnings("unused") public void setSuccessfulSetProbability(DoubleParameter successfulSetProbability) {
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
    public Set<Observer<DeployFad>> getFadDeploymentObservers() { return fadDeploymentObservers; }

    @SuppressWarnings("unused") public void setFadDeploymentObservers(Set<Observer<DeployFad>> fadDeploymentObservers) {
        this.fadDeploymentObservers = fadDeploymentObservers;
    }

    @SuppressWarnings("unused")
    public Set<Observer<MakeFadSet>> getFadSetObservers() { return fadSetObservers; }

    @SuppressWarnings("unused") public void setFadSetObservers(Set<Observer<MakeFadSet>> fadSetObservers) {
        this.fadSetObservers = fadSetObservers;
    }

    @SuppressWarnings("unused")
    public Set<Observer<MakeUnassociatedSet>> getUnassociatedSetObservers() { return unassociatedSetObservers; }

    @SuppressWarnings("unused")
    public void setUnassociatedSetObservers(Set<Observer<MakeUnassociatedSet>> unassociatedSetObservers) {
        this.unassociatedSetObservers = unassociatedSetObservers;
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
            fadDeploymentObserversCache.computeIfAbsent(fishState, __ -> new LinkedHashSet<>(fadDeploymentObservers)),
            fadSetObserversCache.computeIfAbsent(fishState, __ -> new LinkedHashSet<>(fadSetObservers)),
            unassociatedSetObserversCache.computeIfAbsent(
                fishState,
                __ -> new LinkedHashSet<>(unassociatedSetObservers)
            ),
            Optional.of(biomassLostMonitor),
            actionSpecificRegulations
        );

        final MersenneTwisterFast rng = fishState.getRandom();
        CatchSampler unassociatedCatchSampler =
            unassociatedCatchSamples.computeIfAbsent(
                fishState,
                __ -> new CatchSampler(readUnassociatedCatchSamples(fishState.getBiology()), rng)
            );

        return new PurseSeineGear(
            fadManager,
            minimumFadSetDurationInHours.apply(rng),
            averageFadSetDurationInHours.apply(rng),
            stdDevOfFadSetDurationInHours.apply(rng),
            minimumUnassociatedSetDurationInHours.apply(rng),
            averageUnassociatedSetDurationInHours.apply(rng),
            stdDevOfUnassociatedSetDurationInHours.apply(rng),
            successfulSetProbability.apply(rng),
            unassociatedCatchSampler
        );
    }

    private List<Iterable<Double>> readUnassociatedCatchSamples(final GlobalBiology globalBiology) {
        return parseAllRecords(unassociatedCatchSampleFile).stream()
            .map(r -> Arrays.stream(r.getMetaData().headers())
                .collect(toImmutableSortedMap(
                    Ordering.natural(),
                    speciesCode -> globalBiology
                        .getSpecie(TunaScenario.speciesNames.get(speciesCode.toUpperCase()))
                        .getIndex(),
                    speciesCode -> r.getDouble(speciesCode) * 1000 // convert tonnes to kg
                )).values())
            .collect(toImmutableList());
    }

    @SuppressWarnings("unused") public Path getUnassociatedCatchSampleFile() {
        return unassociatedCatchSampleFile;
    }

    public void setUnassociatedCatchSampleFile(Path unassociatedCatchSampleFile) {
        this.unassociatedCatchSampleFile = unassociatedCatchSampleFile;
    }

}
