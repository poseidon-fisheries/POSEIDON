package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.fisher.equipment.gear.fads.PurseSeineGear;
import uk.ac.ox.oxfish.geography.fads.FadInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.fads.ActionSpecificRegulation;
import uk.ac.ox.oxfish.model.regs.fads.ActiveFadLimitsFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

public class PurseSeineGearFactory implements AlgorithmFactory<PurseSeineGear> {

    private List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations =
        ImmutableList.of(new ActiveFadLimitsFactory());
    private int initialNumberOfFads = 999999; // TODO: find plausible value and allow boats to refill
    private FadInitializerFactory fadInitializerFactory = new FadInitializerFactory();
    // see https://github.com/poseidon-fisheries/tuna/issues/7 re: set duration
    private DoubleParameter minimumSetDurationInHours = new FixedDoubleParameter(3.03333333333333);
    private DoubleParameter averageSetDurationInHours = new FixedDoubleParameter(8.0219505805135);
    private DoubleParameter stdDevOfSetDurationInHours = new FixedDoubleParameter(2.99113291538723);
    // See https://github.com/nicolaspayette/tuna/issues/8 re: successful set probability
    private DoubleParameter successfulSetProbability = new FixedDoubleParameter(0.9231701);
    private DoubleParameter dudProbability = new FixedDoubleParameter(0d);
    private Path unassociatedCatchSampleFile;

    @SuppressWarnings("unused") public List<AlgorithmFactory<? extends ActionSpecificRegulation>> getActionSpecificRegulations() {
        return actionSpecificRegulations;
    }

    public void setActionSpecificRegulations(List<AlgorithmFactory<? extends ActionSpecificRegulation>> actionSpecificRegulations) {
        this.actionSpecificRegulations = actionSpecificRegulations;
    }

    @SuppressWarnings("unused") public DoubleParameter getMinimumSetDurationInHours() { return minimumSetDurationInHours; }

    @SuppressWarnings("unused") public void setMinimumSetDurationInHours(DoubleParameter minimumSetDurationInHours) {
        this.minimumSetDurationInHours = minimumSetDurationInHours;
    }

    @SuppressWarnings("unused") public DoubleParameter getAverageSetDurationInHours() { return averageSetDurationInHours; }

    @SuppressWarnings("unused") public void setAverageSetDurationInHours(DoubleParameter averageSetDurationInHours) {
        this.averageSetDurationInHours = averageSetDurationInHours;
    }

    @SuppressWarnings("unused") public DoubleParameter getStdDevOfSetDurationInHours() { return stdDevOfSetDurationInHours; }

    @SuppressWarnings("unused") public void setStdDevOfSetDurationInHours(DoubleParameter stdDevOfSetDurationInHours) {
        this.stdDevOfSetDurationInHours = stdDevOfSetDurationInHours;
    }

    @SuppressWarnings("unused") public DoubleParameter getSuccessfulSetProbability() { return successfulSetProbability; }

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

    @Override
    public PurseSeineGear apply(FishState fishState) {
        final FadManager fadManager = new FadManager(
            fishState.getFadMap(),
            fadInitializerFactory.apply(fishState),
            initialNumberOfFads,
            dudProbability.apply(fishState.getRandom()),
            actionSpecificRegulations.stream().map(factory -> factory.apply(fishState))
        );
        final MersenneTwisterFast rng = fishState.getRandom();
        double[][] unassociatedCatchSamples =
            parseAllRecords(unassociatedCatchSampleFile).stream()
                .map(r ->
                    IntStream.range(0, fishState.getBiology().getSize())
                        .mapToDouble(i -> r.getDouble(i) * 1000).toArray() // convert tonnes to kg
                ).toArray(double[][]::new);

        return new PurseSeineGear(
            fadManager,
            minimumSetDurationInHours.apply(rng),
            averageSetDurationInHours.apply(rng),
            stdDevOfSetDurationInHours.apply(rng),
            successfulSetProbability.apply(rng),
            unassociatedCatchSamples
        );
    }

    @SuppressWarnings("unused") public Path getUnassociatedCatchSampleFile() {
        return unassociatedCatchSampleFile;
    }

    public void setUnassociatedCatchSampleFile(Path unassociatedCatchSampleFile) {
        this.unassociatedCatchSampleFile = unassociatedCatchSampleFile;
    }

    @SuppressWarnings("unused") public DoubleParameter getDudProbability() {
        return dudProbability;
    }

    @SuppressWarnings("unused") public void setDudProbability(DoubleParameter dudProbability) {
        this.dudProbability = dudProbability;
    }
}
