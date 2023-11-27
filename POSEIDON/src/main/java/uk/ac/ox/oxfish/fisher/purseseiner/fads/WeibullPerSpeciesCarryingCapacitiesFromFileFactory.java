package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class WeibullPerSpeciesCarryingCapacitiesFromFileFactory
        implements uk.ac.ox.oxfish.geography.fads.CarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> {

    private DoubleParameter scalingFactor;
    private IntegerParameter targetYear;
    private InputPath fadCarryingCapacityParameters;

    public WeibullPerSpeciesCarryingCapacitiesFromFileFactory(
            final InputPath fadCarryingCapacityParameters,
            final IntegerParameter targetYear,
            final DoubleParameter scalingFactor
    ) {
        this.fadCarryingCapacityParameters = fadCarryingCapacityParameters;
        this.targetYear = targetYear;
        this.scalingFactor = scalingFactor;
    }

    @Override
    public CarryingCapacityInitializer<PerSpeciesCarryingCapacity> apply(FishState fishState) {

        Map<Integer, Map<String, DoubleParameter>> yearShapeParameters = recordStream((Path) fadCarryingCapacityParameters)
                .collect(Collectors.groupingBy(record -> record.getInt("year"),
                        Collectors.toMap(record -> record.getString("species_code"), record -> new FixedDoubleParameter(record.getDouble("weibull_shape")))));

        final Map<String, DoubleParameter> shapeParameters = yearShapeParameters.get(targetYear);

        Map<Integer, Map<String, DoubleParameter>> yearScaleParameters = recordStream((Path) fadCarryingCapacityParameters)
                .collect(Collectors.groupingBy(record -> record.getInt("year"),
                        Collectors.toMap(record -> record.getString("species_code"), record -> new FixedDoubleParameter(record.getDouble("weibull_scale")))));

        final Map<String, DoubleParameter> scaleParameters = yearScaleParameters.get(targetYear);

        Map<Integer, Map<String, DoubleParameter>> yearProportionOfZeros = recordStream((Path) fadCarryingCapacityParameters)
                .collect(Collectors.groupingBy(record -> record.getInt("year"),
                        Collectors.toMap(record -> record.getString("species_code"), record -> new FixedDoubleParameter(record.getDouble("weibull_scale")))));

        final Map<String, DoubleParameter> proportionOfZeros = yearProportionOfZeros.get(targetYear);

        new WeibullPerSpeciesCarryingCapacitiesFactory(shapeParameters, scaleParameters, proportionOfZeros, scalingFactor);
        return null;
    }

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }


    public DoubleParameter getScalingFactor() {
        return scalingFactor;
    }

    public void setScalingFactor(final DoubleParameter scalingFactor) {
        this.scalingFactor = scalingFactor;
    }



}
