package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class WeibullPerSpeciesCarryingCapacitiesFromFileFactory
    implements AlgorithmFactory<CarryingCapacitySupplier> {

    private DoubleParameter capacityScalingFactor;
    private DoubleParameter shapeScalingFactor;
    private IntegerParameter targetYear;
    private InputPath fadCarryingCapacityFile;

    @SuppressWarnings("unused")
    public WeibullPerSpeciesCarryingCapacitiesFromFileFactory() {
    }

    public WeibullPerSpeciesCarryingCapacitiesFromFileFactory(
        final InputPath fadCarryingCapacityFile,
        final IntegerParameter targetYear,
        final DoubleParameter capacityScalingFactor,
        final DoubleParameter shapeScalingFactor
    ) {
        this.fadCarryingCapacityFile = fadCarryingCapacityFile;
        this.targetYear = targetYear;
        this.capacityScalingFactor = capacityScalingFactor;
        this.shapeScalingFactor = shapeScalingFactor;
    }

    @SuppressWarnings("unused")
    public DoubleParameter getShapeScalingFactor() {
        return shapeScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setShapeScalingFactor(final DoubleParameter shapeScalingFactor) {
        this.shapeScalingFactor = shapeScalingFactor;
    }

    @Override
    public CarryingCapacitySupplier apply(final FishState fishState) {
        final List<Record> recordList =
            recordStream(fadCarryingCapacityFile.get())
                .filter(record -> record.getInt("year") == targetYear.getIntValue())
                .collect(toImmutableList());
        return new WeibullPerSpeciesCarryingCapacitiesFactory(
            extractParameter(recordList, fishState, "weibull_shape"),
            extractParameter(recordList, fishState, "weibull_scale"),
            extractParameter(recordList, fishState, "probability_of_zeros"),
            capacityScalingFactor,
            shapeScalingFactor
        ).apply(fishState);
    }

    private static Map<String, DoubleParameter> extractParameter(
        final Collection<? extends Record> recordList,
        final FishState fishState,
        final String parameter
    ) {
        return recordList.stream().collect(toMap(
            record -> fishState.getBiology().getSpeciesByCode(record.getString("species_code")).getName(),
            record -> new FixedDoubleParameter(record.getDouble(parameter))
        ));
    }

    @SuppressWarnings("unused")
    public DoubleParameter getCapacityScalingFactor() {
        return capacityScalingFactor;
    }

    @SuppressWarnings("unused")
    public void setCapacityScalingFactor(final DoubleParameter capacityScalingFactor) {
        this.capacityScalingFactor = capacityScalingFactor;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    @SuppressWarnings("unused")
    public InputPath getFadCarryingCapacityFile() {
        return fadCarryingCapacityFile;
    }

    @SuppressWarnings("unused")
    public void setFadCarryingCapacityFile(final InputPath fadCarryingCapacityFile) {
        this.fadCarryingCapacityFile = fadCarryingCapacityFile;
    }

}
