package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class WeibullPerSpeciesCarryingCapacitiesFromFileFactory
    implements uk.ac.ox.oxfish.geography.fads.CarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> {

    private DoubleParameter scalingFactor;
    private IntegerParameter targetYear;
    private InputPath fadCarryingCapacityFile;

    @SuppressWarnings("unused")
    public WeibullPerSpeciesCarryingCapacitiesFromFileFactory() {
    }

    public WeibullPerSpeciesCarryingCapacitiesFromFileFactory(
        final InputPath fadCarryingCapacityFile,
        final IntegerParameter targetYear,
        final DoubleParameter scalingFactor
    ) {
        this.fadCarryingCapacityFile = fadCarryingCapacityFile;
        this.targetYear = targetYear;
        this.scalingFactor = scalingFactor;
    }

    @Override
    public CarryingCapacityInitializer<PerSpeciesCarryingCapacity> apply(final FishState fishState) {

        final List<Record> recordList =
            recordStream(fadCarryingCapacityFile.get())
                .filter(record -> record.getInt("year") == targetYear.getIntValue())
                .collect(toImmutableList());

        final Map<String, DoubleParameter> yearShapeParameters = recordList.stream()
            .collect(Collectors.toMap(
                record -> record.getString("species_code"),
                record -> new FixedDoubleParameter(record.getDouble("weibull_shape"))
            ));

        final Map<String, DoubleParameter> yearScaleParameters = recordList.stream()
            .collect(Collectors.toMap(
                record -> record.getString("species_code"),
                record -> new FixedDoubleParameter(record.getDouble("weibull_scale"))
            ));

        final Map<String, DoubleParameter> yearProportionOfZeros = recordList.stream()
            .collect(Collectors.toMap(
                record -> record.getString("species_code"),
                record -> new FixedDoubleParameter(record.getDouble("probability_of_zeros"))
            ));

        return new WeibullPerSpeciesCarryingCapacitiesFactory(
            yearShapeParameters,
            yearScaleParameters,
            yearProportionOfZeros,
            scalingFactor
        ).apply(fishState);
    }

    @SuppressWarnings("unused")
    public DoubleParameter getScalingFactor() {
        return scalingFactor;
    }

    @SuppressWarnings("unused")
    public void setScalingFactor(final DoubleParameter scalingFactor) {
        this.scalingFactor = scalingFactor;
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